package com.tiketkonser.ticket_service.service.impl;

import com.tiketkonser.ticket_service.entity.KategoriTiket;
import com.tiketkonser.ticket_service.entity.Tiket;
import com.tiketkonser.ticket_service.handler.BadRequestException;
import com.tiketkonser.ticket_service.handler.NotFoundException;
import com.tiketkonser.ticket_service.payload.req.IssueTiketReq;
import com.tiketkonser.ticket_service.payload.req.TransferTiketReq;
import com.tiketkonser.ticket_service.payload.res.PageResponse;
import com.tiketkonser.ticket_service.payload.res.ManifestItemRes;
import com.tiketkonser.ticket_service.payload.res.TicketStatsRes;
import com.tiketkonser.ticket_service.payload.res.TiketRes;
import com.tiketkonser.ticket_service.payload.res.TransferTiketRes;
import com.tiketkonser.ticket_service.repository.KategoriTiketRepository;
import com.tiketkonser.ticket_service.repository.TiketRepository;
import com.tiketkonser.ticket_service.service.TiketService;
import com.tiketkonser.ticket_service.utility.TicketMapper;
import lombok.RequiredArgsConstructor;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TiketServiceImpl implements TiketService {

    private final TiketRepository tiketRepository;
    private final KategoriTiketRepository kategoriTiketRepository;
    private final TicketMapper ticketMapper;
    private final RestTemplate restTemplate;

    @Override
    @Transactional
    public TiketRes issueTiket(IssueTiketReq request) {
        KategoriTiket kt = kategoriTiketRepository.findById(request.getKategoriTiketId())
                .orElseThrow(() -> new NotFoundException("Kategori tiket tidak ditemukan"));

        String uniqueQr = "TK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase() + "-" + (System.currentTimeMillis() % 10000);

        Tiket tiket = Tiket.builder()
                .kategoriTiket(kt)
                .detailPemesananId(request.getDetailPemesananId())
                .penggunaId(request.getPenggunaId())
                .kodeQr(uniqueQr)
                .status("ISSUED")
                .diterbitkanAt(LocalDateTime.now())
                .build();

        return ticketMapper.toTiketRes(tiketRepository.save(tiket));
    }

    @Override
    public PageResponse<TiketRes> getMyTiket(Authentication authentication, int page, int size) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Silakan login terlebih dahulu");
        }

        Long userId = (Long) authentication.getCredentials();
        if (userId == null) userId = 1L;

        Pageable pageable = PageRequest.of(page, size, Sort.by("diterbitkanAt").descending());
        Page<Tiket> tiketPage = tiketRepository.findByPenggunaIdAndDeletedAtIsNull(userId, pageable);

        List<TiketRes> content = tiketPage.getContent().stream()
                .map(ticketMapper::toTiketRes)
                .collect(Collectors.toList());

        return PageResponse.<TiketRes>builder()
                .content(content)
                .pageNumber(tiketPage.getNumber())
                .pageSize(tiketPage.getSize())
                .totalElements(tiketPage.getTotalElements())
                .totalPages(tiketPage.getTotalPages())
                .isLast(tiketPage.isLast())
                .build();
    }

    @Override
    public TiketRes getByKodeQr(String kodeQr) {
        Tiket tiket = tiketRepository.findByKodeQrAndDeletedAtIsNull(kodeQr)
                .orElseThrow(() -> new NotFoundException("Tiket tidak ditemukan"));
        return ticketMapper.toTiketRes(tiket);
    }

    @Override
    @Transactional
    public TiketRes validateOrUseTiket(String kodeQr) {
        return validateOrUseTiket(kodeQr, null);
    }

    @Override
    @Transactional
    public TiketRes validateOrUseTiket(String kodeQr, Long acaraId) {
        Tiket tiket = tiketRepository.findByKodeQrAndDeletedAtIsNull(kodeQr)
                .orElseThrow(() -> new NotFoundException("Tiket tidak ditemukan"));

        if (acaraId != null && tiket.getKategoriTiket() != null && !acaraId.equals(tiket.getKategoriTiket().getAcaraId())) {
            throw new BadRequestException("Tiket ini bukan untuk acara/konser ini");
        }

        if ("USED".equalsIgnoreCase(tiket.getStatus())) {
            throw new BadRequestException("Tiket ini sudah pernah digunakan untuk check-in");
        }
        if ("TRANSFERRED".equalsIgnoreCase(tiket.getStatus())) {
            throw new BadRequestException("Tiket ini telah dipindahtangankan (ditransfer) ke pemilik lain dan QR Code ini sudah dicabut.");
        }
        if ("CANCELLED".equalsIgnoreCase(tiket.getStatus())) {
            throw new BadRequestException("Tiket ini telah dibatalkan");
        }

        tiket.setStatus("USED");
        tiket.setDigunakanAt(LocalDateTime.now());

        return ticketMapper.toTiketRes(tiketRepository.save(tiket));
    }

        @Override
    @Transactional
    public TransferTiketRes transferTiket(Long tiketId, TransferTiketReq request, Authentication authentication) {
        return transferTiket(tiketId, request, authentication, null);
    }

    @Override
    @Transactional
    public TransferTiketRes transferTiket(Long tiketId, TransferTiketReq request, Authentication authentication, String authHeader) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Silakan login terlebih dahulu untuk mentransfer tiket");
        }

        Long senderUserId = (Long) authentication.getCredentials();
        if (senderUserId == null) senderUserId = 1L;
        String senderEmail = authentication.getName();

        Tiket oldTiket = tiketRepository.findById(tiketId)
                .orElseThrow(() -> new NotFoundException("Tiket dengan ID " + tiketId + " tidak ditemukan"));

        if (!oldTiket.getPenggunaId().equals(senderUserId)) {
            throw new AccessDeniedException("Anda tidak memiliki hak untuk mentransfer tiket milik orang lain");
        }

        if ("USED".equalsIgnoreCase(oldTiket.getStatus())) {
            throw new BadRequestException("Tiket sudah digunakan untuk check-in, tidak dapat ditransfer");
        }
        if ("TRANSFERRED".equalsIgnoreCase(oldTiket.getStatus())) {
            throw new BadRequestException("Tiket ini sudah pernah dipindahtangankan (ditransfer) sebelumnya");
        }
        if ("CANCELLED".equalsIgnoreCase(oldTiket.getStatus())) {
            throw new BadRequestException("Tiket yang telah dibatalkan tidak dapat ditransfer");
        }

        String targetEmail = request.getPenerimaEmail().trim();
        if (senderEmail != null && senderEmail.equalsIgnoreCase(targetEmail)) {
            throw new BadRequestException("Anda tidak dapat mentransfer tiket ke akun Anda sendiri");
        }

        // Verify recipient from user_service with forwarded JWT
        Long recipientUserId = null;
        String recipientNama = "Pengguna TIKARA";
        String effectiveAuthHeader = resolveAuthHeader(authHeader);

        try {
            String checkUrl = "http://localhost:8081/api/users/check-recipient?email=" + targetEmail;
            HttpHeaders headers = new HttpHeaders();
            if (effectiveAuthHeader != null && !effectiveAuthHeader.isEmpty()) {
                headers.set(HttpHeaders.AUTHORIZATION, effectiveAuthHeader);
            }
            HttpEntity<?> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> userRes = restTemplate.exchange(
                    checkUrl,
                    HttpMethod.GET,
                    entity,
                    Map.class
            );

            if (userRes.getStatusCode().is2xxSuccessful() && userRes.getBody() != null) {
                Map body = userRes.getBody();
                Object idObj = body.get("id");
                if (idObj instanceof Number) {
                    recipientUserId = ((Number) idObj).longValue();
                } else if (idObj != null) {
                    recipientUserId = Long.valueOf(idObj.toString());
                }
                if (body.get("nama") != null) {
                    recipientNama = body.get("nama").toString();
                }
            }
        } catch (HttpStatusCodeException httpEx) {
            if (httpEx.getStatusCode().value() == 404) {
                throw new BadRequestException("Penerima dengan email '" + targetEmail + "' belum terdaftar di TIKARA. Pastikan calon penerima sudah memiliki akun aktif.");
            }
            if (httpEx.getStatusCode().value() == 400) {
                throw new BadRequestException("Anda tidak dapat mentransfer tiket ke akun Anda sendiri");
            }
            if (httpEx.getStatusCode().value() == 403 || httpEx.getStatusCode().value() == 401) {
                throw new AccessDeniedException("Sesi login Anda telah berakhir atau tidak valid. Silakan login kembali.");
            }
            throw new BadRequestException("Gagal memverifikasi akun penerima: " + httpEx.getMessage());
        } catch (Exception e) {
            throw new BadRequestException("Gagal menghubungi server identitas untuk memverifikasi penerima: " + e.getMessage());
        }

        if (recipientUserId == null) {
            throw new BadRequestException("Gagal memverifikasi akun penerima di server identitas TIKARA.");
        }

        if (recipientUserId.equals(senderUserId)) {
            throw new BadRequestException("Anda tidak dapat mentransfer tiket ke akun Anda sendiri");
        }

        // Mark old ticket as TRANSFERRED
        oldTiket.setStatus("TRANSFERRED");
        oldTiket.setDigunakanAt(LocalDateTime.now());
        tiketRepository.save(oldTiket);

        // Issue new fresh ticket for recipient
        String newQr = "TK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase() + "-" + (System.currentTimeMillis() % 10000);
        Tiket newTiket = Tiket.builder()
                .kategoriTiket(oldTiket.getKategoriTiket())
                .detailPemesananId(oldTiket.getDetailPemesananId())
                .penggunaId(recipientUserId)
                .kodeQr(newQr)
                .status("ISSUED")
                .diterbitkanAt(LocalDateTime.now())
                .build();

        Tiket savedNewTiket = tiketRepository.save(newTiket);

        // Send Notification to sender and receiver
        try {
            Map<String, Object> notifSender = Map.of(
                    "penggunaId", senderUserId,
                    "judul", "Transfer Tiket Berhasil",
                    "pesan", "Tiket ID #TKT-" + oldTiket.getId() + " telah berhasil dipindahtangankan kepada " + recipientNama + " (" + targetEmail + ").",
                    "tipe", "IN_APP"
            );
            restTemplate.postForEntity("http://localhost:8086/api/notifications/send", notifSender, Map.class);

            Map<String, Object> notifReceiver = Map.of(
                    "penggunaId", recipientUserId,
                    "judul", "Tiket Konser Diterima!",
                    "pesan", "Selamat! Anda menerima transfer tiket resmi (ID #TKT-" + savedNewTiket.getId() + ") dari " + (senderEmail != null ? senderEmail : "pengguna lain") + ". Buka E-Tiket Anda sekarang.",
                    "tipe", "IN_APP"
            );
            restTemplate.postForEntity("http://localhost:8086/api/notifications/send", notifReceiver, Map.class);
        } catch (Exception notifErr) {
            System.err.println("Gagal mengirim notifikasi transfer: " + notifErr.getMessage());
        }

        return TransferTiketRes.builder()
                .status("SUCCESS")
                .message("Tiket berhasil dipindahtangankan kepada " + recipientNama + " (" + targetEmail + ").")
                .oldTiketId(oldTiket.getId())
                .newTiketId(savedNewTiket.getId())
                .newKodeQr(newQr)
                .penerimaPenggunaId(recipientUserId)
                .penerimaEmail(targetEmail)
                .penerimaNama(recipientNama)
                .transferAt(LocalDateTime.now())
                .build();
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

