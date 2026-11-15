package com.swaped.entity;

/**
 * Represents the physical condition rating of a campus marketplace item.
 */
public enum ItemCondition {
    NEW("New", "Brand new, unused condition", "badge-condition-new"),
    LIKE_NEW("Like New", "Minimal wear, near-mint state", "badge-condition-likenew"),
    GOOD("Good", "Well-maintained with light campus use", "badge-condition-good"),
    FAIR("Fair", "Fully usable with visible notes or wear", "badge-condition-fair");

    private final String displayName;
    private final String description;
    private final String badgeClass;

    ItemCondition(String displayName, String description, String badgeClass) {
        this.displayName = displayName;
        this.description = description;
        this.badgeClass = badgeClass;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public String getBadgeClass() {
        return badgeClass;
    }
}
