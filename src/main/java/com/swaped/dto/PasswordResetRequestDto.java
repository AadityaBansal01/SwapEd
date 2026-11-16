package com.swaped.dto;

import com.swaped.validation.ThaparEmail;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO for requesting a password reset link by Thapar email.
 */
public class PasswordResetRequestDto {

    @NotBlank(message = "Please enter your registered Thapar email address.")
    @ThaparEmail
    private String email;

    public PasswordResetRequestDto() {
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
