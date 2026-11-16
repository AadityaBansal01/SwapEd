package com.swaped.service;

import com.swaped.dto.PasswordResetConfirmDto;
import com.swaped.dto.RegistrationDto;
import com.swaped.entity.EmailVerification;
import com.swaped.entity.PasswordReset;
import com.swaped.entity.Student;
import com.swaped.exception.DuplicateResourceException;
import com.swaped.exception.SwapEdException;
import com.swaped.repository.EmailVerificationRepository;
import com.swaped.repository.PasswordResetRepository;
import com.swaped.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;

/**
 * Handles student registration, @thapar.edu domain enforcement, single-use
 * email verification tokens, and password reset workflows.
 */
@Service
public class AuthService {

    private final StudentRepository studentRepository;
    private final EmailVerificationRepository emailVerificationRepository;
    private final PasswordResetRepository passwordResetRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${swaped.auth.allowed-domain:thapar.edu}")
    private String allowedDomain;

    @Value("${swaped.auth.verification-expiry-hours:24}")
    private int verificationExpiryHours;

    @Value("${swaped.auth.reset-expiry-minutes:30}")
    private int resetExpiryMinutes;

    public AuthService(
            StudentRepository studentRepository,
            EmailVerificationRepository emailVerificationRepository,
            PasswordResetRepository passwordResetRepository,
            PasswordEncoder passwordEncoder,
            EmailService emailService
    ) {
        this.studentRepository = studentRepository;
        this.emailVerificationRepository = emailVerificationRepository;
        this.passwordResetRepository = passwordResetRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    @Transactional
    public EmailService.EmailDispatchResult registerStudent(RegistrationDto dto) {
        String normalizedEmail = normalizeAndValidateDomain(dto.getEmail());
        String normalizedCollegeId = dto.getCollegeId().trim();

        if (!dto.passwordsMatch()) {
            throw new SwapEdException("Passwords do not match.");
        }
        if (dto.getPassword() == null || dto.getPassword().length() < 8) {
            throw new SwapEdException("Password must be at least 8 characters long.");
        }

        if (studentRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new DuplicateResourceException("An account with this Thapar email address (" + normalizedEmail + ") already exists.");
        }
        if (studentRepository.existsByCollegeIdIgnoreCase(normalizedCollegeId)) {
            throw new DuplicateResourceException("An account with Student ID / Roll Number (" + normalizedCollegeId + ") already exists.");
        }

        Student student = new Student(
                dto.getFullName().trim(),
                normalizedEmail,
                normalizedCollegeId,
                dto.getDepartment().trim(),
                dto.getCourse().trim(),
                passwordEncoder.encode(dto.getPassword()),
                false
        );
        student = studentRepository.save(student);

        String rawToken = generateSecureToken();
        EmailVerification verification = new EmailVerification(
                student,
                hashToken(rawToken),
                LocalDateTime.now().plusHours(verificationExpiryHours)
        );
        emailVerificationRepository.save(verification);

        return emailService.sendVerificationEmail(student.getEmail(), student.getFullName(), rawToken);
    }

    @Transactional
    public EmailService.EmailDispatchResult resendVerificationEmail(String email) {
        String normalizedEmail = normalizeAndValidateDomain(email);
        Student student = studentRepository.findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(() -> new SwapEdException("No student account found for " + normalizedEmail + "."));

        if (student.isEmailVerified()) {
            throw new SwapEdException("This Thapar student account is already verified. You can sign in directly.");
        }

        List<EmailVerification> existingTokens = emailVerificationRepository.findByStudentStudentIdAndUsedAtIsNull(student.getStudentId());
        for (EmailVerification oldToken : existingTokens) {
            oldToken.markAsUsed();
        }
        emailVerificationRepository.saveAll(existingTokens);

        String rawToken = generateSecureToken();
        EmailVerification verification = new EmailVerification(
                student,
                hashToken(rawToken),
                LocalDateTime.now().plusHours(verificationExpiryHours)
        );
        emailVerificationRepository.save(verification);

        return emailService.sendVerificationEmail(student.getEmail(), student.getFullName(), rawToken);
    }

    @Transactional
    public Student verifyEmailToken(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            throw new SwapEdException("Verification token is missing or invalid.");
        }
        String tokenHash = hashToken(rawToken.trim());
        EmailVerification verification = emailVerificationRepository.findByTokenHashWithStudent(tokenHash)
                .orElseThrow(() -> new SwapEdException("Invalid verification link. Please request a new verification email."));

        if (verification.isUsed()) {
            throw new SwapEdException("This verification link has already been used. Please sign in to your account.");
        }
        if (verification.isExpired()) {
            throw new SwapEdException("This verification link has expired. Please request a new verification email.");
        }

        verification.markAsUsed();
        emailVerificationRepository.save(verification);

        Student student = verification.getStudent();
        student.setEmailVerified(true);
        return studentRepository.save(student);
    }

