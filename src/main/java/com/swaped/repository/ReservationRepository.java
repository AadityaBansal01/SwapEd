package com.swaped.repository;

import com.swaped.entity.Reservation;
import com.swaped.entity.ReservationStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM Reservation r JOIN FETCH r.listing l JOIN FETCH l.seller JOIN FETCH r.buyer WHERE r.reservationId = :reservationId")
    Optional<Reservation> findByIdForUpdate(@Param("reservationId") Long reservationId);

    List<Reservation> findByBuyerStudentIdOrderByRequestedAtDesc(Long buyerId);

    @Query("""
        SELECT r FROM Reservation r
        JOIN FETCH r.listing l
        JOIN FETCH l.seller s
        JOIN FETCH r.buyer b
        WHERE s.studentId = :sellerId
        ORDER BY r.requestedAt DESC
        """)
    List<Reservation> findIncomingRequestsForSeller(@Param("sellerId") Long sellerId);

    List<Reservation> findByListingListingIdOrderByRequestedAtDesc(Long listingId);

    List<Reservation> findByListingListingIdAndReservationStatus(Long listingId, ReservationStatus status);

    boolean existsByListingListingIdAndBuyerStudentIdAndReservationStatusIn(
            Long listingId,
            Long buyerId,
            Collection<ReservationStatus> statuses
    );

    boolean existsByListingListingIdAndReservationStatus(Long listingId, ReservationStatus status);

    long countByBuyerStudentIdAndReservationStatusIn(Long buyerId, Collection<ReservationStatus> statuses);

    @Query("SELECT COUNT(r) FROM Reservation r WHERE r.listing.seller.studentId = :sellerId AND r.reservationStatus = :status")
    long countIncomingBySellerAndStatus(@Param("sellerId") Long sellerId, @Param("status") ReservationStatus status);
}
