package com.swaped.dto;

import com.swaped.entity.ItemCondition;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Data Transfer Object for creating and updating marketplace listings.
 */
public class ListingDto {

    private Long listingId;

    @NotBlank(message = "Item title is required.")
    @Size(min = 3, max = 180, message = "Item title must be between 3 and 180 characters.")
    private String title;

    @NotBlank(message = "Description is required.")
    @Size(min = 10, max = 3000, message = "Description must be between 10 and 3000 characters.")
    private String description;

    @NotNull(message = "Please select a category.")
    private Long categoryId;

    @NotBlank(message = "Course name is required.")
    @Size(min = 2, max = 150, message = "Course name must be between 2 and 150 characters.")
    private String courseName;

    @NotBlank(message = "Course code is required (e.g., UCS301, UMA010).")
    @Size(min = 2, max = 40, message = "Course code must be between 2 and 40 characters.")
    private String courseCode;

    @NotNull(message = "Selling price is required.")
    @DecimalMin(value = "0.00", inclusive = true, message = "Selling price cannot be negative.")
    private BigDecimal sellingPrice;

    @DecimalMin(value = "0.00", inclusive = true, message = "Original price cannot be negative.")
    private BigDecimal originalPrice;

    @NotNull(message = "Please select the item condition.")
    private ItemCondition itemCondition;

    @NotBlank(message = "Pickup location on campus is required.")
    @Size(min = 3, max = 180, message = "Pickup location must be between 3 and 180 characters.")
    private String pickupLocation;

    private String existingImagePath;

    public ListingDto() {
    }

    public Long getListingId() {
        return listingId;
    }

    public void setListingId(Long listingId) {
        this.listingId = listingId;
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

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
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
        this.courseCode = courseCode;
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

    public String getPickupLocation() {
        return pickupLocation;
    }

    public void setPickupLocation(String pickupLocation) {
        this.pickupLocation = pickupLocation;
    }

    public String getExistingImagePath() {
        return existingImagePath;
    }

    public void setExistingImagePath(String existingImagePath) {
        this.existingImagePath = existingImagePath;
    }
}
