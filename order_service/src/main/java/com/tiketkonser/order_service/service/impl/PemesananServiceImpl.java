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
import com.tiketkonser.order_service.payload.res.RevenueRes;
import com.tiketkonser.order_service.repository.PemesananRepository;
import com.tiketkonser.order_service.service.PemesananService;
import com.tiketkonser.order_service.utility.OrderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

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
    private final PlatformTransactionManager transactionManager;

    @Override
    @Transactional
    public PemesananRes createOrder(PemesananReq request, Authentication authentication) {
        return createOrder(request, authentication, null);
    }

    @Override
    @Transactional
    public PemesananRes createOrder(PemesananReq request, Authentication authentication, String authHeader) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Silakan login terlebih dahulu");
        }

        Long userId = (Long) authentication.getCredentials();
        if (userId == null) userId = 1L;

        String effectiveAuthHeader = resolveAuthHeader(authHeader);

        String nomorPesanan = "ORD-" + System.currentTimeMillis() / 1000 + "-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();

        BigDecimal subtotalTiket = BigDecimal.ZERO;
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

        // Validasi item kategori tiket
        List<KategoriTiketClientRes> verifiedCategories = new ArrayList<>();
        for (DetailPemesananItemReq itemReq : request.getItems()) {
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

            if (cat.getStatus() != null && !"AVAILABLE".equalsIgnoreCase(cat.getStatus())) {
                throw new BadRequestException("Tiket kategori '" + cat.getNamaKategori() + "' sedang tidak tersedia (" + cat.getStatus() + ")");
            }

            LocalDateTime now = LocalDateTime.now();
            if (cat.getMulaiJual() != null && now.isBefore(cat.getMulaiJual())) {
                throw new BadRequestException("Penjualan tiket untuk kategori '" + cat.getNamaKategori() + "' belum dibuka");
            }

            BigDecimal hargaResmi = cat.getHarga();
            BigDecimal subtotal = hargaResmi.multiply(BigDecimal.valueOf(itemReq.getJumlah()));
            subtotalTiket = subtotalTiket.add(subtotal);

            DetailPemesanan detail = DetailPemesanan.builder()
                    .pemesanan(pemesanan)
                    .kategoriTiketId(itemReq.getKategoriTiketId())
                    .namaKategoriTiket(cat.getNamaKategori())
                    .jumlah(itemReq.getJumlah())
                    .hargaSatuan(hargaResmi)
                    .subtotal(subtotal)
                    .build();
            items.add(detail);
            verifiedCategories.add(cat);
        }

        // Validasi dan hitung diskon promo
        BigDecimal diskon = BigDecimal.ZERO;
        String kodePromoDipakai = null;
        if (request.getKodePromo() != null && !request.getKodePromo().trim().isEmpty()) {
            String kode = request.getKodePromo().trim().toUpperCase();
            try {
                Map<String, Object> promoPayload = Map.of(
                        "kode", kode,
                        "totalHarga", subtotalTiket
                );
                HttpHeaders headers = new HttpHeaders();
                if (authHeader != null && !authHeader.isEmpty()) {
                    headers.set(HttpHeaders.AUTHORIZATION, authHeader);
                }
                HttpEntity<Map<String, Object>> entity = new HttpEntity<>(promoPayload, headers);

                ResponseEntity<Map> promoRes = restTemplate.exchange(
                        PROMO_SERVICE_URL + "/api/promo/validate",
                        HttpMethod.POST,
                        entity,
                        Map.class
                );

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
                        String msg = (String) body.get("message");
                        throw new BadRequestException("Kode promo '" + kode + "' tidak valid: " + (msg != null ? msg : "Syarat promo tidak terpenuhi"));
                    }
                } else {
                    throw new BadRequestException("Kode promo '" + kode + "' tidak valid");
                }
            } catch (HttpStatusCodeException ex) {
                String errMsg = extractErrorMessage(ex);
                log.warn("Validasi promo {} gagal: {}", kode, errMsg);
                throw new BadRequestException("Kode promo '" + kode + "' tidak valid: " + errMsg);
            } catch (BadRequestException bre) {
                throw bre;
            } catch (Exception e) {
                log.error("Gagal menghubungi promo_service untuk kode {}: {}", kode, e.getMessage());
                throw new BadRequestException("Gagal memvalidasi kode promo: " + e.getMessage());
            }
        }

        // Hold stok tiket
        List<DetailPemesananItemReq> heldItems = new ArrayList<>();
        for (DetailPemesananItemReq itemReq : request.getItems()) {
            Map<String, Object> holdPayload = Map.of(
                    "kategoriTiketId", itemReq.getKategoriTiketId(),
                    "jumlah", itemReq.getJumlah()
            );
            try {
                restTemplate.postForEntity(TICKET_SERVICE_URL + "/api/inventory/hold", holdPayload, Map.class);
                heldItems.add(itemReq);
            } catch (Exception e) {
                log.error("Gagal melakukan hold stok untuk kategori ID {}: {}", itemReq.getKategoriTiketId(), e.getMessage());
                // Kompensasi rilis item-item yang sudah sempat di-hold di loop ini
                for (DetailPemesananItemReq prevHeld : heldItems) {
                    try {
                        Map<String, Object> relPayload = Map.of(
                                "kategoriTiketId", prevHeld.getKategoriTiketId(),
                                "jumlah", prevHeld.getJumlah()
                        );
                        restTemplate.postForEntity(TICKET_SERVICE_URL + "/api/inventory/release", relPayload, Map.class);
                    } catch (Exception ignore) {}
                }
                throw new BadRequestException("Stok tiket untuk kategori ID " + itemReq.getKategoriTiketId() + " tidak mencukupi untuk jumlah yang diminta");
            }
        }

        // Biaya platform dan administrasi
        BigDecimal biayaLayanan = BigDecimal.ZERO;
        if (subtotalTiket.compareTo(BigDecimal.ZERO) > 0) {
            biayaLayanan = subtotalTiket.multiply(new BigDecimal("0.02")).setScale(0, RoundingMode.HALF_UP);
            if (biayaLayanan.compareTo(new BigDecimal("2500")) < 0) {
                biayaLayanan = new BigDecimal("2500");
            }
        }

        BigDecimal biayaAdmin = subtotalTiket.compareTo(BigDecimal.ZERO) > 0 ? new BigDecimal("5000") : BigDecimal.ZERO;

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

        // Notifikasi pembuatan pesanan
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
        return paySimulation(id, authentication, null);
    }

    @Override
    @Transactional
    public PemesananRes paySimulation(Long id, Authentication authentication, String authHeader) {
        Pemesanan order = pemesananRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new NotFoundException("Pesanan tidak ditemukan"));

        validateOwnershipOrAdmin(order.getPenggunaId(), authentication);

        if (!"PENDING".equalsIgnoreCase(order.getStatus())) {
            throw new BadRequestException("Pesanan tidak berstatus PENDING untuk dibayar");
        }

        // Validasi tenggat waktu pembayaran
        if (order.getTenggatPembayaran() != null && LocalDateTime.now().isAfter(order.getTenggatPembayaran())) {
            if (order.getDetailPemesananList() != null) {
                for (DetailPemesanan item : order.getDetailPemesananList()) {
                    try {
                        Map<String, Object> releasePayload = Map.of(
                                "kategoriTiketId", item.getKategoriTiketId(),
                                "jumlah", item.getJumlah()
                        );
                        restTemplate.postForEntity(TICKET_SERVICE_URL + "/api/inventory/release", releasePayload, Map.class);
                    } catch (Exception e) {
                        log.warn("Gagal melepaskan reservasi stok kategori ID {} saat pembayaran expired: {}",
                                item.getKategoriTiketId(), e.getMessage());
                    }
                }
            }

            // Commit status CANCELLED dalam transaksi terpisah (REQUIRES_NEW) agar tidak ter-rollback saat throw exception
            TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
            txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
            txTemplate.execute(status -> {
                Pemesanan o = pemesananRepository.findById(order.getId()).orElse(order);
                o.setStatus("CANCELLED");
                o.setUpdatedAt(LocalDateTime.now());
                return pemesananRepository.save(o);
            });

            throw new BadRequestException("Tenggat waktu pembayaran untuk pesanan ini telah habis. Reservasi tiket telah dibatalkan.");
        }

        String effectiveAuthHeader = resolveAuthHeader(authHeader);

        // Catat penggunaan promo
        boolean promoUsed = false;
        if (order.getKodePromo() != null && !order.getKodePromo().trim().isEmpty()) {
            String kode = order.getKodePromo().trim();
            Map<String, Object> usePayload = Map.of(
                    "kode", kode,
                    "pemesananId", order.getId(),
                    "totalHarga", order.getSubtotalTiket() != null ? order.getSubtotalTiket() : order.getTotalHarga()
            );

            HttpHeaders headers = new HttpHeaders();
            if (effectiveAuthHeader != null) {
                headers.set(HttpHeaders.AUTHORIZATION, effectiveAuthHeader);
            }
            HttpEntity<Map<String, Object>> useEntity = new HttpEntity<>(usePayload, headers);

            try {
                ResponseEntity<Map> useRes = restTemplate.exchange(
                        PROMO_SERVICE_URL + "/api/promo/use",
                        HttpMethod.POST,
                        useEntity,
                        Map.class
                );

                if (!useRes.getStatusCode().is2xxSuccessful()) {
                    throw new BadRequestException("Gagal menggunakan promo: Response status " + useRes.getStatusCode());
                }
                promoUsed = true;
                log.info("Kode promo {} berhasil dicatat terpakai untuk order #{}", kode, order.getNomorPesanan());
            } catch (HttpStatusCodeException ex) {
                String errMsg = extractErrorMessage(ex);
                log.warn("Gagal menggunakan promo {} untuk order #{}: {}", kode, order.getNomorPesanan(), errMsg);
                // Batalkan proses jika promo gagal dicatat
                throw new BadRequestException("Gagal menerapkan kode promo: " + errMsg);
            } catch (BadRequestException bre) {
                throw bre;
            } catch (Exception e) {
                log.error("Koneksi ke promo_service gagal saat order #{}: {}", order.getNomorPesanan(), e.getMessage());
                throw new BadRequestException("Gagal menghubungi layanan promo: " + e.getMessage());
            }
        }

        // Konfirmasi kuota dan terbitkan tiket
        List<DetailPemesanan> confirmedItems = new ArrayList<>();
        List<Long> issuedDetailIds = new ArrayList<>();

        try {
            if (order.getDetailPemesananList() != null) {
                for (DetailPemesanan item : order.getDetailPemesananList()) {
                    // Confirm stok
                    Map<String, Object> confirmPayload = Map.of(
                            "kategoriTiketId", item.getKategoriTiketId(),
                            "jumlah", item.getJumlah()
                    );
                    ResponseEntity<Map> confirmRes = restTemplate.postForEntity(
                            TICKET_SERVICE_URL + "/api/inventory/confirm",
                            confirmPayload,
                            Map.class
                    );
                    if (!confirmRes.getStatusCode().is2xxSuccessful()) {
                        throw new RuntimeException("Gagal mengonfirmasi stok terjual untuk kategori ID " + item.getKategoriTiketId());
                    }
                    confirmedItems.add(item);

                    // B. Issue lembaran tiket fisik sebanyak 'jumlah'
                    for (int i = 0; i < item.getJumlah(); i++) {
                        Map<String, Object> issuePayload = Map.of(
                                "kategoriTiketId", item.getKategoriTiketId(),
                                "detailPemesananId", item.getId(),
                                "penggunaId", order.getPenggunaId()
                        );
                        ResponseEntity<Map> issueRes = restTemplate.postForEntity(
                                TICKET_SERVICE_URL + "/api/tiket/issue",
                                issuePayload,
                                Map.class
                        );
                        if (!issueRes.getStatusCode().is2xxSuccessful()) {
                            throw new RuntimeException("Gagal menerbitkan e-tiket lembar ke-" + (i + 1) + " untuk detail ID " + item.getId());
                        }
                    }
                    issuedDetailIds.add(item.getId());
                }
            }
        } catch (Exception ticketErr) {
            log.error("Kendala pada ticket_service saat pembayaran order #{}: {}", order.getNomorPesanan(), ticketErr.getMessage());

            // KOMPENSASI 1: Jika promo sudah sempat tercatat USED, batalkan penggunaan promo!
            if (promoUsed) {
                try {
                    HttpHeaders headers = new HttpHeaders();
                    if (effectiveAuthHeader != null) {
                        headers.set(HttpHeaders.AUTHORIZATION, effectiveAuthHeader);
                    }
                    HttpEntity<Void> cancelEntity = new HttpEntity<>(headers);
                    restTemplate.exchange(
                            PROMO_SERVICE_URL + "/api/promo/cancel-usage/" + order.getId(),
                            HttpMethod.POST,
                            cancelEntity,
                            Map.class
                    );
                    log.info("Kompensasi: Penggunaan promo untuk order #{} berhasil dibatalkan.", order.getNomorPesanan());
                } catch (Exception compErr) {
                    log.error("Gagal melakukan kompensasi promo untuk order #{}: {}", order.getNomorPesanan(), compErr.getMessage());
                }
            }

            // KOMPENSASI 2: Jika stok sudah sempat dikonfirmasi terjual, kembalikan kuota stok terjual (return)
            for (DetailPemesanan cItem : confirmedItems) {
                try {
                    Map<String, Object> returnPayload = Map.of(
                            "kategoriTiketId", cItem.getKategoriTiketId(),
                            "jumlah", cItem.getJumlah()
                    );
                    restTemplate.postForEntity(TICKET_SERVICE_URL + "/api/inventory/return", returnPayload, Map.class);
                    log.info("Kompensasi: Stok kategori ID {} berhasil dikembalikan sebanyak {}", cItem.getKategoriTiketId(), cItem.getJumlah());
                } catch (Exception retErr) {
                    log.error("Gagal kompensasi return stok kategori ID {}: {}", cItem.getKategoriTiketId(), retErr.getMessage());
                }
            }

            // KOMPENSASI 3: Jika ada lembar tiket yang sudah sempat terbit sebagian, tandai REFUNDED
            for (Long dId : issuedDetailIds) {
                try {
                    restTemplate.postForEntity(TICKET_SERVICE_URL + "/api/tiket/refund-by-detail/" + dId, null, Map.class);
                    log.info("Kompensasi: Tiket terbit untuk detail ID {} berhasil ditandai REFUNDED", dId);
                } catch (Exception refErr) {
                    log.error("Gagal kompensasi refund tiket detail ID {}: {}", dId, refErr.getMessage());
                }
            }

            throw new BadRequestException("Pembayaran gagal karena kesalahan pada sistem tiket: " + ticketErr.getMessage() + ". Penggunaan promo telah dikembalikan.");
        }

        // Update status pesanan
        order.setStatus("PAID");
        order.setUpdatedAt(LocalDateTime.now());
        Pemesanan paidOrder = pemesananRepository.save(order);

        // Kirim notifikasi pembayaran berhasil
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

    private String resolveAuthHeader(String authHeader) {
        if (authHeader != null && !authHeader.trim().isEmpty()) {
            return authHeader.trim();
        }
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null && attrs.getRequest() != null) {
                String h = attrs.getRequest().getHeader(HttpHeaders.AUTHORIZATION);
                if (h != null && !h.trim().isEmpty()) {
                    return h.trim();
                }
            }
        } catch (Exception ignored) {}
        return null;
    }

    private String extractErrorMessage(HttpStatusCodeException ex) {
        try {
            String body = ex.getResponseBodyAsString();
            if (body != null && body.contains("\"message\"")) {
                int idx = body.indexOf("\"message\"");
                int startQuote = body.indexOf("\"", idx + 9);
                int endQuote = body.indexOf("\"", startQuote + 1);
                if (startQuote != -1 && endQuote != -1) {
                    return body.substring(startQuote + 1, endQuote);
                }
            }
            if (body != null && !body.trim().isEmpty()) {
                return body;
            }
        } catch (Exception ignored) {}
        return ex.getStatusText() != null ? ex.getStatusText() : ex.getMessage();
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

    @Override
    public RevenueRes getRevenueByAcara(Long acaraId, List<Long> kategoriTiketIds) {
        if (kategoriTiketIds == null || kategoriTiketIds.isEmpty()) {
            return RevenueRes.builder()
                    .acaraId(acaraId)
                    .totalRevenue(BigDecimal.ZERO)
                    .totalOrder(0L)
                    .totalTiket(0L)
                    .perKategori(new java.util.ArrayList<>())
                    .build();
        }

        List<Pemesanan> paidOrders = pemesananRepository.findPaidOrdersByKategoriIds(kategoriTiketIds);

        BigDecimal totalRevenue = BigDecimal.ZERO;
        long totalTiket = 0;

        Map<Long, RevenueRes.KategoriRevenueItem> kategoriMap = new java.util.LinkedHashMap<>();

        for (Pemesanan p : paidOrders) {
            totalRevenue = totalRevenue.add(p.getTotalHarga() != null ? p.getTotalHarga() : BigDecimal.ZERO);
            for (DetailPemesanan d : p.getDetailPemesananList()) {
                if (kategoriTiketIds.contains(d.getKategoriTiketId())) {
                    totalTiket += d.getJumlah();
                    kategoriMap.merge(
                        d.getKategoriTiketId(),
                        RevenueRes.KategoriRevenueItem.builder()
                                .kategoriTiketId(d.getKategoriTiketId())
                                .namaKategori(d.getNamaKategoriTiket())
                                .jumlahTerjual(d.getJumlah())
                                .hargaSatuan(d.getHargaSatuan())
                                .subtotal(d.getSubtotal())
                                .build(),
                        (existing, newer) -> {
                            existing.setJumlahTerjual(existing.getJumlahTerjual() + newer.getJumlahTerjual());
                            existing.setSubtotal(existing.getSubtotal().add(newer.getSubtotal()));
                            return existing;
                        }
                    );
                }
            }
        }

        return RevenueRes.builder()
                .acaraId(acaraId)
                .totalRevenue(totalRevenue)
                .totalOrder((long) paidOrders.size())
                .totalTiket(totalTiket)
                .perKategori(new java.util.ArrayList<>(kategoriMap.values()))
                .build();
    }
}