    @Transactional
    public EmailService.EmailDispatchResult initiatePasswordReset(String email) {
        String normalizedEmail = normalizeAndValidateDomain(email);
        Student student = studentRepository.findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(() -> new SwapEdException("No registered Thapar student account found for " + normalizedEmail + "."));

        List<PasswordReset> activeTokens = passwordResetRepository.findByStudentStudentIdAndUsedAtIsNull(student.getStudentId());
        for (PasswordReset oldToken : activeTokens) {
            oldToken.markAsUsed();
        }
        passwordResetRepository.saveAll(activeTokens);

        String rawToken = generateSecureToken();
        PasswordReset reset = new PasswordReset(
                student,
                hashToken(rawToken),
                LocalDateTime.now().plusMinutes(resetExpiryMinutes)
        );
        passwordResetRepository.save(reset);

        return emailService.sendPasswordResetEmail(student.getEmail(), student.getFullName(), rawToken);
    }

    @Transactional(readOnly = true)
    public PasswordReset validateResetToken(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            throw new SwapEdException("Password reset token is missing.");
        }
        String tokenHash = hashToken(rawToken.trim());
        PasswordReset reset = passwordResetRepository.findByTokenHashWithStudent(tokenHash)
                .orElseThrow(() -> new SwapEdException("Invalid password reset link. Please request a new link."));

        if (reset.isUsed()) {
            throw new SwapEdException("This password reset link has already been used.");
        }
        if (reset.isExpired()) {
            throw new SwapEdException("This password reset link has expired. Please request a new link.");
        }
        return reset;
    }

    @Transactional
    public void resetPassword(PasswordResetConfirmDto dto) {
        if (!dto.passwordsMatch()) {
            throw new SwapEdException("Passwords do not match.");
        }
        if (dto.getNewPassword() == null || dto.getNewPassword().length() < 8) {
            throw new SwapEdException("New password must be at least 8 characters long.");
        }

        PasswordReset reset = validateResetToken(dto.getToken());
        reset.markAsUsed();
        passwordResetRepository.save(reset);

        Student student = reset.getStudent();
        student.setPasswordHash(passwordEncoder.encode(dto.getNewPassword()));
        student.setEmailVerified(true);
        studentRepository.save(student);
    }

    public String normalizeAndValidateDomain(String email) {
        if (email == null || email.isBlank()) {
            throw new SwapEdException("Thapar email address is required.");
        }
        String normalized = email.trim().toLowerCase();
        String domain = (allowedDomain != null && !allowedDomain.isBlank())
                ? allowedDomain.trim().toLowerCase()
                : "thapar.edu";
        if (domain.startsWith("@")) {
            domain = domain.substring(1);
        }
        if (!normalized.endsWith("@" + domain) || normalized.indexOf('@') <= 0) {
            throw new SwapEdException("Only official @" + domain + " email addresses are permitted on SwapEd.");
        }
        return normalized;
    }

    private String generateSecureToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }
}
