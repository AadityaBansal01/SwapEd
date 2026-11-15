package com.swaped.entity;

/**
 * Status of a completed or cancelled campus exchange trade.
 */
public enum TradeStatus {
    COMPLETED("Completed", "badge-trade-completed"),
    CANCELLED("Cancelled", "badge-trade-cancelled");

    private final String displayName;
    private final String badgeClass;

    TradeStatus(String displayName, String badgeClass) {
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
