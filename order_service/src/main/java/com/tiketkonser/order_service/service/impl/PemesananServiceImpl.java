package com.tiketkonser.order_service.service.impl;

import com.tiketkonser.order_service.entity.DetailPemesanan;
import com.tiketkonser.order_service.entity.Pemesanan;
import com.tiketkonser.order_service.handler.BadRequestException;
import com.tiketkonser.order_service.handler.NotFoundException;
import com.tiketkonser.order_service.payload.req.DetailPemesananItemReq;
import com.tiketkonser.order_service.payload.req.PemesananReq;
import com.tiketkonser.order_service.payload.res.KategoriTiketClientRes;
import com.tiketkonser.order_service.payload.res.PageResponse;
import com.tiketkonser.order_service.payload.res.PemesananRes;
import com.tiketkonser.order_service.repository.PemesananRepository;
import com.tiketkonser.order_service.service.PemesananService;
import com.tiketkonser.order_service.utility.OrderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PemesananServiceImpl implements PemesananService {

    private static final String TICKET_SERVICE_URL = "http://localhost:8083";
    private static final String PROMO_SERVICE_URL = "http://localhost:8088";
    private static final String NOTIFICATION_SERVICE_URL = "http://localhost:8086";

    private final PemesananRepository pemesananRepository;
    private final OrderMapper orderMapper;
    private final RestTemplate restTemplate;

    @Override
    @Transactional
    public PemesananRes createOrder(PemesananReq request, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Silakan login terlebih dahulu");
        }

        Long userId = (Long) authentication.getCredentials();
        if (userId == null) userId = 1L;

        String nomorPesanan = "ORD-" + System.currentTimeMillis() / 1000 + "-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();

        BigDecimal totalHarga = BigDecimal.ZERO;
        List<DetailPemesanan> items = new ArrayList<>();

        Pemesanan pemesanan = Pemesanan.builder()
                .penggunaId(userId)
                .nomorPesanan(nomorPesanan)
                .totalHarga(BigDecimal.ZERO)
                .status("PENDING")
                .tenggatPembayaran(LocalDateTime.now().plusMinutes(15))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        for (DetailPemesananItemReq itemReq : request.getItems()) {
            // 1. Ambil data kategori tiket dari ticket_service
            String catUrl = TICKET_SERVICE_URL + "/api/kategori-tiket/" + itemReq.getKategoriTiketId();
            KategoriTiketClientRes cat;
            try {
                cat = restTemplate.getForObject(catUrl, KategoriTiketClientRes.class);
            } catch (Exception e) {
                log.error("Gagal menghubungi ticket_service untuk kategori ID {}: {}", itemReq.getKategoriTiketId(), e.getMessage());
                throw new BadRequestException("Kategori tiket ID " + itemReq.getKategoriTiketId() + " tidak valid atau tidak ditemukan");
            }

            if (cat == null) {
                throw new BadRequestException("Kategori tiket ID " + itemReq.getKategoriTiketId() + " tidak ditemukan");
            }

            // 2. Validasi status ketersediaan tiket
            if (cat.getStatus() != null && !"AVAILABLE".equalsIgnoreCase(cat.getStatus())) {
                throw new BadRequestException("Tiket kategori '" + cat.getNamaKategori() + "' sedang tidak tersedia (" + cat.getStatus() + ")");
            }

            // 3. Validasi periode jadwal penjualan tiket (mulaiJual & selesaiJual)
            LocalDateTime now = LocalDateTime.now();
            if (cat.getMulaiJual() != null && now.isBefore(cat.getMulaiJual())) {
                throw new BadRequestException("Penjualan tiket untuk kategori '" + cat.getNamaKategori() + "' belum dibuka");
            }
            if (cat.getSelesaiJual() != null && now.isAfter(cat.getSelesaiJual())) {
                throw new BadRequestException("Penjualan tiket untuk kategori '" + cat.getNamaKategori() + "' sudah ditutup");
            }

            // 4. Hitung harga dan subtotal sepenuhnya di server
            BigDecimal hargaResmi = cat.getHarga();
            if (hargaResmi == null || hargaResmi.compareTo(BigDecimal.ZERO) < 0) {
                throw new BadRequestException("Harga resmi tiket kategori '" + cat.getNamaKategori() + "' tidak valid");
            }

            BigDecimal subtotal = hargaResmi.multiply(BigDecimal.valueOf(itemReq.getJumlah()));
            totalHarga = totalHarga.add(subtotal);

            // 5. Reservasi stok sementara (hold) di ticket_service
            Map<String, Object> holdPayload = Map.of(
                    "kategoriTiketId", itemReq.getKategoriTiketId(),
                    "jumlah", itemReq.getJumlah()
            );
            try {
                restTemplate.postForEntity(TICKET_SERVICE_URL + "/api/inventory/hold", holdPayload, Map.class);
            } catch (Exception e) {
                log.error("Gagal melakukan hold stok untuk kategori ID {}: {}", itemReq.getKategoriTiketId(), e.getMessage());
                throw new BadRequestException("Stok tiket untuk kategori '" + cat.getNamaKategori() + "' tidak mencukupi untuk jumlah yang diminta");
            }

            DetailPemesanan detail = DetailPemesanan.builder()
                    .pemesanan(pemesanan)
                    .kategoriTiketId(itemReq.getKategoriTiketId())
                    .namaKategoriTiket(cat.getNamaKategori())
                    .jumlah(itemReq.getJumlah())
                    .hargaSatuan(hargaResmi)
                    .subtotal(subtotal)
                    .build();
            items.add(detail);
        }

        // Biaya Platform Loket.com Standard:
        // 1. Subtotal Tiket
        BigDecimal subtotalTiket = totalHarga;

        // 2. Validasi & Hitung Kode Promo (jika ada)
        BigDecimal diskon = BigDecimal.ZERO;
        String kodePromoDipakai = null;
        if (request.getKodePromo() != null && !request.getKodePromo().trim().isEmpty()) {
            String kode = request.getKodePromo().trim().toUpperCase();
            try {
                Map<String, Object> promoPayload = Map.of(
                        "kode", kode,
                        "totalHarga", subtotalTiket
                );
                ResponseEntity<Map> promoRes = restTemplate.postForEntity(PROMO_SERVICE_URL + "/api/promo/validate", promoPayload, Map.class);
                if (promoRes.getStatusCode().is2xxSuccessful() && promoRes.getBody() != null) {
                    Map body = promoRes.getBody();
                    Boolean isValid = (Boolean) body.get("valid");
                    if (Boolean.TRUE.equals(isValid)) {
                        Object potonganObj = body.get("potongan");
                        if (potonganObj != null) {
                            diskon = new BigDecimal(potonganObj.toString());
                            kodePromoDipakai = kode;
                            log.info("Kode promo {} valid dengan diskon Rp {}", kode, diskon);
                        }
                    } else {
                        log.warn("Kode promo {} tidak valid: {}", kode, body.get("message"));
                    }
                }
            } catch (Exception e) {
                log.warn("Gagal menghubungi promo_service untuk kode {}: {}", kode, e.getMessage());
            }
        }

        // 3. Biaya Layanan Sistem (Platform Fee 2%, minimal Rp 2.500)
        BigDecimal biayaLayanan = BigDecimal.ZERO;
        if (subtotalTiket.compareTo(BigDecimal.ZERO) > 0) {
            biayaLayanan = subtotalTiket.multiply(new BigDecimal("0.02")).setScale(0, RoundingMode.HALF_UP);
            if (biayaLayanan.compareTo(new BigDecimal("2500")) < 0) {
                biayaLayanan = new BigDecimal("2500");
            }
        }

        // 4. Biaya Jasa Penanganan / Administrasi Transaksi Flat Rp 5.000
        BigDecimal biayaAdmin = subtotalTiket.compareTo(BigDecimal.ZERO) > 0 ? new BigDecimal("5000") : BigDecimal.ZERO;

        // 5. Total Bayar Akhir (Subtotal - Diskon + Biaya Layanan + Biaya Admin)
        BigDecimal subtotalSetelahDiskon = subtotalTiket.subtract(diskon).max(BigDecimal.ZERO);
        BigDecimal totalBayarAkhir = subtotalSetelahDiskon.add(biayaLayanan).add(biayaAdmin);

        pemesanan.setSubtotalTiket(subtotalTiket);
        pemesanan.setBiayaLayanan(biayaLayanan);
        pemesanan.setBiayaAdmin(biayaAdmin);
        pemesanan.setDiskon(diskon);
        pemesanan.setKodePromo(kodePromoDipakai);
        pemesanan.setTotalHarga(totalBayarAkhir);
        pemesanan.setDetailPemesananList(items);

        Pemesanan savedOrder = pemesananRepository.save(pemesanan);

        // 6. Notifikasi Otomatis: Pesanan Dibuat
        try {
            Map<String, Object> notifReq = Map.of(
                    "penggunaId", savedOrder.getPenggunaId(),
                    "judul", "Pesanan Berhasil Dibuat",
                    "pesan", "Pesanan #" + savedOrder.getNomorPesanan() + " telah berhasil dibuat. Segera selesaikan pembayaran sebelum tenggat waktu.",
                    "tipe", "IN_APP"
            );
            restTemplate.postForEntity(NOTIFICATION_SERVICE_URL + "/api/notifications/send", notifReq, Map.class);
            log.info("Notifikasi pesanan dibuat berhasil dikirim untuk pengguna ID {}", savedOrder.getPenggunaId());
        } catch (Exception e) {
            log.warn("Gagal kirim notifikasi pemesanan baru: {}", e.getMessage());
        }

        return orderMapper.toPemesananRes(savedOrder);
    }

    @Override
    public PageResponse<PemesananRes> getMyOrders(Authentication authentication, int page, int size) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Silakan login terlebih dahulu");
        }

        Long userId = (Long) authentication.getCredentials();
        if (userId == null) userId = 1L;

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Pemesanan> orderPage = pemesananRepository.findByPenggunaIdAndDeletedAtIsNull(userId, pageable);

        List<PemesananRes> content = orderPage.getContent().stream()
                .map(orderMapper::toPemesananRes)
                .collect(Collectors.toList());

        return PageResponse.<PemesananRes>builder()
                .content(content)
                .pageNumber(orderPage.getNumber())
                .pageSize(orderPage.getSize())
                .totalElements(orderPage.getTotalElements())
                .totalPages(orderPage.getTotalPages())
                .isLast(orderPage.isLast())
                .build();
    }

    @Override
    public PemesananRes getOrderById(Long id, Authentication authentication) {
        Pemesanan order = pemesananRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new NotFoundException("Pesanan tidak ditemukan"));

        validateOwnershipOrAdmin(order.getPenggunaId(), authentication);
        return orderMapper.toPemesananRes(order);
    }

    @Override
    @Transactional
    public PemesananRes cancelOrder(Long id, Authentication authentication) {
        Pemesanan order = pemesananRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new NotFoundException("Pesanan tidak ditemukan"));

        validateOwnershipOrAdmin(order.getPenggunaId(), authentication);

        if (!"PENDING".equalsIgnoreCase(order.getStatus())) {
            throw new BadRequestException("Hanya pesanan berstatus PENDING yang dapat dibatalkan");
        }

        // Lepaskan stok yang sebelumnya direservasi (release)
        if (order.getDetailPemesananList() != null) {
            for (DetailPemesanan item : order.getDetailPemesananList()) {
                Map<String, Object> releasePayload = Map.of(
                        "kategoriTiketId", item.getKategoriTiketId(),
                        "jumlah", item.getJumlah()
                );
                try {
                    restTemplate.postForEntity(TICKET_SERVICE_URL + "/api/inventory/release", releasePayload, Map.class);
                } catch (Exception e) {
                    log.warn("Gagal merilis reservasi stok untuk kategori ID {}: {}", item.getKategoriTiketId(), e.getMessage());
                }
            }
        }

        order.setStatus("CANCELLED");
        order.setUpdatedAt(LocalDateTime.now());
        Pemesanan cancelledOrder = pemesananRepository.save(order);

        // Notifikasi Otomatis: Pesanan Dibatalkan
        try {
            Map<String, Object> notifReq = Map.of(
                    "penggunaId", cancelledOrder.getPenggunaId(),
                    "judul", "Pesanan Dibatalkan",
                    "pesan", "Pesanan #" + cancelledOrder.getNomorPesanan() + " telah dibatalkan.",
                    "tipe", "IN_APP"
            );
            restTemplate.postForEntity(NOTIFICATION_SERVICE_URL + "/api/notifications/send", notifReq, Map.class);
        } catch (Exception e) {
            log.warn("Gagal kirim notifikasi pembatalan: {}", e.getMessage());
        }

        return orderMapper.toPemesananRes(cancelledOrder);
    }

    @Override
    @Transactional
    public PemesananRes paySimulation(Long id, Authentication authentication) {
        Pemesanan order = pemesananRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new NotFoundException("Pesanan tidak ditemukan"));

        validateOwnershipOrAdmin(order.getPenggunaId(), authentication);

        if (!"PENDING".equalsIgnoreCase(order.getStatus())) {
            throw new BadRequestException("Pesanan tidak berstatus PENDING untuk dibayar");
        }

        if (order.getTenggatPembayaran() != null && LocalDateTime.now().isAfter(order.getTenggatPembayaran())) {
            throw new BadRequestException("Tenggat waktu pembayaran untuk pesanan ini telah habis");
        }

        // 1. Konfirmasi kuota terjual dan terbitkan tiket fisik/digital di ticket_service
        if (order.getDetailPemesananList() != null) {
            for (DetailPemesanan item : order.getDetailPemesananList()) {
                // A. Confirm stok
                Map<String, Object> confirmPayload = Map.of(
                        "kategoriTiketId", item.getKategoriTiketId(),
                        "jumlah", item.getJumlah()
                );
                try {
                    restTemplate.postForEntity(TICKET_SERVICE_URL + "/api/inventory/confirm", confirmPayload, Map.class);
                } catch (Exception e) {
                    log.error("Gagal mengonfirmasi stok terjual untuk kategori ID {}: {}", item.getKategoriTiketId(), e.getMessage());
                }

                // B. Issue lembaran tiket fisik sebanyak 'jumlah'
                for (int i = 0; i < item.getJumlah(); i++) {
                    Map<String, Object> issuePayload = Map.of(
                            "kategoriTiketId", item.getKategoriTiketId(),
                            "detailPemesananId", item.getId(),
                            "penggunaId", order.getPenggunaId()
                    );
                    try {
                        restTemplate.postForEntity(TICKET_SERVICE_URL + "/api/tiket/issue", issuePayload, Map.class);
                    } catch (Exception e) {
                        log.error("Gagal menerbitkan e-tiket lembar ke-{} untuk detail ID {}: {}", (i + 1), item.getId(), e.getMessage());
                    }
                }
            }
        }

        // Mark Promo as used in promo_service if applicable
        if (order.getKodePromo() != null && !order.getKodePromo().trim().isEmpty()) {
            try {
                Map<String, Object> usePayload = Map.of(
                        "kode", order.getKodePromo().trim(),
                        "pemesananId", order.getId(),
                        "totalHarga", order.getSubtotalTiket() != null ? order.getSubtotalTiket() : order.getTotalHarga()
                );
                restTemplate.postForEntity(PROMO_SERVICE_URL + "/api/promo/use", usePayload, Map.class);
                log.info("Kode promo {} berhasil dicatat terpakai untuk order #{}", order.getKodePromo(), order.getNomorPesanan());
            } catch (Exception e) {
                log.warn("Gagal menggunakan promo di promo_service: {}", e.getMessage());
            }
        }

        order.setStatus("PAID");
        order.setUpdatedAt(LocalDateTime.now());
        Pemesanan paidOrder = pemesananRepository.save(order);

        // Notifikasi Otomatis: Pembayaran Berhasil & Tiket Terbit
        try {
            Map<String, Object> notifReq = Map.of(
                    "penggunaId", paidOrder.getPenggunaId(),
                    "judul", "Pembayaran Berhasil! E-Tiket Diterbitkan",
                    "pesan", "Pembayaran pesanan #" + paidOrder.getNomorPesanan() + " telah diterima. E-Tiket Anda telah terbit dan siap digunakan di gate masuk.",
                    "tipe", "IN_APP"
            );
            restTemplate.postForEntity(NOTIFICATION_SERVICE_URL + "/api/notifications/send", notifReq, Map.class);
            log.info("Notifikasi pembayaran berhasil dikirim untuk pengguna ID {}", paidOrder.getPenggunaId());
        } catch (Exception e) {
            log.warn("Gagal kirim notifikasi pembayaran: {}", e.getMessage());
        }

        return orderMapper.toPemesananRes(paidOrder);
    }

    private void validateOwnershipOrAdmin(Long targetUserId, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Silakan login terlebih dahulu");
        }

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (isAdmin) return;

        Long currentUserId = (Long) authentication.getCredentials();
        if (currentUserId != null && !currentUserId.equals(targetUserId)) {
            throw new AccessDeniedException("Anda tidak memiliki hak akses untuk pesanan ini");
        }
    }
}
