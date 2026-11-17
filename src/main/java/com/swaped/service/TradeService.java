package com.swaped.service;

import com.swaped.entity.Listing;
import com.swaped.entity.ListingStatus;
import com.swaped.entity.Reservation;
import com.swaped.entity.ReservationStatus;
import com.swaped.entity.Trade;
import com.swaped.entity.TradeStatus;
import com.swaped.exception.InvalidReservationException;
import com.swaped.exception.ResourceNotFoundException;
import com.swaped.exception.UnauthorizedActionException;
import com.swaped.repository.ListingRepository;
import com.swaped.repository.ReservationRepository;
import com.swaped.repository.TradeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Handles offline campus handover confirmation and trade history retrieval.
 */
@Service
public class TradeService {

    private final TradeRepository tradeRepository;
    private final ReservationRepository reservationRepository;
    private final ListingRepository listingRepository;

    public TradeService(
            TradeRepository tradeRepository,
            ReservationRepository reservationRepository,
            ListingRepository listingRepository
    ) {
        this.tradeRepository = tradeRepository;
        this.reservationRepository = reservationRepository;
        this.listingRepository = listingRepository;
    }

    @Transactional
    public Trade confirmHandoverAndCompleteTrade(Long reservationId, Long sellerId) {
        Reservation reservation = reservationRepository.findByIdForUpdate(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation #" + reservationId + " was not found."));

        Listing listing = listingRepository.findByIdForUpdate(reservation.getListing().getListingId())
                .orElseThrow(() -> new ResourceNotFoundException("Associated listing was not found."));

        if (!listing.getSeller().getStudentId().equals(sellerId)) {
            throw new UnauthorizedActionException("Only the seller of the item can confirm handover and complete the trade.");
        }

        if (reservation.getReservationStatus() != ReservationStatus.ACCEPTED) {
            throw new InvalidReservationException(
                    "Cannot complete trade: the reservation must be in ACCEPTED state before confirming campus handover."
            );
        }

        if (listing.getListingStatus() == ListingStatus.SOLD) {
            throw new InvalidReservationException("This listing has already been marked as sold.");
        }

        reservation.setReservationStatus(ReservationStatus.COMPLETED);
        reservationRepository.save(reservation);

        listing.setListingStatus(ListingStatus.SOLD);
        listingRepository.save(listing);

        Trade completedTrade = new Trade(
                listing,
                reservation.getBuyer(),
                listing.getSeller(),
                listing.getSellingPrice(),
                TradeStatus.COMPLETED
        );
        return tradeRepository.save(completedTrade);
    }

    @Transactional(readOnly = true)
    public List<Trade> getTradesForStudent(Long studentId) {
        return tradeRepository.findStudentTradeHistory(studentId);
    }
}
