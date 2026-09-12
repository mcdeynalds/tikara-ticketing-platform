package com.tiketkonser.user_service.repository;

import com.tiketkonser.user_service.entity.ResetPasswordOtp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;

@Repository
public interface ResetPasswordOtpRepository extends JpaRepository<ResetPasswordOtp, Long> {

    Optional<ResetPasswordOtp> findFirstByEmailAndOtpCodeAndIsUsedFalseAndExpiredAtAfterOrderByCreatedAtDesc(
            String email, String otpCode, LocalDateTime now);

    List<ResetPasswordOtp> findByEmailAndIsUsedFalse(String email);
}
