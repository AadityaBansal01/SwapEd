package com.swaped.controller;

import com.swaped.entity.Category;
import com.swaped.entity.ItemCondition;
import com.swaped.entity.Listing;
import com.swaped.security.CustomUserDetails;
import com.swaped.service.ListingService;
import com.swaped.service.WishlistService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Set;

/**
 * Handles landing page, marketplace search/filter page, category directory, and static info pages.
 */
@Controller
public class HomeController {

    private final ListingService listingService;
    private final WishlistService wishlistService;

    public HomeController(ListingService listingService, WishlistService wishlistService) {
        this.listingService = listingService;
        this.wishlistService = wishlistService;
    }

    @GetMapping("/")
    public String homePage(@AuthenticationPrincipal CustomUserDetails currentUser, Model model) {
        List<Category> categories = listingService.getAllCategories();
        List<Listing> recentListings = listingService.getRecentAvailableListings();
        Set<Long> wishlistedIds = currentUser != null
                ? wishlistService.getWishlistedListingIds(currentUser.getStudentId())
                : Set.of();

        model.addAttribute("categories", categories);
        model.addAttribute("recentListings", recentListings);
        model.addAttribute("wishlistedIds", wishlistedIds);
        model.addAttribute("activeNav", "home");
        return "index";
    }

    @GetMapping("/marketplace")
    public String marketplacePage(
            @RequestParam(name = "q", required = false) String keyword,
            @RequestParam(name = "categoryId", required = false) Long categoryId,
            @RequestParam(name = "condition", required = false) String conditionStr,
            @RequestParam(name = "sort", defaultValue = "newest") String sort,
            @RequestParam(name = "availableOnly", defaultValue = "false") boolean availableOnly,
            @AuthenticationPrincipal CustomUserDetails currentUser,
            Model model
    ) {
        ItemCondition selectedCondition = null;
        if (conditionStr != null && !conditionStr.isBlank()) {
            try {
                selectedCondition = ItemCondition.valueOf(conditionStr.trim().toUpperCase());
            } catch (IllegalArgumentException ignored) {
                selectedCondition = null;
            }
        }

        List<Category> categories = listingService.getAllCategories();
        List<Listing> listings = listingService.searchListings(keyword, categoryId, selectedCondition, sort, availableOnly);
        Set<Long> wishlistedIds = currentUser != null
                ? wishlistService.getWishlistedListingIds(currentUser.getStudentId())
                : Set.of();

        model.addAttribute("categories", categories);
        model.addAttribute("conditions", ItemCondition.values());
        model.addAttribute("listings", listings);
        model.addAttribute("resultsCount", listings.size());
        model.addAttribute("keyword", keyword != null ? keyword : "");
        model.addAttribute("selectedCategoryId", categoryId);
        model.addAttribute("selectedCondition", selectedCondition != null ? selectedCondition.name() : "");
        model.addAttribute("selectedSort", sort);
        model.addAttribute("availableOnly", availableOnly);
        model.addAttribute("wishlistedIds", wishlistedIds);
        model.addAttribute("activeNav", "marketplace");
        return "marketplace/list";
    }

    @GetMapping("/categories")
    public String categoriesRedirect() {
        return "redirect:/#categories";
    }

    @GetMapping("/how-it-works")
    public String howItWorksRedirect() {
        return "redirect:/#how-it-works";
    }

    @GetMapping("/about")
    public String aboutPage(Model model) {
        model.addAttribute("pageSection", "about");
        model.addAttribute("pageTitle", "About SwapEd");
        return "info/info-page";
    }

    @GetMapping("/contact")
    public String contactPage(Model model) {
        model.addAttribute("pageSection", "contact");
        model.addAttribute("pageTitle", "Contact Campus Support");
        return "info/info-page";
    }

    @GetMapping("/help")
    public String helpPage(Model model) {
        model.addAttribute("pageSection", "help");
        model.addAttribute("pageTitle", "Help & Campus Exchange Guide");
        return "info/info-page";
    }

    @GetMapping("/privacy")
    public String privacyPage(Model model) {
        model.addAttribute("pageSection", "privacy");
        model.addAttribute("pageTitle", "Privacy Policy");
        return "info/info-page";
    }

    @GetMapping("/terms")
    public String termsPage(Model model) {
        model.addAttribute("pageSection", "terms");
        model.addAttribute("pageTitle", "Terms and Conditions");
        return "info/info-page";
    }
}
