package com.tiketkonser.user_service.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reset_password_otp")
@Data
public class ResetPasswordOtp {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "email", nullable = false)
    private String email;
    @Column(name = "otp_code", nullable = false, length = 6)
    private String otpCode;
    @Column(name = "expired_at", nullable = false)
    private LocalDateTime expiredAt;
    @Column(name = "is_used", nullable = false)
    private Boolean isUsed;
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public ResetPasswordOtp() {
    }

    public ResetPasswordOtp(Long id, String email, String otpCode, LocalDateTime expiredAt, Boolean isUsed,
            LocalDateTime createdAt) {
        this.id = id;
        this.email = email;
        this.otpCode = otpCode;
        this.expiredAt = expiredAt;
        this.isUsed = isUsed;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getOtpCode() {
        return otpCode;
    }

    public void setOtpCode(String otpCode) {
        this.otpCode = otpCode;
    }

    public LocalDateTime getExpiredAt() {
        return expiredAt;
    }

    public void setExpiredAt(LocalDateTime expiredAt) {
        this.expiredAt = expiredAt;
    }

    public Boolean getIsUsed() {
        return isUsed;
    }

    public void setIsUsed(Boolean isUsed) {
        this.isUsed = isUsed;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
