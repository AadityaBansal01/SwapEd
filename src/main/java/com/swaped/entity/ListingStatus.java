package com.swaped.entity;

/**
 * Availability status of a marketplace listing.
 */
public enum ListingStatus {
    AVAILABLE("Available", "badge-status-available"),
    RESERVED("Reserved", "badge-status-reserved"),
    SOLD("Sold", "badge-status-sold"),
    INACTIVE("Unavailable", "badge-status-inactive");

    private final String displayName;
    private final String badgeClass;

    ListingStatus(String displayName, String badgeClass) {
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
