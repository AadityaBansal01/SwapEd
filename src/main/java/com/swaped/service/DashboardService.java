package com.swaped.service;

import com.swaped.dto.DashboardStatsDto;
import com.swaped.entity.ListingStatus;
import com.swaped.entity.ReservationStatus;
import com.swaped.entity.TradeStatus;
import com.swaped.repository.ListingRepository;
import com.swaped.repository.ReservationRepository;
import com.swaped.repository.TradeRepository;
import com.swaped.repository.WishlistRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Computes real-time student dashboard statistics directly from MySQL tables.
 */
@Service
public class DashboardService {

    private final ListingRepository listingRepository;
    private final ReservationRepository reservationRepository;
    private final TradeRepository tradeRepository;
    private final WishlistRepository wishlistRepository;

    public DashboardService(
            ListingRepository listingRepository,
            ReservationRepository reservationRepository,
            TradeRepository tradeRepository,
            WishlistRepository wishlistRepository
    ) {
        this.listingRepository = listingRepository;
        this.reservationRepository = reservationRepository;
        this.tradeRepository = tradeRepository;
        this.wishlistRepository = wishlistRepository;
    }

    @Transactional(readOnly = true)
    public DashboardStatsDto getStatsForStudent(Long studentId) {
        long totalListings = listingRepository.countBySellerStudentId(studentId);
        long activeListings = listingRepository.countBySellerStudentIdAndListingStatus(studentId, ListingStatus.AVAILABLE);
        long buyerPendingReservations = reservationRepository.countByBuyerStudentIdAndReservationStatusIn(
                studentId,
                List.of(ReservationStatus.PENDING, ReservationStatus.ACCEPTED)
        );
        long incomingPending = reservationRepository.countIncomingBySellerAndStatus(studentId, ReservationStatus.PENDING);
        long incomingAccepted = reservationRepository.countIncomingBySellerAndStatus(studentId, ReservationStatus.ACCEPTED);
        long totalPendingReservations = buyerPendingReservations + incomingPending + incomingAccepted;
        long completedTrades = tradeRepository.countStudentTradesByStatus(studentId, TradeStatus.COMPLETED);
        long wishlistCount = wishlistRepository.countByStudentStudentId(studentId);

        return new DashboardStatsDto(
                totalListings,
                activeListings,
                totalPendingReservations,
                incomingPending + incomingAccepted,
                completedTrades,
                wishlistCount
        );
    }
}
