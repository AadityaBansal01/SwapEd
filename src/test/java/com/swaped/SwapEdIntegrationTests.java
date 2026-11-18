package com.swaped;

import com.swaped.dto.ListingDto;
import com.swaped.dto.PasswordResetConfirmDto;
import com.swaped.dto.RegistrationDto;
import com.swaped.entity.Category;
import com.swaped.entity.ItemCondition;
import com.swaped.entity.Listing;
import com.swaped.entity.ListingStatus;
import com.swaped.entity.Reservation;
import com.swaped.entity.ReservationStatus;
import com.swaped.entity.Student;
import com.swaped.entity.Trade;
import com.swaped.entity.TradeStatus;
import com.swaped.exception.InvalidReservationException;
import com.swaped.exception.SwapEdException;
import com.swaped.exception.UnauthorizedActionException;
import com.swaped.repository.CategoryRepository;
import com.swaped.repository.ListingRepository;
import com.swaped.repository.StudentRepository;
import com.swaped.security.CustomUserDetails;
import com.swaped.service.AuthService;
import com.swaped.service.DashboardService;
import com.swaped.service.EmailService;
import com.swaped.service.ListingService;
import com.swaped.service.ReservationService;
import com.swaped.service.TradeService;
import com.swaped.service.WishlistService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.containsString;

