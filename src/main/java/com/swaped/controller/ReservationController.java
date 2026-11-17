package com.swaped.controller;

import com.swaped.entity.Reservation;
import com.swaped.exception.SwapEdException;
import com.swaped.security.CustomUserDetails;
import com.swaped.service.ReservationService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Handles buyer reservation requests and seller accept/reject actions.
 */
@Controller
@RequestMapping("/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping("/create")
    public String createReservation(
            @RequestParam("listingId") Long listingId,
            @AuthenticationPrincipal CustomUserDetails currentUser,
            RedirectAttributes redirectAttributes
    ) {
        try {
            Reservation reservation = reservationService.requestReservation(listingId, currentUser.getStudentId());
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Pickup reservation request sent to " + reservation.getListing().getSeller().getFullName()
                            + "! Track status in My Reservations."
            );
            return "redirect:/dashboard/reservations";
        } catch (SwapEdException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/listings/" + listingId;
        }
    }

    @PostMapping("/{id:\\d+}/accept")
    public String acceptReservation(
            @PathVariable("id") Long reservationId,
            @AuthenticationPrincipal CustomUserDetails currentUser,
            RedirectAttributes redirectAttributes
    ) {
        try {
            Reservation reservation = reservationService.acceptReservation(reservationId, currentUser.getStudentId());
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Reservation accepted for " + reservation.getBuyer().getFullName()
                            + ". Coordinate pickup at " + reservation.getListing().getPickupLocation()
                            + ", then click 'Confirm Handover' once completed."
            );
        } catch (SwapEdException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/dashboard/requests";
    }

    @PostMapping("/{id:\\d+}/reject")
    public String rejectReservation(
            @PathVariable("id") Long reservationId,
            @AuthenticationPrincipal CustomUserDetails currentUser,
            RedirectAttributes redirectAttributes
    ) {
        try {
            reservationService.rejectReservation(reservationId, currentUser.getStudentId());
            redirectAttributes.addFlashAttribute("successMessage", "Reservation request declined.");
        } catch (SwapEdException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/dashboard/requests";
    }

    @PostMapping("/{id:\\d+}/cancel")
    public String cancelReservation(
            @PathVariable("id") Long reservationId,
            @AuthenticationPrincipal CustomUserDetails currentUser,
            RedirectAttributes redirectAttributes
    ) {
        try {
            reservationService.cancelReservation(reservationId, currentUser.getStudentId());
            redirectAttributes.addFlashAttribute("successMessage", "Your reservation request has been cancelled.");
        } catch (SwapEdException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/dashboard/reservations";
    }
}
