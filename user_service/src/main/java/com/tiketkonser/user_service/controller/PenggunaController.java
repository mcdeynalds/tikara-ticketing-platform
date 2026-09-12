package com.tiketkonser.user_service.controller;

import com.tiketkonser.user_service.payload.req.ChangePasswordReq;
import com.tiketkonser.user_service.payload.req.LoginReq;
import com.tiketkonser.user_service.payload.req.RefreshTokenReq;
import com.tiketkonser.user_service.payload.req.RegisterPenggunaReq;
import com.tiketkonser.user_service.payload.req.UpdateProfilReq;
import com.tiketkonser.user_service.payload.res.LoginRes;
import com.tiketkonser.user_service.payload.res.PageResponse;
import com.tiketkonser.user_service.payload.res.PenggunaRes;
import com.tiketkonser.user_service.payload.res.ProfilPenggunaRes;
import com.tiketkonser.user_service.service.PenggunaService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Pengguna", description = "Endpoints untuk manajemen data pengguna")
@SecurityRequirement(name = "bearerAuth")
public class PenggunaController {

    private final PenggunaService penggunaService;

    // =========================
    // REGISTER (USER BARU)
    // =========================
    @PostMapping("/register")
    public ResponseEntity<PenggunaRes> register(
            @Valid @RequestBody RegisterPenggunaReq request) {

        PenggunaRes response = penggunaService.register(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // =========================
    // LOGIN
    // =========================
    @PostMapping("/login")
    public ResponseEntity<LoginRes> login(
            @Valid @RequestBody LoginReq request) {

        LoginRes response = penggunaService.login(request);

        return ResponseEntity.ok(response);
    }

    // =========================
    // REFRESH TOKEN
    // =========================
    @PostMapping("/refresh-token")
    public ResponseEntity<LoginRes> refreshToken(
            @Valid @RequestBody RefreshTokenReq request) {

        LoginRes response = penggunaService.refreshToken(request);

        return ResponseEntity.ok(response);
    }

    // =========================
    // LOGOUT
    // =========================
    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout() {
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Berhasil keluar dari sesi akun TIKARA"
        ));
    }

    // =========================
    // GET PROFILE (LOGGED IN USER)
    // =========================
    @GetMapping({"/profile", "/me"})
    public ResponseEntity<ProfilPenggunaRes> getProfile(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Akses ditolak. Silakan login terlebih dahulu.");
        }

        String email = authentication.getName();
        ProfilPenggunaRes response = penggunaService.getProfile(email);

