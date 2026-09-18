package com.tiketkonser.user_service.controller;

import com.tiketkonser.user_service.payload.req.*;
import com.tiketkonser.user_service.payload.res.LoginRes;
import com.tiketkonser.user_service.payload.res.PenggunaRes;
import com.tiketkonser.user_service.service.PenggunaService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints untuk autentikasi dan pemulihan akun")
public class AuthController {

    private final PenggunaService penggunaService;

    @PostMapping("/register")
    public ResponseEntity<PenggunaRes> register(@Valid @RequestBody RegisterPenggunaReq request) {
        PenggunaRes response = penggunaService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginRes> login(@Valid @RequestBody LoginReq request) {
        LoginRes response = penggunaService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<LoginRes> refreshToken(@Valid @RequestBody RefreshTokenReq request) {
        LoginRes response = penggunaService.refreshToken(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout() {
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Berhasil keluar dari sesi akun TIKARA"
        ));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(@Valid @RequestBody ForgotPasswordReq request) {
        String message = penggunaService.forgotPassword(request);
        return ResponseEntity.ok(Map.of(
                "message", message,
                "status", "success"
        ));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<Map<String, String>> verifyOtp(@Valid @RequestBody VerifyOtpReq request) {
        String resetToken = penggunaService.verifyOtp(request);
        return ResponseEntity.ok(Map.of(
                "message", "Kode OTP valid",
                "resetToken", resetToken,
                "status", "success"
        ));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@Valid @RequestBody ResetPasswordReq request) {
        penggunaService.resetPassword(request);
        return ResponseEntity.ok(Map.of(
                "message", "Kata sandi berhasil diperbarui. Silakan login kembali.",
                "status", "success"
        ));
    }
}
