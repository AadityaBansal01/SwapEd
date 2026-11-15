package com.swaped.entity;

/**
 * Lifecycle status of a buyer reservation request.
 */
public enum ReservationStatus {
    PENDING("Pending Approval", "badge-res-pending"),
    ACCEPTED("Accepted (Ready for Pickup)", "badge-res-accepted"),
    REJECTED("Rejected", "badge-res-rejected"),
    CANCELLED("Cancelled", "badge-res-cancelled"),
    COMPLETED("Handover Completed", "badge-res-completed");

    private final String displayName;
    private final String badgeClass;

    ReservationStatus(String displayName, String badgeClass) {
        this.displayName = displayName;
        this.badgeClass = badgeClass;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getBadgeClass() {
        return badgeClass;
    }
}