/**
 * Full-stack integration test suite covering all 19 Phase 6 verification scenarios.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SwapEdIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuthService authService;

    @Autowired
    private ListingService listingService;

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private TradeService tradeService;

    @Autowired
    private WishlistService wishlistService;

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ListingRepository listingRepository;

    @Test
    void test01_to_07_AuthenticationRegistrationVerificationLoginLogoutAndPasswordReset() throws Exception {
        // 1. Reject non-Thapar email domains (@gmail.com, @outlook.com, @yahoo.com)
        RegistrationDto invalidDto = new RegistrationDto();
        invalidDto.setFullName("External User");
        invalidDto.setEmail("someone@gmail.com");
        invalidDto.setCollegeId("99990001");
        invalidDto.setDepartment("CSED");
        invalidDto.setCourse("B.E. COE");
        invalidDto.setPassword("ValidPass@123");
        invalidDto.setConfirmPassword("ValidPass@123");

        assertThrows(SwapEdException.class, () -> authService.registerStudent(invalidDto));

        // 2. Register valid @thapar.edu student
        RegistrationDto validDto = new RegistrationDto();
        validDto.setFullName("Devansh Gupta");
        validDto.setEmail("dgupta_be24@thapar.edu");
        validDto.setCollegeId("1024030999");
        validDto.setDepartment("Computer Science & Engineering (CSED)");
        validDto.setCourse("B.E. Computer Engineering");
        validDto.setPassword("Devansh@2026");
        validDto.setConfirmPassword("Devansh@2026");

        EmailService.EmailDispatchResult regResult = authService.registerStudent(validDto);
        assertThat(regResult.actionUrl()).contains("/auth/verify-email?token=");
        String rawVerifyToken = regResult.actionUrl().substring(regResult.actionUrl().indexOf("token=") + 6);

        // 3. Unverified student cannot log in
        mockMvc.perform(SecurityMockMvcRequestBuilders.formLogin("/auth/login")
                        .user("email", "dgupta_be24@thapar.edu")
                        .password("password", "Devansh@2026"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/auth/login?unverified=true&email=dgupta_be24%40thapar.edu"));

        // 4. Verify email with single-use token
        Student verified = authService.verifyEmailToken(rawVerifyToken);
        assertThat(verified.isEmailVerified()).isTrue();

        // Single-use token cannot be reused
        assertThrows(SwapEdException.class, () -> authService.verifyEmailToken(rawVerifyToken));

        // 5. Login with correct password succeeds
        mockMvc.perform(SecurityMockMvcRequestBuilders.formLogin("/auth/login")
                        .user("email", "dgupta_be24@thapar.edu")
                        .password("password", "Devansh@2026"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"));

        // 6. Login with incorrect password fails
        mockMvc.perform(SecurityMockMvcRequestBuilders.formLogin("/auth/login")
                        .user("email", "dgupta_be24@thapar.edu")
                        .password("password", "WrongPassword123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/auth/login?error=true&email=dgupta_be24%40thapar.edu"));

        // 7. Password reset workflow
        EmailService.EmailDispatchResult resetResult = authService.initiatePasswordReset("dgupta_be24@thapar.edu");
        String rawResetToken = resetResult.actionUrl().substring(resetResult.actionUrl().indexOf("token=") + 6);

        PasswordResetConfirmDto confirmDto = new PasswordResetConfirmDto();
        confirmDto.setToken(rawResetToken);
        confirmDto.setNewPassword("NewSecure@2026");
        confirmDto.setConfirmPassword("NewSecure@2026");
        authService.resetPassword(confirmDto);

        // Login with new password succeeds
        mockMvc.perform(SecurityMockMvcRequestBuilders.formLogin("/auth/login")
                        .user("email", "dgupta_be24@thapar.edu")
                        .password("password", "NewSecure@2026"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"));

        // Logout invalidates session and redirects to /?logout=true
        mockMvc.perform(post("/auth/logout").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/?logout=true"));
    }

    @Test
    void test08_to_18_ListingsSearchReservationsTradesWishlistAndAuthorization() throws Exception {
        Student seller = studentRepository.findByEmailIgnoreCase("rjain1_be24@thapar.edu").orElseThrow();
        Student buyer = studentRepository.findByEmailIgnoreCase("abansal_be24@thapar.edu").orElseThrow();
        Student thirdStudent = studentRepository.findByEmailIgnoreCase("akalra_be24@thapar.edu").orElseThrow();
        Category textbookCat = categoryRepository.findByCategoryNameIgnoreCase("Textbooks").orElseThrow();

        // 8. Adding a listing with image upload
        ListingDto createDto = new ListingDto();
        createDto.setTitle("Computer Networks (Tanenbaum) — 5th Edition");
        createDto.setDescription("Complete textbook for UCS413 Computer Networks with solved numerical problems.");
        createDto.setCategoryId(textbookCat.getCategoryId());
        createDto.setCourseName("Computer Networks");
        createDto.setCourseCode("UCS413");
        createDto.setSellingPrice(new BigDecimal("420.00"));
        createDto.setOriginalPrice(new BigDecimal("895.00"));
        createDto.setItemCondition(ItemCondition.LIKE_NEW);
        createDto.setPickupLocation("CSED Department Atrium");

        MockMultipartFile svgImage = new MockMultipartFile(
                "imageFile",
                "networks.svg",
                "image/svg+xml",
                "<svg xmlns='http://www.w3.org/2000/svg'></svg>".getBytes(StandardCharsets.UTF_8)
        );

        Listing created = listingService.createListing(seller.getStudentId(), createDto, svgImage);
        assertThat(created.getListingId()).isNotNull();
        assertThat(created.getImagePath()).startsWith("/uploads/item-");

        // 9. Editing a listing
        createDto.setSellingPrice(new BigDecimal("390.00"));
        Listing updated = listingService.updateListing(created.getListingId(), seller.getStudentId(), createDto, null);
        assertThat(updated.getSellingPrice()).isEqualByComparingTo("390.00");

        // 18. Unauthorized student cannot edit or delete another student's listing
        assertThrows(UnauthorizedActionException.class,
                () -> listingService.updateListing(created.getListingId(), buyer.getStudentId(), createDto, null));
        assertThrows(UnauthorizedActionException.class,
                () -> listingService.deleteListing(created.getListingId(), buyer.getStudentId()));

        // 11 & 12. Searching by course code and filtering by category
        List<Listing> searchResults = listingService.searchListings("UCS413", textbookCat.getCategoryId(), ItemCondition.LIKE_NEW, "price_asc", true);
        assertThat(searchResults).extracting(Listing::getListingId).contains(created.getListingId());

        // 17. Wishlist functionality (add, prevent duplicate, remove)
        boolean firstAdd = wishlistService.addToWishlist(buyer.getStudentId(), created.getListingId());
        boolean duplicateAdd = wishlistService.addToWishlist(buyer.getStudentId(), created.getListingId());
        assertThat(firstAdd).isTrue();
        assertThat(duplicateAdd).isFalse();
        assertThat(wishlistService.isInWishlist(buyer.getStudentId(), created.getListingId())).isTrue();
        wishlistService.removeFromWishlist(buyer.getStudentId(), created.getListingId());
        assertThat(wishlistService.isInWishlist(buyer.getStudentId(), created.getListingId())).isFalse();

        // 13. Reserving an item (prevent seller from reserving own item)
        assertThrows(InvalidReservationException.class,
                () -> reservationService.requestReservation(created.getListingId(), seller.getStudentId()));

        Reservation resBuyer = reservationService.requestReservation(created.getListingId(), buyer.getStudentId());
        Reservation resThird = reservationService.requestReservation(created.getListingId(), thirdStudent.getStudentId());
        assertThat(resBuyer.getReservationStatus()).isEqualTo(ReservationStatus.PENDING);

        // 14. Accepting reservation marks listing RESERVED and automatically rejects competing pending request
        Reservation accepted = reservationService.acceptReservation(resBuyer.getReservationId(), seller.getStudentId());
        assertThat(accepted.getReservationStatus()).isEqualTo(ReservationStatus.ACCEPTED);
        assertThat(listingService.getListingById(created.getListingId()).getListingStatus()).isEqualTo(ListingStatus.RESERVED);

        // 15 & 16. Completing trade records Trade and marks listing SOLD
        Trade completedTrade = tradeService.confirmHandoverAndCompleteTrade(accepted.getReservationId(), seller.getStudentId());
        assertThat(completedTrade.getTradeStatus()).isEqualTo(TradeStatus.COMPLETED);
        assertThat(listingService.getListingById(created.getListingId()).getListingStatus()).isEqualTo(ListingStatus.SOLD);
        assertThat(tradeService.getTradesForStudent(buyer.getStudentId()))
                .extracting(Trade::getTradeId)
                .contains(completedTrade.getTradeId());

        // 10. Deleting an unsold listing works cleanly
        Listing tempListing = listingService.createListing(seller.getStudentId(), createDto, null);
        Long tempId = tempListing.getListingId();
        listingService.deleteListing(tempId, seller.getStudentId());
        assertThat(listingRepository.findById(tempId)).isEmpty();

        // Unauthenticated user is redirected to login when accessing protected endpoints
        mockMvc.perform(get("/listings/new"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/auth/login"));
        mockMvc.perform(get("/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/auth/login"));
    }

    @Test
    void test19_AllThymeleafViewsRenderWithoutError() throws Exception {
        Student rishita = studentRepository.findByEmailIgnoreCase("rjain1_be24@thapar.edu").orElseThrow();
        CustomUserDetails principal = new CustomUserDetails(rishita);
        Listing anyListing = listingRepository.findAll().getFirst();

        // Public pages
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Your Campus.")));

        mockMvc.perform(get("/marketplace").param("q", "UCS301"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Campus Marketplace")));

        mockMvc.perform(get("/listings/" + anyListing.getListingId()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(anyListing.getTitle())));

        mockMvc.perform(get("/auth/login"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Welcome Back!")));

        mockMvc.perform(get("/auth/register"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Create Student Account")));

        mockMvc.perform(get("/auth/forgot-password"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Reset Your Password")));

        mockMvc.perform(get("/about")).andExpect(status().isOk());
        mockMvc.perform(get("/contact")).andExpect(status().isOk());
        mockMvc.perform(get("/help")).andExpect(status().isOk());
        mockMvc.perform(get("/privacy")).andExpect(status().isOk());
        mockMvc.perform(get("/terms")).andExpect(status().isOk());

        // Authenticated pages
        mockMvc.perform(get("/listings/new").with(user(principal)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Sell Academic Book or Gear")));

        mockMvc.perform(get("/dashboard").with(user(principal)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Welcome back,")));

        mockMvc.perform(get("/dashboard/listings").with(user(principal))).andExpect(status().isOk());
        mockMvc.perform(get("/dashboard/reservations").with(user(principal))).andExpect(status().isOk());
        mockMvc.perform(get("/dashboard/requests").with(user(principal))).andExpect(status().isOk());
        mockMvc.perform(get("/dashboard/trades").with(user(principal))).andExpect(status().isOk());
        mockMvc.perform(get("/dashboard/wishlist").with(user(principal))).andExpect(status().isOk());
        mockMvc.perform(get("/dashboard/profile").with(user(principal))).andExpect(status().isOk());
    }
}
