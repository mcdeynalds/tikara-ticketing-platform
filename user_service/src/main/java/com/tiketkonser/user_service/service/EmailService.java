package com.tiketkonser.user_service.service;

public interface EmailService {
    void sendOtpEmail(String toEmail, String otpCode);
}
