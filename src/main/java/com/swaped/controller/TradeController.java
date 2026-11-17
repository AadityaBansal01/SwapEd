package com.swaped.controller;

import com.swaped.entity.Trade;
import com.swaped.exception.SwapEdException;
import com.swaped.security.CustomUserDetails;
import com.swaped.service.TradeService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Handles confirming offline campus handover and viewing completed trades.
 */
@Controller
@RequestMapping("/trades")
public class TradeController {

    private final TradeService tradeService;

    public TradeController(TradeService tradeService) {
        this.tradeService = tradeService;
    }

    @PostMapping("/complete/{reservationId:\\d+}")
    public String completeTrade(
            @PathVariable("reservationId") Long reservationId,
            @AuthenticationPrincipal CustomUserDetails currentUser,
            RedirectAttributes redirectAttributes
    ) {
        try {
            Trade trade = tradeService.confirmHandoverAndCompleteTrade(reservationId, currentUser.getStudentId());
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Campus handover confirmed! Trade #" + trade.getTradeId() + " for \"" + trade.getListing().getTitle()
                            + "\" with " + trade.getBuyer().getFullName() + " has been recorded."
            );
            return "redirect:/dashboard/trades";
        } catch (SwapEdException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/dashboard/requests";
        }
    }

    @GetMapping("/history")
    public String viewTradeHistory() {
        return "redirect:/dashboard/trades";
    }
}
