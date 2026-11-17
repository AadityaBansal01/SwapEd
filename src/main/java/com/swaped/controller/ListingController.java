package com.swaped.controller;

import com.swaped.dto.ListingDto;
import com.swaped.entity.ItemCondition;
import com.swaped.entity.Listing;
import com.swaped.entity.ReservationStatus;
import com.swaped.exception.SwapEdException;
import com.swaped.repository.ReservationRepository;
import com.swaped.security.CustomUserDetails;
import com.swaped.service.ListingService;
import com.swaped.service.WishlistService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Handles product details view, listing creation, editing, availability toggling, and deletion.
 */
@Controller
@RequestMapping("/listings")
public class ListingController {

    private final ListingService listingService;
    private final WishlistService wishlistService;
    private final ReservationRepository reservationRepository;

    public ListingController(
            ListingService listingService,
            WishlistService wishlistService,
            ReservationRepository reservationRepository
    ) {
        this.listingService = listingService;
        this.wishlistService = wishlistService;
        this.reservationRepository = reservationRepository;
    }

    @GetMapping("/{id:\\d+}")
    public String viewListingDetails(
            @PathVariable("id") Long listingId,
            @AuthenticationPrincipal CustomUserDetails currentUser,
            Model model
    ) {
        Listing listing = listingService.getListingById(listingId);
        boolean isOwner = currentUser != null && listing.getSeller().getStudentId().equals(currentUser.getStudentId());
        boolean isWishlisted = currentUser != null && wishlistService.isInWishlist(currentUser.getStudentId(), listingId);
        boolean hasActiveReservationByUser = currentUser != null && reservationRepository
                .existsByListingListingIdAndBuyerStudentIdAndReservationStatusIn(
                        listingId,
                        currentUser.getStudentId(),
                        List.of(ReservationStatus.PENDING, ReservationStatus.ACCEPTED)
                );

        model.addAttribute("listing", listing);
        model.addAttribute("isOwner", isOwner);
        model.addAttribute("isWishlisted", isWishlisted);
        model.addAttribute("hasActiveReservationByUser", hasActiveReservationByUser);
        model.addAttribute("activeNav", "marketplace");
        return "listings/detail";
    }

    @GetMapping("/new")
    public String newListingForm(Model model) {
        if (!model.containsAttribute("listingDto")) {
            model.addAttribute("listingDto", new ListingDto());
        }
        model.addAttribute("categories", listingService.getAllCategories());
        model.addAttribute("conditions", ItemCondition.values());
        model.addAttribute("isEdit", false);
        model.addAttribute("activeNav", "sell");
        return "listings/form";
    }

    @PostMapping("/create")
    public String createListing(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @Valid @ModelAttribute("listingDto") ListingDto listingDto,
            BindingResult bindingResult,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", listingService.getAllCategories());
            model.addAttribute("conditions", ItemCondition.values());
            model.addAttribute("isEdit", false);
            model.addAttribute("activeNav", "sell");
            return "listings/form";
        }

        try {
            Listing created = listingService.createListing(currentUser.getStudentId(), listingDto, imageFile);
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Your listing \"" + created.getTitle() + "\" is now live on the Thapar marketplace!"
            );
            return "redirect:/listings/" + created.getListingId();
        } catch (SwapEdException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("categories", listingService.getAllCategories());
            model.addAttribute("conditions", ItemCondition.values());
            model.addAttribute("isEdit", false);
            model.addAttribute("activeNav", "sell");
            return "listings/form";
        }
    }

    @GetMapping("/{id:\\d+}/edit")
    public String editListingForm(
            @PathVariable("id") Long listingId,
            @AuthenticationPrincipal CustomUserDetails currentUser,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        Listing listing = listingService.getListingById(listingId);
        if (!listing.getSeller().getStudentId().equals(currentUser.getStudentId())) {
            redirectAttributes.addFlashAttribute("errorMessage", "You can only edit your own listings.");
            return "redirect:/listings/" + listingId;
        }

        model.addAttribute("listingDto", listingService.toDto(listing));
        model.addAttribute("categories", listingService.getAllCategories());
        model.addAttribute("conditions", ItemCondition.values());
        model.addAttribute("isEdit", true);
        model.addAttribute("activeNav", "dashboard");
        return "listings/form";
    }

    @PostMapping("/{id:\\d+}/update")
    public String updateListing(
            @PathVariable("id") Long listingId,
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @Valid @ModelAttribute("listingDto") ListingDto listingDto,
            BindingResult bindingResult,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", listingService.getAllCategories());
            model.addAttribute("conditions", ItemCondition.values());
            model.addAttribute("isEdit", true);
            return "listings/form";
        }

        try {
            Listing updated = listingService.updateListing(listingId, currentUser.getStudentId(), listingDto, imageFile);
            redirectAttributes.addFlashAttribute("successMessage", "Listing \"" + updated.getTitle() + "\" updated successfully.");
            return "redirect:/listings/" + updated.getListingId();
        } catch (SwapEdException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("categories", listingService.getAllCategories());
            model.addAttribute("conditions", ItemCondition.values());
            model.addAttribute("isEdit", true);
            return "listings/form";
        }
    }

    @PostMapping("/{id:\\d+}/status")
    public String toggleAvailability(
            @PathVariable("id") Long listingId,
            @AuthenticationPrincipal CustomUserDetails currentUser,
            RedirectAttributes redirectAttributes
    ) {
        try {
            Listing updated = listingService.toggleListingAvailability(listingId, currentUser.getStudentId());
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Listing \"" + updated.getTitle() + "\" marked as " + updated.getListingStatus().getDisplayName() + "."
            );
        } catch (SwapEdException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/dashboard/listings";
    }

    @PostMapping("/{id:\\d+}/delete")
    public String deleteListing(
            @PathVariable("id") Long listingId,
            @AuthenticationPrincipal CustomUserDetails currentUser,
            RedirectAttributes redirectAttributes
    ) {
        try {
            listingService.deleteListing(listingId, currentUser.getStudentId());
            redirectAttributes.addFlashAttribute("successMessage", "Listing deleted from your marketplace items.");
        } catch (SwapEdException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/dashboard/listings";
    }
}
