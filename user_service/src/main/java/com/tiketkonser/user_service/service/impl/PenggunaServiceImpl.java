package com.tiketkonser.user_service.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tiketkonser.user_service.entity.Pengguna;
import com.tiketkonser.user_service.entity.ProfilPengguna;
import com.tiketkonser.user_service.entity.ResetPasswordOtp;
import com.tiketkonser.user_service.handler.BadRequestException;
import com.tiketkonser.user_service.handler.ConflictException;
import com.tiketkonser.user_service.handler.NotFoundException;
import com.tiketkonser.user_service.payload.req.*;
import com.tiketkonser.user_service.payload.res.*;
import com.tiketkonser.user_service.repository.PenggunaRepository;
import com.tiketkonser.user_service.repository.ProfilPenggunaRepository;
import com.tiketkonser.user_service.repository.ResetPasswordOtpRepository;
import com.tiketkonser.user_service.security.JwtService;
import com.tiketkonser.user_service.service.EmailService;
import com.tiketkonser.user_service.service.PenggunaService;
import com.tiketkonser.user_service.utility.PenggunaMapper;
import com.tiketkonser.user_service.utility.PenggunaSpecification;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PenggunaServiceImpl implements PenggunaService {

    private final PenggunaRepository penggunaRepository;
    private final ProfilPenggunaRepository profilPenggunaRepository;
    private final ResetPasswordOtpRepository otpRepository;
    private final EmailService emailService;
    private final PenggunaMapper penggunaMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    private void validatePasswordPolicy(String password) {
        if (password == null || password.isBlank()) {
            throw new BadRequestException("Kata sandi tidak boleh kosong");
        }

        List<String> errors = new ArrayList<>();

        if (password.length() < 8) {
            errors.add("Minimal 8 karakter");
        }

        if (!Character.isUpperCase(password.charAt(0))) {
            errors.add("Harus diawali dengan huruf kapital");
        }

        if (!password.matches(".*\\d.*")) {
            errors.add("Harus memiliki minimal 1 angka");
        }

        if (!password.matches(".*[^a-zA-Z0-9].*")) {
            errors.add("Harus memiliki minimal 1 simbol");
        }

        if (!errors.isEmpty()) {
            throw new BadRequestException("Password belum memenuhi ketentuan: " + String.join(", ", errors));
        }
    }

    @Override
    @Transactional
    public PenggunaRes register(RegisterPenggunaReq request) {
        validatePasswordPolicy(request.getKataSandi());
        if (penggunaRepository.existsByEmailIgnoreCaseAndDeletedAtIsNull(request.getEmail())) {
            throw new ConflictException("Email sudah terdaftar. Silakan gunakan email lain.");
        }

        if ("ADMIN".equalsIgnoreCase(request.getRole())) {
            throw new BadRequestException("Pendaftaran akun dengan peran ADMIN tidak diizinkan melalui registrasi publik.");
        }

        Pengguna pengguna = penggunaMapper.toEntity(request);
        pengguna.setKataSandiHash(passwordEncoder.encode(request.getKataSandi()));

        Pengguna savedPengguna = penggunaRepository.save(pengguna);

        ProfilPengguna profil = new ProfilPengguna(null, savedPengguna, request.getNomorTelepon(), null, null, null, LocalDateTime.now(), LocalDateTime.now());
        profilPenggunaRepository.save(profil);

        return penggunaMapper.toRes(savedPengguna);
    }

    @Override
    public LoginRes login(LoginReq request) {
        Pengguna pengguna = penggunaRepository.findByEmailIgnoreCaseAndDeletedAtIsNull(request.getEmail())
                .orElseThrow(() -> new NotFoundException("Email belum terdaftar. Silakan daftar terlebih dahulu."));

        if (!passwordEncoder.matches(request.getKataSandi(), pengguna.getKataSandiHash())) {
            throw new BadRequestException("Password yang Anda masukkan salah.");
        }

        String token = jwtService.generateToken(pengguna.getEmail(), pengguna.getPeran(), pengguna.getId());
        String refreshToken = jwtService.generateRefreshToken(pengguna.getEmail(), pengguna.getPeran(), pengguna.getId());

        return new LoginRes(pengguna.getId(), pengguna.getNama(), pengguna.getEmail(), pengguna.getPeran(), token, refreshToken, "Bearer");
    }

    @Override
    public PenggunaRes getById(Long id) {
        return getPenggunaById(id);
    }

    public PenggunaRes getPenggunaById(Long id) {
        Pengguna pengguna = penggunaRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new NotFoundException("Pengguna dengan ID " + id + " tidak ditemukan"));
        return penggunaMapper.toRes(pengguna);
    }

    @Override
    public PenggunaRes getByEmail(String email) {
        Pengguna pengguna = penggunaRepository.findByEmailIgnoreCaseAndDeletedAtIsNull(email)
                .orElseThrow(() -> new NotFoundException("Pengguna dengan email " + email + " tidak ditemukan"));
        return penggunaMapper.toRes(pengguna);
    }

    @Override
    public PageResponse<PenggunaRes> getAll(String search, String role, int page, int size, String sortBy, String sortDir) {
        return getAllPengguna(page, size, sortBy, sortDir, search, role);
    }

    public PageResponse<PenggunaRes> getAllPengguna(int pageNo, int pageSize, String sortBy, String sortDir, String search, String role) {
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);

        Specification<Pengguna> spec = PenggunaSpecification.filterPengguna(search, role);
        Page<Pengguna> penggunaPage = penggunaRepository.findAll(spec, pageable);

        List<PenggunaRes> content = penggunaPage.getContent().stream()
                .map(penggunaMapper::toRes)
                .collect(Collectors.toList());

        return new PageResponse<PenggunaRes>(content, penggunaPage.getNumber(), penggunaPage.getSize(), penggunaPage.getTotalElements(), penggunaPage.getTotalPages(), penggunaPage.isLast());
    }

    @Override
    @Transactional
    public PenggunaRes update(Long id, RegisterPenggunaReq request) {
        Pengguna pengguna = penggunaRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new NotFoundException("Pengguna tidak ditemukan"));

        if (request.getNama() != null) pengguna.setNama(request.getNama());
        if (request.getKataSandi() != null && !request.getKataSandi().isBlank()) {
            pengguna.setKataSandiHash(passwordEncoder.encode(request.getKataSandi()));
        }
        pengguna.setUpdatedAt(LocalDateTime.now());
        return penggunaMapper.toRes(penggunaRepository.save(pengguna));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        deletePengguna(id);
    }

    public void deletePengguna(Long id) {
        Pengguna pengguna = penggunaRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new NotFoundException("Pengguna dengan ID " + id + " tidak ditemukan"));

        pengguna.setDeletedAt(LocalDateTime.now());
        pengguna.setUpdatedAt(LocalDateTime.now());
        penggunaRepository.save(pengguna);
    }

    @Override
    @Transactional
    public PenggunaRes restore(Long id) {
        Pengguna pengguna = penggunaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Pengguna tidak ditemukan"));
        pengguna.setDeletedAt(null);
        pengguna.setUpdatedAt(LocalDateTime.now());
        return penggunaMapper.toRes(penggunaRepository.save(pengguna));
    }

    @Override
    public ProfilPenggunaRes getProfile(String email) {
        return getProfilPengguna(email);
    }

    public ProfilPenggunaRes getProfilPengguna(String email) {
        Pengguna pengguna = penggunaRepository.findByEmailIgnoreCaseAndDeletedAtIsNull(email)
                .orElseThrow(() -> new NotFoundException("Pengguna tidak ditemukan"));

        ProfilPengguna profil = profilPenggunaRepository.findByPenggunaId(pengguna.getId()).orElse(null);

        return new ProfilPenggunaRes(
                profil != null ? profil.getId() : null,
                pengguna.getId(),
                pengguna.getNama(),
                pengguna.getEmail(),
                pengguna.getPeran(),
                profil != null ? profil.getNoTelepon() : null,
                profil != null ? profil.getNoTelepon() : null,
                profil != null ? profil.getTanggalLahir() : null,
                profil != null ? profil.getAlamat() : null,
                profil != null ? profil.getFotoProfil() : null,
                profil != null ? profil.getFotoProfil() : null,
                pengguna.getCreatedAt(),
                profil != null ? profil.getUpdatedAt() : pengguna.getUpdatedAt()
        );
    }

    @Override
    @Transactional
    public ProfilPenggunaRes updateProfile(String email, UpdateProfilReq request) {
        return updateProfil(email, request);
    }

    public ProfilPenggunaRes updateProfil(String email, UpdateProfilReq request) {
        Pengguna pengguna = penggunaRepository.findByEmailIgnoreCaseAndDeletedAtIsNull(email)
                .orElseThrow(() -> new NotFoundException("Pengguna tidak ditemukan"));

        if (request.getNama() != null && !request.getNama().isBlank()) {
            pengguna.setNama(request.getNama());
            pengguna.setUpdatedAt(LocalDateTime.now());
            penggunaRepository.save(pengguna);
        }

        ProfilPengguna profil = profilPenggunaRepository.findByPenggunaId(pengguna.getId())
                .orElseGet(() -> new ProfilPengguna(null, pengguna, null, null, null, null, LocalDateTime.now(), LocalDateTime.now()));

        if (request.getNoTelepon() != null) profil.setNoTelepon(request.getNoTelepon());
        if (request.getTanggalLahir() != null) profil.setTanggalLahir(request.getTanggalLahir());
        if (request.getAlamat() != null) profil.setAlamat(request.getAlamat());
        if (request.getFotoProfil() != null) profil.setFotoProfil(request.getFotoProfil());

        profil.setUpdatedAt(LocalDateTime.now());
        profilPenggunaRepository.save(profil);

        return getProfilPengguna(email);
    }

    @Override
    @Transactional
    public ProfilPenggunaRes uploadAvatar(String email, org.springframework.web.multipart.MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File foto profil tidak boleh kosong");
        }
        if (file.getSize() > 2 * 1024 * 1024) {
            throw new BadRequestException("Ukuran foto maksimal 2 MB");
        }
        String contentType = file.getContentType();
        if (contentType == null || (!contentType.startsWith("image/"))) {
            throw new BadRequestException("Hanya file gambar (JPG, PNG, WebP) yang diperbolehkan");
        }

        try {
            java.nio.file.Path uploadDir = java.nio.file.Paths.get("uploads", "avatars");
            if (!java.nio.file.Files.exists(uploadDir)) {
                java.nio.file.Files.createDirectories(uploadDir);
            }
            String originalFilename = file.getOriginalFilename();
            String extension = ".jpg";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String filename = java.util.UUID.randomUUID().toString() + extension;
            java.nio.file.Path targetPath = uploadDir.resolve(filename);
            java.nio.file.Files.copy(file.getInputStream(), targetPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);

            String fileUrl = "/uploads/avatars/" + filename;
            return uploadAvatar(email, fileUrl);
        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            throw new BadRequestException("Gagal menyimpan foto profil: " + e.getMessage());
        }
    }

    public ProfilPenggunaRes uploadAvatar(String email, String fileUrl) {
        Pengguna pengguna = penggunaRepository.findByEmailIgnoreCaseAndDeletedAtIsNull(email)
                .orElseThrow(() -> new NotFoundException("Pengguna tidak ditemukan"));

        ProfilPengguna profil = profilPenggunaRepository.findByPenggunaId(pengguna.getId())
                .orElseGet(() -> new ProfilPengguna(null, pengguna, null, null, null, fileUrl, LocalDateTime.now(), LocalDateTime.now()));

        profil.setFotoProfil(fileUrl);
        profil.setUpdatedAt(LocalDateTime.now());
        profilPenggunaRepository.save(profil);

        return getProfilPengguna(email);
    }

    @Override
    @Transactional
    public void changePassword(String email, ChangePasswordReq request) {
        Pengguna pengguna = penggunaRepository.findByEmailIgnoreCaseAndDeletedAtIsNull(email)
                .orElseThrow(() -> new NotFoundException("Pengguna tidak ditemukan"));

        if (!passwordEncoder.matches(request.getKataSandiLama(), pengguna.getKataSandiHash())) {
            throw new BadRequestException("Kata sandi lama tidak sesuai");
        }

        if (!request.getKataSandiBaru().equals(request.getKonfirmasiKataSandiBaru())) {
            throw new BadRequestException("Konfirmasi kata sandi baru tidak cocok");
        }

        if (passwordEncoder.matches(request.getKataSandiBaru(), pengguna.getKataSandiHash())) {
            throw new BadRequestException("Kata sandi baru tidak boleh sama dengan kata sandi lama");
        }

        validatePasswordPolicy(request.getKataSandiBaru());
            pengguna.setKataSandiHash(passwordEncoder.encode(request.getKataSandiBaru()));
        pengguna.setUpdatedAt(LocalDateTime.now());
        penggunaRepository.save(pengguna);

        System.out.println(">>> [CHANGE PASSWORD SUCCESS] User " + email + " successfully changed password!");
    }
    @Override
    @Transactional
    public String forgotPassword(ForgotPasswordReq request) {
        Pengguna pengguna = penggunaRepository.findByEmailIgnoreCaseAndDeletedAtIsNull(request.getEmail())
                .orElseThrow(() -> new NotFoundException("Email belum terdaftar. Silakan periksa kembali email Anda."));

        if ("ADMIN".equalsIgnoreCase(pengguna.getPeran())) {
            throw new BadRequestException("Akun Administrator tidak diizinkan melakukan pemulihan kata sandi melalui form publik. Harap hubungi Administrator Sistem Utama atau gunakan akses database internal.");
        }

        List<ResetPasswordOtp> activeOtps = otpRepository.findByEmailAndIsUsedFalse(pengguna.getEmail());
        for (var oldOtp : activeOtps) {
            oldOtp.setIsUsed(true);
        }
        if (!activeOtps.isEmpty()) {
            otpRepository.saveAll(activeOtps);
        }

        String otpCode = String.format("%06d", new java.util.Random().nextInt(900000) + 100000);

        ResetPasswordOtp otp = new ResetPasswordOtp(null, pengguna.getEmail(), otpCode, LocalDateTime.now().plusMinutes(10), false, LocalDateTime.now());
        otpRepository.save(otp);

        emailService.sendOtpEmail(pengguna.getEmail(), otpCode);

        return "Kode OTP 6-digit berhasil dikirim ke " + pengguna.getEmail();
    }

    @Override
    @Transactional
    public String verifyOtp(VerifyOtpReq request) {
        ResetPasswordOtp otp = otpRepository
                .findFirstByEmailAndOtpCodeAndIsUsedFalseAndExpiredAtAfterOrderByCreatedAtDesc(
                        request.getEmail(), request.getOtpCode(), LocalDateTime.now())
                .orElseThrow(() -> new BadRequestException("Kode OTP tidak valid atau telah kadaluarsa. Silakan minta kode baru."));

        return jwtService.generateResetToken(otp.getEmail());
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordReq request) {
        try {
            if (jwtService.isTokenExpired(request.getToken())) {
                throw new BadRequestException("Token reset password sudah kadaluarsa");
            }

            String type = jwtService.extractTokenType(request.getToken());
            if (!"RESET_PASSWORD".equals(type)) {
                throw new BadRequestException("Token reset password tidak valid");
            }

            String email = jwtService.extractEmail(request.getToken());
            Pengguna pengguna = penggunaRepository.findByEmailIgnoreCaseAndDeletedAtIsNull(email)
                    .orElseThrow(() -> new NotFoundException("Pengguna tidak ditemukan"));

            validatePasswordPolicy(request.getKataSandiBaru());
            pengguna.setKataSandiHash(passwordEncoder.encode(request.getKataSandiBaru()));
            pengguna.setUpdatedAt(LocalDateTime.now());
            penggunaRepository.save(pengguna);

            List<ResetPasswordOtp> otps = otpRepository.findByEmailAndIsUsedFalse(email);
            for (var o : otps) {
                o.setIsUsed(true);
            }
            if (!otps.isEmpty()) {
                otpRepository.saveAll(otps);
            }

            System.out.println(">>> [RESET PASSWORD SUCCESS] User " + email + " password updated in DB!");
        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            throw new BadRequestException("Gagal melakukan reset password: " + e.getMessage());
        }
    }

    @Override
    public LoginRes refreshToken(RefreshTokenReq request) {
        try {
            if (jwtService.isTokenExpired(request.getRefreshToken())) {
                throw new BadRequestException("Refresh token sudah kadaluarsa");
            }

            String type = jwtService.extractTokenType(request.getRefreshToken());
            if (!"REFRESH".equals(type)) {
                throw new BadRequestException("Token type harus REFRESH");
            }

            String email = jwtService.extractEmail(request.getRefreshToken());
            Pengguna pengguna = penggunaRepository.findByEmailIgnoreCaseAndDeletedAtIsNull(email)
                    .orElseThrow(() -> new NotFoundException("Pengguna tidak ditemukan"));

            String newAccessToken = jwtService.generateToken(pengguna.getEmail(), pengguna.getPeran(), pengguna.getId());
            String newRefreshToken = jwtService.generateRefreshToken(pengguna.getEmail(), pengguna.getPeran(), pengguna.getId());

            return new LoginRes(pengguna.getId(), pengguna.getNama(), pengguna.getEmail(), pengguna.getPeran(), newAccessToken, newRefreshToken, "Bearer");
        } catch (Exception e) {
            throw new BadRequestException("Refresh token tidak valid: " + e.getMessage());
        }
    }

    @Override
    public void logout(String authHeader) {
        System.out.println(">>> Logout request received: " + authHeader);
    }
}
