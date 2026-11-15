package com.swaped.repository;

import com.swaped.entity.PasswordReset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PasswordResetRepository extends JpaRepository<PasswordReset, Long> {

    @Query("SELECT pr FROM PasswordReset pr JOIN FETCH pr.student WHERE pr.tokenHash = :tokenHash")
    Optional<PasswordReset> findByTokenHashWithStudent(@Param("tokenHash") String tokenHash);

    List<PasswordReset> findByStudentStudentIdAndUsedAtIsNull(Long studentId);
}
