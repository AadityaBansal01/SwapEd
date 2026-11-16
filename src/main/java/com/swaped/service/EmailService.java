package com.swaped.service;

import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/**
 * Handles transactional emails (Email Verification and Password Reset) using JavaMailSender.
 * Never pretends an email was sent when SMTP credentials are not configured.
 */
@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String mailUsername;

    @Value("${spring.mail.password:}")
    private String mailPassword;

    @Value("${swaped.mail.from:noreply@swaped.thapar.edu}")
    private String mailFrom;

    @Value("${swaped.app.base-url:http://localhost:8080}")
    private String baseUrl;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public record EmailDispatchResult(boolean smtpSent, String actionUrl, String statusNotice) {
    }

    public boolean isSmtpConfigured() {
        return mailUsername != null && !mailUsername.isBlank()
                && mailPassword != null && !mailPassword.isBlank();
    }

    private String resolveActiveBaseUrl() {
        try {
            return ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
        } catch (Exception ignored) {
            return baseUrl;
        }
    }

    public EmailDispatchResult sendVerificationEmail(String recipientEmail, String fullName, String rawToken) {
        String verifyUrl = resolveActiveBaseUrl() + "/auth/verify-email?token=" + rawToken;
        log.info("[SwapEd Email Verification] Student: {} | Verification Link: {}", recipientEmail, verifyUrl);

        if (!isSmtpConfigured()) {
            String notice = "SMTP is not configured (set MAIL_USERNAME and MAIL_PASSWORD environment variables to send live emails). "
                    + "For local evaluation, use the verification link generated below.";
            return new EmailDispatchResult(false, verifyUrl, notice);
        }

        String subject = "Verify your SwapEd Thapar Student Account";
        String htmlBody = """
                <div style="font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; max-width: 560px; margin: 0 auto; padding: 32px; border: 1px solid #e4e4e7; border-radius: 12px; color: #18181b;">
                    <h2 style="margin-top: 0; color: #059669;">SwapEd — Campus Marketplace</h2>
                    <p>Hi <strong>%s</strong>,</p>
                    <p>Welcome to SwapEd! Please verify your official Thapar University email address to activate your student account.</p>
                    <p style="margin: 28px 0;">
                        <a href="%s" style="background-color: #059669; color: #ffffff; text-decoration: none; padding: 12px 24px; border-radius: 8px; font-weight: 600; display: inline-block;">Verify Thapar Email</a>
                    </p>
                    <p style="font-size: 13px; color: #71717a;">This verification link is single-use and expires in 24 hours.<br>Direct link: <a href="%s">%s</a></p>
                </div>
                """.formatted(fullName, verifyUrl, verifyUrl, verifyUrl);

        return dispatchHtmlEmail(recipientEmail, subject, htmlBody, verifyUrl);
    }

    public EmailDispatchResult sendPasswordResetEmail(String recipientEmail, String fullName, String rawToken) {
        String resetUrl = resolveActiveBaseUrl() + "/auth/reset-password?token=" + rawToken;
        log.info("[SwapEd Password Reset] Student: {} | Reset Link: {}", recipientEmail, resetUrl);

        if (!isSmtpConfigured()) {
            String notice = "SMTP is not configured (set MAIL_USERNAME and MAIL_PASSWORD environment variables to send live emails). "
                    + "For local evaluation, use the time-limited password reset link generated below.";
            return new EmailDispatchResult(false, resetUrl, notice);
        }

        String subject = "Reset your SwapEd Account Password";
        String htmlBody = """
                <div style="font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; max-width: 560px; margin: 0 auto; padding: 32px; border: 1px solid #e4e4e7; border-radius: 12px; color: #18181b;">
                    <h2 style="margin-top: 0; color: #059669;">SwapEd — Password Reset</h2>
                    <p>Hi <strong>%s</strong>,</p>
                    <p>We received a request to reset the password for your SwapEd student account (%s).</p>
                    <p style="margin: 28px 0;">
                        <a href="%s" style="background-color: #059669; color: #ffffff; text-decoration: none; padding: 12px 24px; border-radius: 8px; font-weight: 600; display: inline-block;">Reset Password</a>
                    </p>
                    <p style="font-size: 13px; color: #71717a;">This reset link is single-use and expires in 30 minutes.<br>Direct link: <a href="%s">%s</a></p>
                </div>
                """.formatted(fullName, recipientEmail, resetUrl, resetUrl, resetUrl);

        return dispatchHtmlEmail(recipientEmail, subject, htmlBody, resetUrl);
    }

    private EmailDispatchResult dispatchHtmlEmail(String to, String subject, String htmlBody, String actionUrl) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(mailFrom);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            mailSender.send(message);
            return new EmailDispatchResult(true, actionUrl, "Email successfully dispatched via SMTP to " + to + ".");
        } catch (Exception ex) {
            log.error("Failed to send SMTP email to {}: {}", to, ex.getMessage());
            String errorNotice = "SMTP dispatch failed (" + ex.getMessage() + "). Verify MAIL_HOST, MAIL_PORT, MAIL_USERNAME, and MAIL_PASSWORD.";
            return new EmailDispatchResult(false, actionUrl, errorNotice);
        }
    }
}
