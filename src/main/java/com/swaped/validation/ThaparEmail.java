package com.swaped.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom Jakarta Bean Validation annotation to enforce official university email domain (@thapar.edu).
 */
@Documented
@Constraint(validatedBy = ThaparEmailValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ThaparEmail {

    String message() default "Only official Thapar University email addresses (@thapar.edu) are accepted.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
