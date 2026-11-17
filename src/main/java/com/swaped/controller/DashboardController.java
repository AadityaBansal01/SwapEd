package com.swaped.controller;

import com.swaped.dto.DashboardStatsDto;
import com.swaped.dto.ProfileUpdateDto;
import com.swaped.entity.Listing;
import com.swaped.entity.Reservation;
import com.swaped.entity.Student;
import com.swaped.entity.Trade;
import com.swaped.entity.Wishlist;
import com.swaped.security.CustomUserDetails;
import com.swaped.service.DashboardService;
import com.swaped.service.ListingService;
import com.swaped.service.ReservationService;
import com.swaped.service.StudentService;
import com.swaped.service.TradeService;
import com.swaped.service.WishlistService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * Renders the student dashboard and all sub-views (Overview, My Listings,
 * My Reservations, Reservation Requests, Completed Trades, Wishlist, My Profile).
 */
@Controller
@RequestMapping("/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;
    private final StudentService studentService;
    private final ListingService listingService;
    private final ReservationService reservationService;
    private final TradeService tradeService;
    private final WishlistService wishlistService;

    public DashboardController(
            DashboardService dashboardService,
            StudentService studentService,
            ListingService listingService,
            ReservationService reservationService,
            TradeService tradeService,
            WishlistService wishlistService
    ) {
        this.dashboardService = dashboardService;
        this.studentService = studentService;
        this.listingService = listingService;
        this.reservationService = reservationService;
        this.tradeService = tradeService;
        this.wishlistService = wishlistService;
    }

    @GetMapping
    public String overview(@AuthenticationPrincipal CustomUserDetails currentUser, Model model) {
        Long studentId = currentUser.getStudentId();
        populateCommonDashboardModel(studentId, "overview", model);

        List<Listing> myListings = listingService.getListingsBySeller(studentId);
        List<Reservation> incomingRequests = reservationService.getSellerIncomingRequests(studentId);
        List<Reservation> myReservations = reservationService.getBuyerReservations(studentId);
        List<Trade> recentTrades = tradeService.getTradesForStudent(studentId);

        model.addAttribute("myListings", myListings.stream().limit(4).toList());
        model.addAttribute("incomingRequests", incomingRequests.stream().limit(5).toList());
        model.addAttribute("myReservations", myReservations.stream().limit(5).toList());
        model.addAttribute("recentTrades", recentTrades.stream().limit(4).toList());
        return "dashboard/index";
    }

    @GetMapping("/listings")
    public String myListings(@AuthenticationPrincipal CustomUserDetails currentUser, Model model) {
        Long studentId = currentUser.getStudentId();
        populateCommonDashboardModel(studentId, "listings", model);
        model.addAttribute("myListings", listingService.getListingsBySeller(studentId));
        return "dashboard/index";
    }

    @GetMapping("/reservations")
    public String myReservations(@AuthenticationPrincipal CustomUserDetails currentUser, Model model) {
        Long studentId = currentUser.getStudentId();
        populateCommonDashboardModel(studentId, "reservations", model);
        model.addAttribute("myReservations", reservationService.getBuyerReservations(studentId));
        return "dashboard/index";
    }

    @GetMapping("/requests")
    public String reservationRequests(@AuthenticationPrincipal CustomUserDetails currentUser, Model model) {
        Long studentId = currentUser.getStudentId();
        populateCommonDashboardModel(studentId, "requests", model);
        model.addAttribute("incomingRequests", reservationService.getSellerIncomingRequests(studentId));
        return "dashboard/index";
    }

    @GetMapping("/trades")
    public String completedTrades(@AuthenticationPrincipal CustomUserDetails currentUser, Model model) {
        Long studentId = currentUser.getStudentId();
        populateCommonDashboardModel(studentId, "trades", model);
        model.addAttribute("trades", tradeService.getTradesForStudent(studentId));
        return "dashboard/index";
    }

    @GetMapping("/wishlist")
    public String wishlistTab(@AuthenticationPrincipal CustomUserDetails currentUser, Model model) {
        Long studentId = currentUser.getStudentId();
        populateCommonDashboardModel(studentId, "wishlist", model);
        List<Wishlist> wishlistItems = wishlistService.getStudentWishlist(studentId);
        model.addAttribute("wishlistItems", wishlistItems);
        return "dashboard/index";
    }

    @GetMapping("/profile")
    public String profileTab(@AuthenticationPrincipal CustomUserDetails currentUser, Model model) {
        Long studentId = currentUser.getStudentId();
        Student student = populateCommonDashboardModel(studentId, "profile", model);
        model.addAttribute(
                "profileDto",
                new ProfileUpdateDto(student.getFullName(), student.getDepartment(), student.getCourse())
        );
        return "dashboard/index";
    }

    private Student populateCommonDashboardModel(Long studentId, String activeTab, Model model) {
        Student student = studentService.getStudentById(studentId);
        DashboardStatsDto stats = dashboardService.getStatsForStudent(studentId);
        model.addAttribute("student", student);
        model.addAttribute("stats", stats);
        model.addAttribute("activeTab", activeTab);
        model.addAttribute("activeNav", "dashboard");
        return student;
    }
}
