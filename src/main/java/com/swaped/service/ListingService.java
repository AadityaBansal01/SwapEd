package com.swaped.service;

import com.swaped.dto.ListingDto;
import com.swaped.entity.Category;
import com.swaped.entity.ItemCondition;
import com.swaped.entity.Listing;
import com.swaped.entity.ListingStatus;
import com.swaped.entity.Student;
import com.swaped.exception.ResourceNotFoundException;
import com.swaped.exception.SwapEdException;
import com.swaped.exception.UnauthorizedActionException;
import com.swaped.repository.CategoryRepository;
import com.swaped.repository.ListingRepository;
import com.swaped.repository.StudentRepository;
import com.swaped.repository.TradeRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Business logic for creating, updating, searching, filtering, and managing
 * campus marketplace listings and product image uploads.
 */
@Service
public class ListingService {

    private static final long MAX_IMAGE_BYTES = 5L * 1024 * 1024;
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg", "image/jpg", "image/png", "image/webp", "image/gif", "image/svg+xml"
    );

    private final ListingRepository listingRepository;
    private final CategoryRepository categoryRepository;
    private final StudentRepository studentRepository;

    @Value("${swaped.upload.dir:uploads}")
    private String uploadDir;

    public ListingService(
            ListingRepository listingRepository,
            CategoryRepository categoryRepository,
            StudentRepository studentRepository
    ) {
        this.listingRepository = listingRepository;
        this.categoryRepository = categoryRepository;
        this.studentRepository = studentRepository;
    }

    @Transactional(readOnly = true)
    public List<Category> getAllCategories() {
        return categoryRepository.findAllByOrderByCategoryIdAsc();
    }

    @Transactional(readOnly = true)
    public List<Listing> getRecentAvailableListings() {
        return listingRepository.findTop8ByListingStatusOrderByCreatedAtDesc(ListingStatus.AVAILABLE);
    }

    @Transactional(readOnly = true)
    public List<Listing> searchListings(
            String keyword,
            Long categoryId,
            ItemCondition condition,
            String sortBy,
            boolean availableOnly
    ) {
        String trimmedKeyword = (keyword != null && !keyword.isBlank()) ? keyword.trim() : null;
        Sort sort = resolveSort(sortBy);
        return listingRepository.searchMarketplaceListings(trimmedKeyword, categoryId, condition, availableOnly, sort);
    }

    @Transactional(readOnly = true)
    public Listing getListingById(Long listingId) {
        return listingRepository.findById(listingId)
                .orElseThrow(() -> new ResourceNotFoundException("Listing #" + listingId + " was not found."));
    }

    @Transactional(readOnly = true)
    public List<Listing> getListingsBySeller(Long sellerId) {
        return listingRepository.findBySellerStudentIdOrderByCreatedAtDesc(sellerId);
    }

    @Transactional
    public Listing createListing(Long sellerId, ListingDto dto, MultipartFile imageFile) {
        validateListingPrices(dto.getSellingPrice(), dto.getOriginalPrice());

        Student seller = studentRepository.findById(sellerId)
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated seller account not found."));
        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Selected category not found."));

        Listing listing = new Listing();
        listing.setSeller(seller);
        listing.setCategory(category);
        listing.setTitle(sanitizeText(dto.getTitle()));
        listing.setDescription(sanitizeText(dto.getDescription()));
        listing.setCourseName(sanitizeText(dto.getCourseName()));
        listing.setCourseCode(sanitizeText(dto.getCourseCode()).toUpperCase());
        listing.setSellingPrice(dto.getSellingPrice());
        listing.setOriginalPrice(dto.getOriginalPrice());
        listing.setItemCondition(dto.getItemCondition());
        listing.setPickupLocation(sanitizeText(dto.getPickupLocation()));
        listing.setListingStatus(ListingStatus.AVAILABLE);

        if (imageFile != null && !imageFile.isEmpty()) {
            String storedPath = storeProductImage(imageFile);
            listing.setImagePath(storedPath);
        }

        return listingRepository.save(listing);
    }

    @Transactional
    public Listing updateListing(Long listingId, Long sellerId, ListingDto dto, MultipartFile imageFile) {
        Listing listing = getListingById(listingId);
        verifyOwnership(listing, sellerId);

        if (listing.getListingStatus() == ListingStatus.SOLD) {
            throw new SwapEdException("Sold listings are archived as part of trade history and cannot be edited.");
        }

        validateListingPrices(dto.getSellingPrice(), dto.getOriginalPrice());

        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Selected category not found."));

        listing.setCategory(category);
        listing.setTitle(sanitizeText(dto.getTitle()));
        listing.setDescription(sanitizeText(dto.getDescription()));
        listing.setCourseName(sanitizeText(dto.getCourseName()));
        listing.setCourseCode(sanitizeText(dto.getCourseCode()).toUpperCase());
        listing.setSellingPrice(dto.getSellingPrice());
        listing.setOriginalPrice(dto.getOriginalPrice());
        listing.setItemCondition(dto.getItemCondition());
        listing.setPickupLocation(sanitizeText(dto.getPickupLocation()));

        if (imageFile != null && !imageFile.isEmpty()) {
            String storedPath = storeProductImage(imageFile);
            listing.setImagePath(storedPath);
        }

        return listingRepository.save(listing);
    }

    @Transactional
    public Listing toggleListingAvailability(Long listingId, Long sellerId) {
        Listing listing = getListingById(listingId);
        verifyOwnership(listing, sellerId);

        if (listing.getListingStatus() == ListingStatus.SOLD) {
            throw new SwapEdException("This item has already been sold and cannot be re-activated.");
        }
        if (listing.getListingStatus() == ListingStatus.RESERVED) {
            throw new SwapEdException("This item currently has an accepted pickup reservation. Reject or cancel the reservation first.");
        }

        if (listing.getListingStatus() == ListingStatus.AVAILABLE) {
            listing.setListingStatus(ListingStatus.INACTIVE);
        } else {
            listing.setListingStatus(ListingStatus.AVAILABLE);
        }
        return listingRepository.save(listing);
    }

    @Transactional
    public void deleteListing(Long listingId, Long sellerId) {
        Listing listing = getListingById(listingId);
        verifyOwnership(listing, sellerId);

        if (listing.getListingStatus() == ListingStatus.SOLD) {
            throw new SwapEdException("Completed trade items cannot be deleted because they are part of verified trade records.");
        }
        listingRepository.delete(listing);
    }

    public ListingDto toDto(Listing listing) {
        ListingDto dto = new ListingDto();
        dto.setListingId(listing.getListingId());
        dto.setTitle(listing.getTitle());
        dto.setDescription(listing.getDescription());
        dto.setCategoryId(listing.getCategory().getCategoryId());
        dto.setCourseName(listing.getCourseName());
        dto.setCourseCode(listing.getCourseCode());
        dto.setSellingPrice(listing.getSellingPrice());
        dto.setOriginalPrice(listing.getOriginalPrice());
        dto.setItemCondition(listing.getItemCondition());
        dto.setPickupLocation(listing.getPickupLocation());
        dto.setExistingImagePath(listing.getResolvedImagePath());
        return dto;
    }

    private void verifyOwnership(Listing listing, Long sellerId) {
        if (sellerId == null || !listing.getSeller().getStudentId().equals(sellerId)) {
            throw new UnauthorizedActionException("You are only authorized to modify your own marketplace listings.");
        }
    }

    private void validateListingPrices(BigDecimal sellingPrice, BigDecimal originalPrice) {
        if (sellingPrice == null || sellingPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new SwapEdException("Selling price cannot be negative.");
        }
        if (originalPrice != null && originalPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new SwapEdException("Original price cannot be negative.");
        }
    }

    private String storeProductImage(MultipartFile file) {
        if (file.getSize() > MAX_IMAGE_BYTES) {
            throw new SwapEdException("Product image exceeds the maximum file size of 5 MB.");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new SwapEdException("Unsupported image format. Please upload a JPG, PNG, WEBP, GIF, or SVG file.");
        }

        String originalFilename = file.getOriginalFilename();
        String ext = ".jpg";
        if (originalFilename != null && originalFilename.contains(".")) {
            String candidateExt = originalFilename.substring(originalFilename.lastIndexOf('.')).toLowerCase();
            if (candidateExt.matches("^\\.(jpg|jpeg|png|webp|gif|svg)$")) {
                ext = candidateExt;
            }
        }

        try {
            Path targetDir = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(targetDir);
            String filename = "item-" + UUID.randomUUID() + ext;
            Path destination = targetDir.resolve(filename).normalize();
            if (!destination.startsWith(targetDir)) {
                throw new SwapEdException("Invalid upload path.");
            }
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destination, StandardCopyOption.REPLACE_EXISTING);
            }
            return "/uploads/" + filename;
        } catch (IOException ex) {
            throw new SwapEdException("Failed to save uploaded image: " + ex.getMessage(), ex);
        }
    }

    private Sort resolveSort(String sortBy) {
        if ("price_asc".equalsIgnoreCase(sortBy)) {
            return Sort.by(Sort.Direction.ASC, "sellingPrice").and(Sort.by(Sort.Direction.DESC, "createdAt"));
        }
        if ("price_desc".equalsIgnoreCase(sortBy)) {
            return Sort.by(Sort.Direction.DESC, "sellingPrice").and(Sort.by(Sort.Direction.DESC, "createdAt"));
        }
        return Sort.by(Sort.Direction.DESC, "createdAt");
    }

    private String sanitizeText(String input) {
        if (input == null) {
            return "";
        }
        return input.trim().replaceAll("[<>]", "");
    }
}
