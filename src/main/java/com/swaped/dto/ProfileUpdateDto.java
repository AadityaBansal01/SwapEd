package com.swaped.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Data Transfer Object for updating permitted student profile details.
 */
public class ProfileUpdateDto {

    @NotBlank(message = "Full name is required.")
    @Size(min = 2, max = 120, message = "Full name must be between 2 and 120 characters.")
    private String fullName;

    @NotBlank(message = "Department is required.")
    @Size(max = 120, message = "Department must be at most 120 characters.")
    private String department;

    @NotBlank(message = "Course / Program is required.")
    @Size(max = 120, message = "Course must be at most 120 characters.")
    private String course;

    public ProfileUpdateDto() {
    }

    public ProfileUpdateDto(String fullName, String department, String course) {
        this.fullName = fullName;
        this.department = department;
        this.course = course;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getCourse() {
        return course;
    }

    public void setCourse(String course) {
        this.course = course;
    }
}
