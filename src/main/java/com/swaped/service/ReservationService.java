package com.swaped.service;

import com.swaped.entity.Listing;
import com.swaped.entity.ListingStatus;
import com.swaped.entity.Reservation;
import com.swaped.entity.ReservationStatus;
import com.swaped.entity.Student;
import com.swaped.exception.InvalidReservationException;
import com.swaped.exception.ResourceNotFoundException;
import com.swaped.exception.UnauthorizedActionException;
import com.swaped.repository.ListingRepository;
import com.swaped.repository.ReservationRepository;
import com.swaped.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implements transactional reservation management with pessimistic locking
 * to prevent race conditions and conflicting active reservations.
 */
@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ListingRepository listingRepository;
    private final StudentRepository studentRepository;

    public ReservationService(
            ReservationRepository reservationRepository,
            ListingRepository listingRepository,
            StudentRepository studentRepository
    ) {
        this.reservationRepository = reservationRepository;
        this.listingRepository = listingRepository;
        this.studentRepository = studentRepository;
    }

    @Transactional
    public Reservation requestReservation(Long listingId, Long buyerId) {
        Listing listing = listingRepository.findByIdForUpdate(listingId)
                .orElseThrow(() -> new ResourceNotFoundException("Listing #" + listingId + " was not found."));

        if (listing.getSeller().getStudentId().equals(buyerId)) {
            throw new InvalidReservationException("You cannot reserve your own marketplace listing.");
        }

        if (listing.getListingStatus() != ListingStatus.AVAILABLE) {
            throw new InvalidReservationException(
                    "This item is currently " + listing.getListingStatus().getDisplayName().toLowerCase() + " and cannot be reserved."
            );
        }

        if (reservationRepository.existsByListingListingIdAndReservationStatus(listingId, ReservationStatus.ACCEPTED)) {
            throw new InvalidReservationException("Another student already has an accepted pickup reservation for this item.");
        }

        boolean alreadyRequested = reservationRepository.existsByListingListingIdAndBuyerStudentIdAndReservationStatusIn(
                listingId,
                buyerId,
                List.of(ReservationStatus.PENDING, ReservationStatus.ACCEPTED)
        );
        if (alreadyRequested) {
            throw new InvalidReservationException("You already have an active reservation request for this item.");
        }

        Student buyer = studentRepository.findById(buyerId)
                .orElseThrow(() -> new ResourceNotFoundException("Student account not found."));

        Reservation reservation = new Reservation(listing, buyer, ReservationStatus.PENDING);
        return reservationRepository.save(reservation);
    }

    @Transactional
    public Reservation acceptReservation(Long reservationId, Long sellerId) {
        Reservation reservation = reservationRepository.findByIdForUpdate(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation #" + reservationId + " was not found."));

        Listing listing = listingRepository.findByIdForUpdate(reservation.getListing().getListingId())
                .orElseThrow(() -> new ResourceNotFoundException("Associated listing was not found."));

        if (!listing.getSeller().getStudentId().equals(sellerId)) {
            throw new UnauthorizedActionException("Only the seller of this item can accept reservation requests.");
        }

        if (reservation.getReservationStatus() != ReservationStatus.PENDING) {
            throw new InvalidReservationException(
                    "Only pending reservation requests can be accepted (current status: " + reservation.getReservationStatus().getDisplayName() + ")."
            );
        }

        if (listing.getListingStatus() != ListingStatus.AVAILABLE) {
            throw new InvalidReservationException("This listing is no longer available to reserve.");
        }

        if (reservationRepository.existsByListingListingIdAndReservationStatus(listing.getListingId(), ReservationStatus.ACCEPTED)) {
            throw new InvalidReservationException("Another reservation is already accepted for this listing.");
        }

        reservation.setReservationStatus(ReservationStatus.ACCEPTED);
        reservationRepository.save(reservation);

        listing.setListingStatus(ListingStatus.RESERVED);
        listingRepository.save(listing);

        // Automatically decline any other competing PENDING requests for the same listing
        List<Reservation> otherPending = reservationRepository.findByListingListingIdAndReservationStatus(
                listing.getListingId(),
                ReservationStatus.PENDING
        );
        for (Reservation other : otherPending) {
            if (!other.getReservationId().equals(reservationId)) {
                other.setReservationStatus(ReservationStatus.REJECTED);
                reservationRepository.save(other);
            }
        }

        return reservation;
    }

    @Transactional
    public Reservation rejectReservation(Long reservationId, Long sellerId) {
        Reservation reservation = reservationRepository.findByIdForUpdate(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation #" + reservationId + " was not found."));

        Listing listing = listingRepository.findByIdForUpdate(reservation.getListing().getListingId())
                .orElseThrow(() -> new ResourceNotFoundException("Associated listing was not found."));

        if (!listing.getSeller().getStudentId().equals(sellerId)) {
            throw new UnauthorizedActionException("Only the seller of this item can reject reservation requests.");
        }

        if (reservation.getReservationStatus() != ReservationStatus.PENDING
                && reservation.getReservationStatus() != ReservationStatus.ACCEPTED) {
            throw new InvalidReservationException("This reservation can no longer be rejected.");
        }

        boolean wasAccepted = reservation.getReservationStatus() == ReservationStatus.ACCEPTED;
        reservation.setReservationStatus(ReservationStatus.REJECTED);
        reservationRepository.save(reservation);

        if (wasAccepted && listing.getListingStatus() == ListingStatus.RESERVED) {
            listing.setListingStatus(ListingStatus.AVAILABLE);
            listingRepository.save(listing);
        }

        return reservation;
    }

    @Transactional
    public Reservation cancelReservation(Long reservationId, Long buyerId) {
        Reservation reservation = reservationRepository.findByIdForUpdate(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation #" + reservationId + " was not found."));

        Listing listing = listingRepository.findByIdForUpdate(reservation.getListing().getListingId())
                .orElseThrow(() -> new ResourceNotFoundException("Associated listing was not found."));

        if (!reservation.getBuyer().getStudentId().equals(buyerId)) {
            throw new UnauthorizedActionException("Only the buyer who placed this reservation can cancel it.");
        }

        if (reservation.getReservationStatus() != ReservationStatus.PENDING
                && reservation.getReservationStatus() != ReservationStatus.ACCEPTED) {
            throw new InvalidReservationException("Only pending or accepted reservations can be cancelled.");
        }

        boolean wasAccepted = reservation.getReservationStatus() == ReservationStatus.ACCEPTED;
        reservation.setReservationStatus(ReservationStatus.CANCELLED);
        reservationRepository.save(reservation);

        if (wasAccepted && listing.getListingStatus() == ListingStatus.RESERVED) {
            listing.setListingStatus(ListingStatus.AVAILABLE);
            listingRepository.save(listing);
        }

        return reservation;
    }

    @Transactional(readOnly = true)
    public List<Reservation> getBuyerReservations(Long buyerId) {
        return reservationRepository.findByBuyerStudentIdOrderByRequestedAtDesc(buyerId);
    }

    @Transactional(readOnly = true)
    public List<Reservation> getSellerIncomingRequests(Long sellerId) {
        return reservationRepository.findIncomingRequestsForSeller(sellerId);
    }
}
