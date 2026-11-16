package com.swaped.dto;

import com.swaped.validation.ThaparEmail;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Data Transfer Object for student registration requests.
 */
public class RegistrationDto {

    @NotBlank(message = "Full name is required.")
    @Size(min = 2, max = 120, message = "Full name must be between 2 and 120 characters.")
    private String fullName;

    @NotBlank(message = "Thapar email address is required.")
    @ThaparEmail
    private String email;

    @NotBlank(message = "Student ID / Roll Number is required.")
    @Size(min = 4, max = 40, message = "Student ID must be between 4 and 40 characters.")
    private String collegeId;

    @NotBlank(message = "Department is required.")
    @Size(max = 120, message = "Department must be at most 120 characters.")
    private String department;

    @NotBlank(message = "Course / Program is required.")
    @Size(max = 120, message = "Course must be at most 120 characters.")
    private String course;

    @NotBlank(message = "Password is required.")
    @Size(min = 8, max = 72, message = "Password must be at least 8 characters long.")
    private String password;

    @NotBlank(message = "Please confirm your password.")
    private String confirmPassword;

    public RegistrationDto() {
    }

    public boolean passwordsMatch() {
        return password != null && password.equals(confirmPassword);
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCollegeId() {
        return collegeId;
    }

    public void setCollegeId(String collegeId) {
        this.collegeId = collegeId;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }
}
