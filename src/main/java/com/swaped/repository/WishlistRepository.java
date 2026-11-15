package com.swaped.repository;

import com.swaped.entity.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, Long> {

    @Query("""
        SELECT w FROM Wishlist w
        JOIN FETCH w.listing l
        JOIN FETCH l.category
        JOIN FETCH l.seller
        WHERE w.student.studentId = :studentId
        ORDER BY w.createdAt DESC
        """)
    List<Wishlist> findByStudentIdWithDetails(@Param("studentId") Long studentId);

    Optional<Wishlist> findByStudentStudentIdAndListingListingId(Long studentId, Long listingId);

    boolean existsByStudentStudentIdAndListingListingId(Long studentId, Long listingId);

    @Query("SELECT w.listing.listingId FROM Wishlist w WHERE w.student.studentId = :studentId")
    Set<Long> findListingIdsByStudentId(@Param("studentId") Long studentId);

    long countByStudentStudentId(Long studentId);
}
