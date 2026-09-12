package com.tiketkonser.user_service.payload.req;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
public class VerifyOtpReq {
    @NotBlank(message = "Email wajib diisi")
    @Email(message = "Format email tidak valid")
    private String email;

    @NotBlank(message = "Kode OTP wajib diisi")
    @Size(min = 6, max = 6, message = "Kode OTP harus 6 digit")
    @JsonAlias({"otp", "code", "kodeOtp", "otp_code"})
    private String otpCode;

    public VerifyOtpReq() {}

    public VerifyOtpReq(String email, String otpCode) {
        this.email = email;
        this.otpCode = otpCode;
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getOtpCode() { return otpCode; }
    public void setOtpCode(String otpCode) { this.otpCode = otpCode; }
}
