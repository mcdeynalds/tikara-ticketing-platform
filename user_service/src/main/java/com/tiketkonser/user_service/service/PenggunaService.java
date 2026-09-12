package com.tiketkonser.user_service.service;

import com.tiketkonser.user_service.payload.req.*;
import com.tiketkonser.user_service.payload.res.*;

public interface PenggunaService {

    PenggunaRes register(RegisterPenggunaReq request);

    LoginRes login(LoginReq request);

    PenggunaRes getById(Long id);

    PenggunaRes getByEmail(String email);

    PageResponse<PenggunaRes> getAll(String search, String role, int page, int size, String sortBy, String sortDir);

    PenggunaRes update(Long id, RegisterPenggunaReq request);

    void delete(Long id);
    PenggunaRes restore(Long id);
    ProfilPenggunaRes uploadAvatar(String email, org.springframework.web.multipart.MultipartFile file);

    // GAP FEATURES
    ProfilPenggunaRes getProfile(String email);

    ProfilPenggunaRes updateProfile(String email, UpdateProfilReq request);

    void changePassword(String email, ChangePasswordReq request);

    void logout(String authHeader);

    String forgotPassword(ForgotPasswordReq request);

    String verifyOtp(com.tiketkonser.user_service.payload.req.VerifyOtpReq request);

    void resetPassword(ResetPasswordReq request);

    LoginRes refreshToken(RefreshTokenReq request);
}
