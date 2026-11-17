package com.swaped.service;

import com.swaped.entity.Listing;
import com.swaped.entity.Student;
import com.swaped.entity.Wishlist;
import com.swaped.exception.ResourceNotFoundException;
import com.swaped.repository.ListingRepository;
import com.swaped.repository.StudentRepository;
import com.swaped.repository.WishlistRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Manages student wishlists, preventing duplicate wishlist entries.
 */
@Service
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final StudentRepository studentRepository;
    private final ListingRepository listingRepository;

    public WishlistService(
            WishlistRepository wishlistRepository,
            StudentRepository studentRepository,
            ListingRepository listingRepository
    ) {
        this.wishlistRepository = wishlistRepository;
        this.studentRepository = studentRepository;
        this.listingRepository = listingRepository;
    }

    @Transactional
    public boolean addToWishlist(Long studentId, Long listingId) {
        if (wishlistRepository.existsByStudentStudentIdAndListingListingId(studentId, listingId)) {
            return false;
        }
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student account not found."));
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new ResourceNotFoundException("Listing #" + listingId + " was not found."));

        Wishlist wishlist = new Wishlist(student, listing);
        wishlistRepository.save(wishlist);
        return true;
    }

    @Transactional
    public void removeFromWishlist(Long studentId, Long listingId) {
        Optional<Wishlist> existing = wishlistRepository.findByStudentStudentIdAndListingListingId(studentId, listingId);
        existing.ifPresent(wishlistRepository::delete);
    }

    @Transactional
    public boolean toggleWishlist(Long studentId, Long listingId) {
        Optional<Wishlist> existing = wishlistRepository.findByStudentStudentIdAndListingListingId(studentId, listingId);
        if (existing.isPresent()) {
            wishlistRepository.delete(existing.get());
            return false;
        }
        addToWishlist(studentId, listingId);
        return true;
    }

    @Transactional(readOnly = true)
    public boolean isInWishlist(Long studentId, Long listingId) {
        if (studentId == null || listingId == null) {
            return false;
        }
        return wishlistRepository.existsByStudentStudentIdAndListingListingId(studentId, listingId);
    }

    @Transactional(readOnly = true)
    public Set<Long> getWishlistedListingIds(Long studentId) {
        if (studentId == null) {
            return Collections.emptySet();
        }
        return wishlistRepository.findListingIdsByStudentId(studentId);
    }

    @Transactional(readOnly = true)
    public List<Wishlist> getStudentWishlist(Long studentId) {
        return wishlistRepository.findByStudentIdWithDetails(studentId);
    }
}
