package com.swaped.controller;

import com.swaped.dto.PasswordResetConfirmDto;
import com.swaped.dto.PasswordResetRequestDto;
import com.swaped.dto.RegistrationDto;
import com.swaped.entity.PasswordReset;
import com.swaped.entity.Student;
import com.swaped.exception.SwapEdException;
import com.swaped.security.CustomUserDetails;
import com.swaped.service.AuthService;
import com.swaped.service.EmailService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Handles student registration, email verification, login page, and password reset endpoints.
 */
@Controller
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/login")
    public String loginPage(
            @RequestParam(name = "error", required = false) String error,
            @RequestParam(name = "unverified", required = false) String unverified,
            @RequestParam(name = "email", required = false) String email,
            @AuthenticationPrincipal CustomUserDetails currentUser,
            Model model
    ) {
        if (currentUser != null) {
            return "redirect:/dashboard";
        }
        model.addAttribute("prefillEmail", email != null ? email : "");
        model.addAttribute("loginError", error != null);
        model.addAttribute("unverifiedError", unverified != null);
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerPage(@AuthenticationPrincipal CustomUserDetails currentUser, Model model) {
        if (currentUser != null) {
            return "redirect:/dashboard";
        }
        if (!model.containsAttribute("registrationDto")) {
            model.addAttribute("registrationDto", new RegistrationDto());
        }
        return "auth/register";
    }

    @PostMapping("/register")
    public String processRegistration(
            @Valid @ModelAttribute("registrationDto") RegistrationDto registrationDto,
            BindingResult bindingResult,
            Model model
    ) {
        if (!registrationDto.passwordsMatch()) {
            bindingResult.rejectValue("confirmPassword", "error.confirmPassword", "Confirm password does not match password.");
        }

        if (bindingResult.hasErrors()) {
            return "auth/register";
        }

        try {
            EmailService.EmailDispatchResult dispatchResult = authService.registerStudent(registrationDto);
            model.addAttribute("registeredEmail", registrationDto.getEmail().trim().toLowerCase());
            model.addAttribute("smtpSent", dispatchResult.smtpSent());
            model.addAttribute("verificationUrl", dispatchResult.actionUrl());
            model.addAttribute("statusNotice", dispatchResult.statusNotice());
            return "auth/verification-pending";
        } catch (SwapEdException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            return "auth/register";
        }
    }

    @GetMapping("/verify-email")
    public String verifyEmail(@RequestParam("token") String token, Model model) {
        try {
            Student verifiedStudent = authService.verifyEmailToken(token);
            model.addAttribute("verified", true);
            model.addAttribute("studentName", verifiedStudent.getFullName());
            model.addAttribute("studentEmail", verifiedStudent.getEmail());
            model.addAttribute("message", "Your Thapar student email has been verified! Your account is now active.");
        } catch (SwapEdException ex) {
            model.addAttribute("verified", false);
            model.addAttribute("message", ex.getMessage());
        }
        return "auth/verify-result";
    }

    @PostMapping("/resend-verification")
    public String resendVerification(@RequestParam("email") String email, Model model, RedirectAttributes redirectAttributes) {
        try {
            EmailService.EmailDispatchResult dispatchResult = authService.resendVerificationEmail(email);
            model.addAttribute("registeredEmail", email.trim().toLowerCase());
            model.addAttribute("smtpSent", dispatchResult.smtpSent());
            model.addAttribute("verificationUrl", dispatchResult.actionUrl());
            model.addAttribute("statusNotice", dispatchResult.statusNotice());
            return "auth/verification-pending";
        } catch (SwapEdException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/auth/login";
        }
    }

    @GetMapping("/forgot-password")
    public String forgotPasswordPage(Model model) {
        if (!model.containsAttribute("resetRequestDto")) {
            model.addAttribute("resetRequestDto", new PasswordResetRequestDto());
        }
        return "auth/forgot-password";
    }

    @PostMapping("/forgot-password")
    public String processForgotPassword(
            @Valid @ModelAttribute("resetRequestDto") PasswordResetRequestDto resetRequestDto,
            BindingResult bindingResult,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            return "auth/forgot-password";
        }
        try {
            EmailService.EmailDispatchResult dispatchResult = authService.initiatePasswordReset(resetRequestDto.getEmail());
            model.addAttribute("requestSubmitted", true);
            model.addAttribute("targetEmail", resetRequestDto.getEmail().trim().toLowerCase());
            model.addAttribute("smtpSent", dispatchResult.smtpSent());
            model.addAttribute("resetUrl", dispatchResult.actionUrl());
            model.addAttribute("statusNotice", dispatchResult.statusNotice());
            return "auth/forgot-password";
        } catch (SwapEdException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            return "auth/forgot-password";
        }
    }

    @GetMapping("/reset-password")
    public String resetPasswordPage(@RequestParam("token") String token, Model model, RedirectAttributes redirectAttributes) {
        try {
            PasswordReset reset = authService.validateResetToken(token);
            PasswordResetConfirmDto confirmDto = new PasswordResetConfirmDto();
            confirmDto.setToken(token);
            model.addAttribute("resetConfirmDto", confirmDto);
            model.addAttribute("studentEmail", reset.getStudent().getEmail());
            return "auth/reset-password";
        } catch (SwapEdException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/auth/forgot-password";
        }
    }

    @PostMapping("/reset-password")
    public String processResetPassword(
            @Valid @ModelAttribute("resetConfirmDto") PasswordResetConfirmDto confirmDto,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (!confirmDto.passwordsMatch()) {
            bindingResult.rejectValue("confirmPassword", "error.confirmPassword", "Passwords do not match.");
        }
        if (bindingResult.hasErrors()) {
            return "auth/reset-password";
        }
        try {
            authService.resetPassword(confirmDto);
            redirectAttributes.addFlashAttribute("successMessage", "Your password has been updated! Sign in with your new password.");
            return "redirect:/auth/login";
        } catch (SwapEdException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            return "auth/reset-password";
        }
    }
}