        return ResponseEntity.ok(response);
    }

    // =========================
    // UPDATE PROFILE (LOGGED IN USER)
    // =========================
    @PutMapping({"/profile", "/profile/update"})
    public ResponseEntity<ProfilPenggunaRes> updateProfile(
            @Valid @RequestBody UpdateProfilReq request,
            Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Akses ditolak. Silakan login terlebih dahulu.");
        }

        String email = authentication.getName();
        ProfilPenggunaRes response = penggunaService.updateProfile(email, request);

        return ResponseEntity.ok(response);
    }

    // =========================
    // CHANGE PASSWORD (LOGGED IN USER)
    // =========================
    @PutMapping({"/change-password", "/profile/change-password"})
    public ResponseEntity<Map<String, String>> changePassword(
            @Valid @RequestBody ChangePasswordReq request,
            Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Akses ditolak. Silakan login terlebih dahulu.");
        }

        String email = authentication.getName();
        penggunaService.changePassword(email, request);

        return ResponseEntity.ok(Map.of("message", "Kata sandi berhasil diperbarui"));
    }

    // =========================
    // UPLOAD AVATAR (LOGGED IN USER)
    // =========================
    @PostMapping(value = {"/profile/upload-avatar", "/avatar", "/profile/avatar"}, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProfilPenggunaRes> uploadAvatar(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Akses ditolak. Silakan login terlebih dahulu.");
        }

        String email = authentication.getName();
        ProfilPenggunaRes response = penggunaService.uploadAvatar(email, file);
        return ResponseEntity.ok(response);
    }

    // =========================
    // GET ALL (SEARCH, FILTER, SORT, PAGINATION - ADMIN ONLY)
    // =========================
    @GetMapping
    public ResponseEntity<PageResponse<PenggunaRes>> getAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            Authentication authentication) {

        validateAdmin(authentication);

        PageResponse<PenggunaRes> response = penggunaService.getAll(search, role, page, size, sortBy, sortDir);

        return ResponseEntity.ok(response);
    }

    // =========================
    // GET BY ID (WITH OWNERSHIP / ADMIN CHECK)
    // =========================
    @GetMapping("/{id}")
    public ResponseEntity<PenggunaRes> getById(
            @PathVariable Long id,
            Authentication authentication) {

        validateOwnershipOrAdmin(id, authentication);

        PenggunaRes response = penggunaService.getById(id);

        return ResponseEntity.ok(response);
    }

    // =========================
    // GET BY EMAIL (WITH OWNERSHIP / ADMIN CHECK)
    // =========================
    @GetMapping("/email/{email}")
    public ResponseEntity<PenggunaRes> getByEmail(
            @PathVariable String email,
            Authentication authentication) {

        PenggunaRes targetUser = penggunaService.getByEmail(email);
        validateOwnershipOrAdmin(targetUser.getId(), authentication);

        return ResponseEntity.ok(targetUser);
    }

    // =========================
    // UPDATE BY ID (WITH OWNERSHIP / ADMIN CHECK)
    // =========================
    @PutMapping("/{id}")
    public ResponseEntity<PenggunaRes> update(
            @PathVariable Long id,
            @Valid @RequestBody RegisterPenggunaReq request,
            Authentication authentication) {

        validateOwnershipOrAdmin(id, authentication);

        PenggunaRes response = penggunaService.update(id, request);

        return ResponseEntity.ok(response);
    }

    // =========================
    // DELETE (WITH OWNERSHIP / ADMIN CHECK)
    // =========================
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            Authentication authentication) {

        validateOwnershipOrAdmin(id, authentication);

        penggunaService.delete(id);

        return ResponseEntity.noContent().build();
    }

    // =========================
    // HELPER: VALIDATE ADMIN
    // =========================
    private void validateAdmin(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Akses ditolak. Silakan login terlebih dahulu.");
        }

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin) {
            throw new AccessDeniedException("Hanya ADMIN yang memiliki hak akses untuk fitur ini.");
        }
    }

    // =========================
    // HELPER: VALIDATE OWNERSHIP OR ADMIN
    // =========================
    private void validateOwnershipOrAdmin(Long targetUserId, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Akses ditolak. Silakan login terlebih dahulu.");
        }

        String currentEmail = authentication.getName();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (isAdmin) {
            return; // Admin bebas mengakses data pengguna manapun
        }

        PenggunaRes currentUser = penggunaService.getByEmail(currentEmail);
        if (!currentUser.getId().equals(targetUserId)) {
            throw new AccessDeniedException("Anda tidak memiliki hak akses untuk data pengguna lain.");
        }
    }

    // =========================
    // RESTORE SOFT DELETED USER (ADMIN ONLY)
    // =========================
    @PatchMapping("/{id}/restore")
    public ResponseEntity<PenggunaRes> restore(
            @PathVariable Long id,
            Authentication authentication) {

        validateAdmin(authentication);
        PenggunaRes response = penggunaService.restore(id);
        return ResponseEntity.ok(response);
    }

    // =========================
    // CHECK RECIPIENT FOR TICKET TRANSFER
    // =========================
    @GetMapping("/check-recipient")
    public ResponseEntity<Map<String, Object>> checkRecipient(
            @RequestParam String email,
            Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Akses ditolak. Silakan login terlebih dahulu.");
        }

        String currentEmail = authentication.getName();
        if (currentEmail != null && currentEmail.equalsIgnoreCase(email.trim())) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "BAD_REQUEST",
                    "valid", false,
                    "message", "Anda tidak dapat mentransfer tiket ke akun Anda sendiri."
            ));
        }

        try {
            PenggunaRes targetUser = penggunaService.getByEmail(email.trim());
            return ResponseEntity.ok(Map.of(
                    "status", "SUCCESS",
                    "valid", true,
                    "id", targetUser.getId(),
                    "nama", targetUser.getNama(),
                    "email", targetUser.getEmail()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "status", "NOT_FOUND",
                    "valid", false,
                    "message", "Pengguna dengan email '" + email + "' belum terdaftar di TIKARA. Pastikan penerima sudah membuat akun."
            ));
        }
    }
}
