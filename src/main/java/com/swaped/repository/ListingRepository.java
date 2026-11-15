package com.swaped.repository;

import com.swaped.entity.ItemCondition;
import com.swaped.entity.Listing;
import com.swaped.entity.ListingStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ListingRepository extends JpaRepository<Listing, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT l FROM Listing l WHERE l.listingId = :listingId")
    Optional<Listing> findByIdForUpdate(@Param("listingId") Long listingId);

    List<Listing> findTop8ByListingStatusOrderByCreatedAtDesc(ListingStatus status);

    List<Listing> findBySellerStudentIdOrderByCreatedAtDesc(Long sellerId);

    long countBySellerStudentId(Long sellerId);

    long countBySellerStudentIdAndListingStatus(Long sellerId, ListingStatus status);

    long countByCategoryCategoryIdAndListingStatus(Long categoryId, ListingStatus status);

    @Query("""
        SELECT l FROM Listing l
        JOIN FETCH l.seller s
        JOIN FETCH l.category c
        WHERE (:keyword IS NULL OR :keyword = '' OR
               LOWER(l.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
               LOWER(l.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
               LOWER(l.courseName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
               LOWER(l.courseCode) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
               LOWER(c.categoryName) LIKE LOWER(CONCAT('%', :keyword, '%')))
          AND (:categoryId IS NULL OR c.categoryId = :categoryId)
          AND (:condition IS NULL OR l.itemCondition = :condition)
          AND (:availableOnly = FALSE OR l.listingStatus = com.swaped.entity.ListingStatus.AVAILABLE)
          AND (l.listingStatus <> com.swaped.entity.ListingStatus.INACTIVE)
        """)
    List<Listing> searchMarketplaceListings(
            @Param("keyword") String keyword,
            @Param("categoryId") Long categoryId,
            @Param("condition") ItemCondition condition,
            @Param("availableOnly") boolean availableOnly,
            Sort sort
    );
}
