package com.swaped.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * JPA Entity mapped to the 'email_verifications' table in swap_ed_db.
 * Extends SecurityTokenBase to inherit single-use, time-bound token behavior.
 */
@Entity
@Table(name = "email_verifications")
public class EmailVerification extends SecurityTokenBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "verification_id")
    private Long verificationId;

    public EmailVerification() {
        super();
    }

    public EmailVerification(Student student, String tokenHash, LocalDateTime expiresAt) {
        super(student, tokenHash, expiresAt);
    }

    @Override
    public String getTokenPurpose() {
        return "EMAIL_VERIFICATION";
    }

    public Long getVerificationId() {
        return verificationId;
    }

    public void setVerificationId(Long verificationId) {
        this.verificationId = verificationId;
    }
}
