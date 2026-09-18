package com.tiketkonser.promo_service.service.impl;

import com.tiketkonser.promo_service.entity.KodePromo;
import com.tiketkonser.promo_service.entity.PenggunaanPromo;
import com.tiketkonser.promo_service.handler.BadRequestException;
import com.tiketkonser.promo_service.handler.ConflictException;
import com.tiketkonser.promo_service.handler.NotFoundException;
import com.tiketkonser.promo_service.payload.req.KodePromoReq;
import com.tiketkonser.promo_service.payload.req.UsePromoReq;
import com.tiketkonser.promo_service.payload.req.ValidatePromoReq;
import com.tiketkonser.promo_service.payload.res.KodePromoRes;
import com.tiketkonser.promo_service.payload.res.ValidatePromoRes;
import com.tiketkonser.promo_service.repository.KodePromoRepository;
import com.tiketkonser.promo_service.repository.PenggunaanPromoRepository;
import com.tiketkonser.promo_service.service.PromoService;
import com.tiketkonser.promo_service.utility.PromoMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PromoServiceImpl implements PromoService {

    private final KodePromoRepository kodePromoRepository;
    private final PenggunaanPromoRepository penggunaanPromoRepository;
    private final PromoMapper promoMapper;

    @Override
    @Transactional
    public KodePromoRes createPromo(KodePromoReq request) {
        if (kodePromoRepository.existsByKodeIgnoreCase(request.getKode())) {
            throw new ConflictException("Kode promo sudah ada");
        }

        KodePromo kp = KodePromo.builder()
                .kode(request.getKode().toUpperCase())
                .persenDiskon(request.getPersenDiskon())
                .potonganMaksimal(request.getPotonganMaksimal())
                .minimalTransaksi(request.getMinimalTransaksi())
                .kuotaPromo(request.getKuotaPromo())
                .terpakai(0)
                .mulaiBerlaku(request.getMulaiBerlaku())
                .selesaiBerlaku(request.getSelesaiBerlaku())
                .status("ACTIVE")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        return promoMapper.toKodePromoRes(kodePromoRepository.save(kp));
    }

    @Override
    public List<KodePromoRes> getAllActivePromo() {
        return kodePromoRepository.findByStatusIgnoreCase("ACTIVE").stream()
                .map(promoMapper::toKodePromoRes)
                .collect(Collectors.toList());
    }

    @Override
    public KodePromoRes getById(Long id) {
        KodePromo kp = kodePromoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Kode promo tidak ditemukan"));
        return promoMapper.toKodePromoRes(kp);
    }

    @Override
    public ValidatePromoRes validatePromo(ValidatePromoReq request, Authentication authentication) {
        KodePromo kp = kodePromoRepository.findByKodeIgnoreCase(request.getKode())
                .orElseThrow(() -> new NotFoundException("Kode promo tidak ditemukan"));

        LocalDateTime now = LocalDateTime.now();

        if (!"ACTIVE".equalsIgnoreCase(kp.getStatus())) {
            return buildInvalidRes(request.getKode(), request.getTotalHarga(), "Kode promo sudah tidak aktif");
        }
        if (now.isBefore(kp.getMulaiBerlaku()) || now.isAfter(kp.getSelesaiBerlaku())) {
            return buildInvalidRes(request.getKode(), request.getTotalHarga(),
                    "Kode promo telah kadaluarsa atau belum berlaku");
        }
        if (kp.getTerpakai() >= kp.getKuotaPromo()) {
            return buildInvalidRes(request.getKode(), request.getTotalHarga(), "Kuota promo sudah habis");
        }
        if (request.getTotalHarga().compareTo(kp.getMinimalTransaksi()) < 0) {
            return buildInvalidRes(request.getKode(), request.getTotalHarga(),
                    "Total belanja belum mencapai minimal transaksi Rp " + kp.getMinimalTransaksi());
        }

        if (authentication != null && authentication.isAuthenticated()) {
            Long userId = (Long) authentication.getCredentials();
            if (userId != null && penggunaanPromoRepository.existsByKodePromoIdAndPenggunaId(kp.getId(), userId)) {
                return buildInvalidRes(request.getKode(), request.getTotalHarga(),
                        "Anda sudah pernah menggunakan kode promo ini");
            }
        }

        BigDecimal potonganHitung = request.getTotalHarga().multiply(kp.getPersenDiskon())
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal potonganFinal = potonganHitung.min(kp.getPotonganMaksimal());
        BigDecimal totalAkhir = request.getTotalHarga().subtract(potonganFinal).max(BigDecimal.ZERO);

        return ValidatePromoRes.builder()
                .valid(true)
                .kode(kp.getKode())
                .potongan(potonganFinal)
                .totalHargaAwal(request.getTotalHarga())
                .totalHargaAkhir(totalAkhir)
                .message("Kode promo valid! Anda mendapatkan potongan Rp " + potonganFinal)
                .build();
    }

    @Override
    @Transactional
    public ValidatePromoRes usePromo(UsePromoReq request, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Silakan login terlebih dahulu untuk menggunakan promo");
        }

        Long userId = (Long) authentication.getCredentials();
        if (userId == null)
            userId = 1L;

        ValidatePromoReq valReq = ValidatePromoReq.builder()
                .kode(request.getKode())
                .totalHarga(request.getTotalHarga())
                .build();

        ValidatePromoRes valRes = validatePromo(valReq, authentication);
        if (!valRes.getValid()) {
            throw new BadRequestException(valRes.getMessage());
        }

        KodePromo kp = kodePromoRepository.findByKodeIgnoreCase(request.getKode()).orElseThrow();
        kp.setTerpakai(kp.getTerpakai() + 1);
        kodePromoRepository.save(kp);

        PenggunaanPromo penggunaan = PenggunaanPromo.builder()
                .kodePromo(kp)
                .penggunaId(userId)
                .pemesananId(request.getPemesananId())
                .potonganDidapat(valRes.getPotongan())
                .digunakanAt(LocalDateTime.now())
                .build();

        penggunaanPromoRepository.save(penggunaan);
        return valRes;
    }

    @Override
    public List<KodePromoRes> getAllPromos() {
        return kodePromoRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(promoMapper::toKodePromoRes)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public KodePromoRes togglePromoStatus(Long id) {
        KodePromo kp = kodePromoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Kode promo tidak ditemukan"));
        if ("ACTIVE".equalsIgnoreCase(kp.getStatus())) {
            kp.setStatus("INACTIVE");
        } else {
            kp.setStatus("ACTIVE");
        }
        kp.setUpdatedAt(LocalDateTime.now());
        return promoMapper.toKodePromoRes(kodePromoRepository.save(kp));
    }

    @Override
    @Transactional
    public KodePromoRes deactivatePromo(Long id) {
        KodePromo kp = kodePromoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Kode promo tidak ditemukan"));
        kp.setStatus("INACTIVE");
        kp.setUpdatedAt(LocalDateTime.now());
        return promoMapper.toKodePromoRes(kodePromoRepository.save(kp));
    }

    @Override
    public Map<String, Object> getPromoStats() {
        long totalPromos = kodePromoRepository.count();
        long activePromos = kodePromoRepository.countByStatusIgnoreCase("ACTIVE");
        long inactivePromos = kodePromoRepository.countByStatusIgnoreCase("INACTIVE");
        Integer totalTerpakai = kodePromoRepository.sumTotalTerpakai();
        BigDecimal totalHemat = penggunaanPromoRepository.sumTotalPotongan();

        return Map.of(
                "totalPromos", totalPromos,
                "activePromos", activePromos,
                "inactivePromos", inactivePromos,
                "totalTerpakai", totalTerpakai != null ? totalTerpakai : 0,
                "totalPenghematan", totalHemat != null ? totalHemat : BigDecimal.ZERO);
    }

    private ValidatePromoRes buildInvalidRes(String kode, BigDecimal totalAwal, String msg) {
        return ValidatePromoRes.builder()
                .valid(false)
                .kode(kode)
                .potongan(BigDecimal.ZERO)
                .totalHargaAwal(totalAwal)
                .totalHargaAkhir(totalAwal)
                .message(msg)
                .build();
    }

    @Override
    @Transactional
    public Map<String, Object> cancelPromoUsage(Long pemesananId, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Silakan login terlebih dahulu untuk membatalkan penggunaan promo");
        }

        Long userId = (Long) authentication.getCredentials();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        Optional<PenggunaanPromo> opt = penggunaanPromoRepository.findByPemesananId(pemesananId);
        if (opt.isEmpty()) {
            return Map.of(
                    "status", "SUCCESS",
                    "message", "Tidak ada riwayat penggunaan promo untuk pemesanan ID " + pemesananId);
        }

        PenggunaanPromo penggunaan = opt.get();
        if (!isAdmin && userId != null && !userId.equals(penggunaan.getPenggunaId())) {
            throw new AccessDeniedException("Anda tidak memiliki hak akses untuk membatalkan promo pesanan ini");
        }

        KodePromo kp = penggunaan.getKodePromo();
        if (kp != null) {
            kp.setTerpakai(Math.max(0, kp.getTerpakai() - 1));
            kp.setUpdatedAt(LocalDateTime.now());
            kodePromoRepository.save(kp);
        }

        penggunaanPromoRepository.delete(penggunaan);
        log.info("Kompensasi: Penggunaan promo {} untuk pemesanan ID {} berhasil dibatalkan dan kuota dikembalikan",
                kp != null ? kp.getKode() : "-", pemesananId);

        return Map.of(
                "status", "SUCCESS",
                "message", "Penggunaan promo berhasil dibatalkan dan kuota telah dikembalikan",
                "pemesananId", pemesananId,
                "kodePromo", kp != null ? kp.getKode() : "");
    }

}
