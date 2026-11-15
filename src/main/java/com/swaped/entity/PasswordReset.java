package com.swaped.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * JPA Entity mapped to the 'password_resets' table in swap_ed_db.
 * Extends SecurityTokenBase to inherit single-use, time-bound token behavior.
 */
@Entity
@Table(name = "password_resets")
public class PasswordReset extends SecurityTokenBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reset_id")
    private Long resetId;

    public PasswordReset() {
        super();
    }

    public PasswordReset(Student student, String tokenHash, LocalDateTime expiresAt) {
        super(student, tokenHash, expiresAt);
    }

    @Override
    public String getTokenPurpose() {
        return "PASSWORD_RESET";
    }

    public Long getResetId() {
        return resetId;
    }

    public void setResetId(Long resetId) {
        this.resetId = resetId;
    }
}
