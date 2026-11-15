package com.swaped.repository;

import com.swaped.entity.Trade;
import com.swaped.entity.TradeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TradeRepository extends JpaRepository<Trade, Long> {

    @Query("""
        SELECT t FROM Trade t
        JOIN FETCH t.listing l
        JOIN FETCH t.buyer b
        JOIN FETCH t.seller s
        WHERE b.studentId = :studentId OR s.studentId = :studentId
        ORDER BY t.completedAt DESC
        """)
    List<Trade> findStudentTradeHistory(@Param("studentId") Long studentId);

    @Query("""
        SELECT COUNT(t) FROM Trade t
        WHERE (t.buyer.studentId = :studentId OR t.seller.studentId = :studentId)
          AND t.tradeStatus = :status
        """)
    long countStudentTradesByStatus(@Param("studentId") Long studentId, @Param("status") TradeStatus status);
}
