package com.swaped.controller;

import com.swaped.dto.ProfileUpdateDto;
import com.swaped.exception.SwapEdException;
import com.swaped.security.CustomUserDetails;
import com.swaped.service.StudentService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Handles student profile updates and account password changes.
 */
@Controller
@RequestMapping("/student")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @PostMapping("/profile/update")
    public String updateProfile(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @Valid @ModelAttribute("profileDto") ProfileUpdateDto profileDto,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please fill in all profile fields accurately.");
            return "redirect:/dashboard/profile";
        }

        try {
            studentService.updateProfile(currentUser.getStudentId(), profileDto);
            redirectAttributes.addFlashAttribute("successMessage", "Your student profile has been updated.");
        } catch (SwapEdException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/dashboard/profile";
    }

    @PostMapping("/password/change")
    public String changePassword(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @RequestParam("currentPassword") String currentPassword,
            @RequestParam("newPassword") String newPassword,
            @RequestParam("confirmPassword") String confirmPassword,
            RedirectAttributes redirectAttributes
    ) {
        try {
            studentService.changePassword(currentUser.getStudentId(), currentPassword, newPassword, confirmPassword);
            redirectAttributes.addFlashAttribute("successMessage", "Your password has been changed successfully.");
        } catch (SwapEdException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/dashboard/profile";
    }
}
