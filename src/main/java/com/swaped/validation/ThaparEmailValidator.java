package com.swaped.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * Validates that an email address is structurally valid and ends with the configured
 * university domain (default: @thapar.edu).
 */
@Component
public class ThaparEmailValidator implements ConstraintValidator<ThaparEmail, String> {

    private static final Pattern EMAIL_STRUCTURE = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    @Value("${swaped.auth.allowed-domain:thapar.edu}")
    private String allowedDomain;

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return false;
        }
        String normalized = value.trim().toLowerCase();
        if (!EMAIL_STRUCTURE.matcher(normalized).matches()) {
            return false;
        }
        String domain = (allowedDomain != null && !allowedDomain.isBlank())
                ? allowedDomain.trim().toLowerCase()
                : "thapar.edu";
        if (domain.startsWith("@")) {
            domain = domain.substring(1);
        }
        boolean matchesDomain = normalized.endsWith("@" + domain);
        if (!matchesDomain && context != null) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                    "Only official @" + domain + " student email addresses are allowed (e.g., rjain1_be24@" + domain + ")."
            ).addConstraintViolation();
        }
        return matchesDomain;
    }
}
