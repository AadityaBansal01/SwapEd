package com.swaped.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

/**
 * JPA Entity mapped to the 'listings' table in swap_ed_db.
 * Represents a textbook, lab coat, drafting tool, or academic item listed by a student.
 */
@Entity
@Table(name = "listings")
public class Listing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "listing_id")
    private Long listingId;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "seller_id", nullable = false)
    private Student seller;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(name = "title", nullable = false, length = 180)
    private String title;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "course_name", nullable = false, length = 150)
    private String courseName;

    @Column(name = "course_code", nullable = false, length = 40)
    private String courseCode;

    @Column(name = "selling_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal sellingPrice;

    @Column(name = "original_price", precision = 10, scale = 2)
    private BigDecimal originalPrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "item_condition", nullable = false, length = 30)
    private ItemCondition itemCondition;

    @Column(name = "image_path", length = 500)
    private String imagePath;

    @Column(name = "pickup_location", nullable = false, length = 180)
    private String pickupLocation;

    @Enumerated(EnumType.STRING)
    @Column(name = "listing_status", nullable = false, length = 30)
    private ListingStatus listingStatus = ListingStatus.AVAILABLE;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Listing() {
    }

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (this.createdAt == null) {
            this.createdAt = now;
        }
        this.updatedAt = now;
        if (this.courseCode != null) {
            this.courseCode = this.courseCode.trim().toUpperCase();
        }
        if (this.listingStatus == null) {
            this.listingStatus = ListingStatus.AVAILABLE;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
        if (this.courseCode != null) {
            this.courseCode = this.courseCode.trim().toUpperCase();
        }
    }

    public boolean isAvailable() {
        return this.listingStatus == ListingStatus.AVAILABLE;
    }

    public int getDiscountPercentage() {
        if (originalPrice == null || sellingPrice == null) {
            return 0;
        }
        if (originalPrice.compareTo(BigDecimal.ZERO) <= 0 || originalPrice.compareTo(sellingPrice) <= 0) {
            return 0;
        }
        BigDecimal savings = originalPrice.subtract(sellingPrice);
        return savings.multiply(BigDecimal.valueOf(100))
                .divide(originalPrice, 0, RoundingMode.HALF_UP)
                .intValue();
    }

    public String getResolvedImagePath() {
        if (imagePath != null && !imagePath.isBlank()) {
            return imagePath;
        }
        if (category != null && category.getCategoryName() != null) {
            String name = category.getCategoryName().toLowerCase();
            if (name.contains("textbook")) return "/images/products/textbook-default.svg";
            if (name.contains("lab")) return "/images/products/labcoat-default.svg";
            if (name.contains("draw") || name.contains("draft")) return "/images/products/drafting-default.svg";
            if (name.contains("calc")) return "/images/products/calculator-default.svg";
            if (name.contains("equip") || name.contains("engineer")) return "/images/products/equipment-default.svg";
        }
        return "/images/products/essentials-default.svg";
    }

    public Long getListingId() {
        return listingId;
    }

    public void setListingId(Long listingId) {
        this.listingId = listingId;
    }

    public Student getSeller() {
        return seller;
    }

    public void setSeller(Student seller) {
        this.seller = seller;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode != null ? courseCode.trim().toUpperCase() : null;
    }

    public BigDecimal getSellingPrice() {
        return sellingPrice;
    }

    public void setSellingPrice(BigDecimal sellingPrice) {
        this.sellingPrice = sellingPrice;
    }

    public BigDecimal getOriginalPrice() {
        return originalPrice;
    }

    public void setOriginalPrice(BigDecimal originalPrice) {
        this.originalPrice = originalPrice;
    }

    public ItemCondition getItemCondition() {
        return itemCondition;
    }

    public void setItemCondition(ItemCondition itemCondition) {
        this.itemCondition = itemCondition;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getPickupLocation() {
        return pickupLocation;
    }

    public void setPickupLocation(String pickupLocation) {
        this.pickupLocation = pickupLocation;
    }

    public ListingStatus getListingStatus() {
        return listingStatus;
    }

    public void setListingStatus(ListingStatus listingStatus) {
        this.listingStatus = listingStatus;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