@Override
    @Transactional
    public void refundTiketByDetailPemesananId(Long detailPemesananId) {
        List<Tiket> tikets = tiketRepository.findByDetailPemesananIdAndDeletedAtIsNull(detailPemesananId);
        for (Tiket t : tikets) {
            t.setStatus("REFUNDED");
            tiketRepository.save(t);
        }
    }


    @Override
    public boolean hasTicketForEvent(Long penggunaId, Long acaraId) {
        if (penggunaId == null || acaraId == null) return false;
        return tiketRepository.existsByPenggunaIdAndKategoriTiket_AcaraIdAndStatusInAndDeletedAtIsNull(
                penggunaId, acaraId, List.of("ISSUED", "USED")
        );
    }

    @Override
    public TicketStatsRes getTicketStatsByAcara(Long acaraId) {
        List<KategoriTiket> kategoriList = kategoriTiketRepository.findByAcaraId(acaraId);
        List<Tiket> allTikets = tiketRepository.findAllByAcaraId(acaraId);

        Map<Long, List<Tiket>> tiketByKategori = allTikets.stream()
                .collect(Collectors.groupingBy(t -> t.getKategoriTiket().getId()));

        int totalKuota = 0, totalTerjual = 0, totalUsed = 0;
        List<TicketStatsRes.KategoriStatsItem> perKategori = new java.util.ArrayList<>();

        for (KategoriTiket kt : kategoriList) {
            List<Tiket> tikets = tiketByKategori.getOrDefault(kt.getId(), List.of());
            int terjual = (int) tikets.stream().filter(t ->
                    List.of("ISSUED", "USED", "TRANSFERRED").contains(t.getStatus())).count();
            int digunakan = (int) tikets.stream().filter(t -> "USED".equalsIgnoreCase(t.getStatus())).count();
            int tersisa = Math.max(0, kt.getKuota() - terjual);
            double persen = kt.getKuota() > 0 ? (double) terjual / kt.getKuota() * 100 : 0;

            totalKuota += kt.getKuota();
            totalTerjual += terjual;
            totalUsed += digunakan;

            perKategori.add(TicketStatsRes.KategoriStatsItem.builder()
                    .kategoriId(kt.getId())
                    .namaKategori(kt.getNamaKategori())
                    .harga(kt.getHarga())
                    .kuota(kt.getKuota())
                    .terjual(terjual)
                    .tersisa(tersisa)
                    .digunakan(digunakan)
                    .persentaseTerjual(Math.round(persen * 10.0) / 10.0)
                    .status(kt.getStatus())
                    .build());
        }

        int totalTersisa = Math.max(0, totalKuota - totalTerjual);
        double overallPersen = totalKuota > 0 ? (double) totalTerjual / totalKuota * 100 : 0;

        return TicketStatsRes.builder()
                .acaraId(acaraId)
                .totalKuota(totalKuota)
                .totalTerjual(totalTerjual)
                .totalTersisa(totalTersisa)
                .totalUsed(totalUsed)
                .persentaseTerjual(Math.round(overallPersen * 10.0) / 10.0)
                .perKategori(perKategori)
                .build();
    }

    @Override
    public List<ManifestItemRes> getManifestByAcara(Long acaraId) {
        List<Tiket> tikets = tiketRepository.findAllByAcaraId(acaraId);
        return tikets.stream().map(t -> ManifestItemRes.builder()
                .tiketId(t.getId())
                .kodeQr(t.getKodeQr())
                .penggunaId(t.getPenggunaId())
                .kategoriTiketId(t.getKategoriTiket().getId())
                .namaKategori(t.getKategoriTiket().getNamaKategori())
                .harga(t.getKategoriTiket().getHarga())
                .status(t.getStatus())
                .diterbitkanAt(t.getDiterbitkanAt() != null ? t.getDiterbitkanAt().toString() : null)
                .digunakanAt(t.getDigunakanAt() != null ? t.getDigunakanAt().toString() : null)
                .build()
        ).collect(Collectors.toList());
    }
}
