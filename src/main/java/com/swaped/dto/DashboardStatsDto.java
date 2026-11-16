package com.swaped.dto;

/**
 * Aggregated student dashboard statistics retrieved from MySQL.
 */
public class DashboardStatsDto {

    private final long totalListings;
    private final long activeListings;
    private final long pendingReservations;
    private final long incomingPendingRequests;
    private final long completedTrades;
    private final long wishlistCount;

    public DashboardStatsDto(
            long totalListings,
            long activeListings,
            long pendingReservations,
            long incomingPendingRequests,
            long completedTrades,
            long wishlistCount
    ) {
        this.totalListings = totalListings;
        this.activeListings = activeListings;
        this.pendingReservations = pendingReservations;
        this.incomingPendingRequests = incomingPendingRequests;
        this.completedTrades = completedTrades;
        this.wishlistCount = wishlistCount;
    }

    public long getTotalListings() {
        return totalListings;
    }

    public long getActiveListings() {
        return activeListings;
    }

    public long getPendingReservations() {
        return pendingReservations;
    }

    public long getIncomingPendingRequests() {
        return incomingPendingRequests;
    }

    public long getCompletedTrades() {
        return completedTrades;
    }

    public long getWishlistCount() {
        return wishlistCount;
    }
}
