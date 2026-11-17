package com.swaped.controller;

import com.swaped.exception.SwapEdException;
import com.swaped.security.CustomUserDetails;
import com.swaped.service.WishlistService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Handles adding, removing, and toggling saved items on a student's wishlist.
 */
@Controller
@RequestMapping("/wishlist")
public class WishlistController {

    private final WishlistService wishlistService;

    public WishlistController(WishlistService wishlistService) {
        this.wishlistService = wishlistService;
    }

    @GetMapping
    public String viewWishlist() {
        return "redirect:/dashboard/wishlist";
    }

    @PostMapping("/toggle")
    public String toggleWishlist(
            @RequestParam("listingId") Long listingId,
            @AuthenticationPrincipal CustomUserDetails currentUser,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes
    ) {
        try {
            boolean added = wishlistService.toggleWishlist(currentUser.getStudentId(), listingId);
            if (added) {
                redirectAttributes.addFlashAttribute("successMessage", "Item saved to your Wishlist.");
            } else {
                redirectAttributes.addFlashAttribute("successMessage", "Item removed from your Wishlist.");
            }
        } catch (SwapEdException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        String referer = request.getHeader("Referer");
        if (referer != null && !referer.isBlank()) {
            return "redirect:" + referer;
        }
        return "redirect:/listings/" + listingId;
    }

    @PostMapping("/remove")
    public String removeFromWishlist(
            @RequestParam("listingId") Long listingId,
            @AuthenticationPrincipal CustomUserDetails currentUser,
            RedirectAttributes redirectAttributes
    ) {
        wishlistService.removeFromWishlist(currentUser.getStudentId(), listingId);
        redirectAttributes.addFlashAttribute("successMessage", "Item removed from your Wishlist.");
        return "redirect:/dashboard/wishlist";
    }
}
