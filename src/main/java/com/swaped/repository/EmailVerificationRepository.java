package com.swaped.repository;

import com.swaped.entity.EmailVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmailVerificationRepository extends JpaRepository<EmailVerification, Long> {

    @Query("SELECT ev FROM EmailVerification ev JOIN FETCH ev.student WHERE ev.tokenHash = :tokenHash")
    Optional<EmailVerification> findByTokenHashWithStudent(@Param("tokenHash") String tokenHash);

    List<EmailVerification> findByStudentStudentIdAndUsedAtIsNull(Long studentId);
}
