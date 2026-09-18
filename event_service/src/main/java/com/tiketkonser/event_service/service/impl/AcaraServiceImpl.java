package com.tiketkonser.event_service.service.impl;

import com.tiketkonser.event_service.entity.Acara;
import com.tiketkonser.event_service.entity.Kategori;
import com.tiketkonser.event_service.handler.BadRequestException;
import com.tiketkonser.event_service.handler.NotFoundException;
import com.tiketkonser.event_service.payload.req.AcaraReq;
import com.tiketkonser.event_service.payload.res.AcaraRes;
import com.tiketkonser.event_service.payload.res.EventSalesSummaryRes;
import com.tiketkonser.event_service.payload.res.OrganizerAnalyticsRes;
import com.tiketkonser.event_service.payload.res.OrganizerEventDetailRes;
import com.tiketkonser.event_service.payload.res.PageResponse;
import com.tiketkonser.event_service.repository.AcaraRepository;
import com.tiketkonser.event_service.repository.KategoriRepository;
import com.tiketkonser.event_service.repository.UlasanRepository;
import lombok.extern.slf4j.Slf4j;
import com.tiketkonser.event_service.service.AcaraService;
import com.tiketkonser.event_service.utility.AcaraSpecification;
import com.tiketkonser.event_service.utility.EventMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AcaraServiceImpl implements AcaraService {
    private AcaraRes enrichWithRating(AcaraRes res) {
        if (res == null || res.getId() == null)
            return res;
        try {
            Double avg = ulasanRepository.findAverageRatingByAcaraId(res.getId());
            Long count = ulasanRepository.countByAcaraId(res.getId());
            res.setAverageRating(avg != null ? Math.round(avg * 10.0) / 10.0 : 0.0);
            res.setTotalUlasan(count != null ? count.intValue() : 0);
        } catch (Exception e) {
            res.setAverageRating(0.0);
            res.setTotalUlasan(0);
        }
        return res;
    }

    private final AcaraRepository acaraRepository;
    private final KategoriRepository kategoriRepository;
    private final UlasanRepository ulasanRepository;
    private final EventMapper eventMapper;
    private final RestTemplate restTemplate;

    @Override
    @Transactional
    public AcaraRes create(AcaraReq request, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Silakan login terlebih dahulu");
        }

        Long organizerId = (Long) authentication.getCredentials();
        String organizerEmail = authentication.getName();
        if (organizerId == null) {
            throw new AccessDeniedException("Identitas penyelenggara tidak valid");
        }

        if (request.getTanggalMulai() == null || request.getTanggalSelesai() == null) {
            throw new BadRequestException("Tanggal mulai dan tanggal selesai konser wajib diisi");
        }
        if (!request.getTanggalSelesai().isAfter(request.getTanggalMulai())) {
            throw new BadRequestException("Tanggal selesai konser harus setelah tanggal mulai");
        }

        Set<Kategori> meKategori = new HashSet<>();
        if (request.getKategoriIds() != null && !request.getKategoriIds().isEmpty()) {
            meKategori = new HashSet<>(kategoriRepository.findAllById(request.getKategoriIds()));
        }

        String initialStatus = "PENDING_REVIEW";
        if (request.getStatus() != null && !request.getStatus().trim().isEmpty()) {
            String requested = request.getStatus().trim().toUpperCase();
            if ("DRAFT".equals(requested)) {
                initialStatus = "DRAFT";
            }
        }

        Acara acara = Acara.builder()
                .organizerId(organizerId)
                .organizerNama(organizerEmail)
                .nama(request.getNama().trim())
                .deskripsi(request.getDeskripsi().trim())
                .venue(request.getVenue().trim())
                .kota(request.getKota().trim())
                .tanggalMulai(request.getTanggalMulai())
                .tanggalSelesai(request.getTanggalSelesai())
                .status(initialStatus)
                .gambarUrl(request.getGambarUrl())
                .meKategori(meKategori)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Acara saved = acaraRepository.save(acara);

        if ("PENDING_REVIEW".equals(initialStatus)) {
            try {
                Map<String, Object> adminNotif = Map.of(
                        "penggunaId", 1L,
                        "judul", "Event Baru Menunggu Review",
                        "pesan",
                        "Penyelenggara " + organizerEmail + " telah mendaftarkan event '" + saved.getNama()
                                + "'. Silakan periksa di Hub Moderasi.",
                        "tipe", "IN_APP");
                restTemplate.postForEntity("http://localhost:8086/api/notifications/send", adminNotif, Map.class);
            } catch (Exception ignored) {
            }
        }

        return enrichWithRating(eventMapper.toAcaraRes(saved));
    }

    @Override
    public PageResponse<AcaraRes> getAll(String search, String kota, String status, int page, int size, String sortBy,
            String sortDir) {
        return getAll(search, kota, null, status, page, size, sortBy, sortDir, null);
    }

    @Override
    public PageResponse<AcaraRes> getAll(String search, String kota, Long kategoriId, String status, int page, int size,
            String sortBy, String sortDir, Authentication authentication) {
        return getAll(search, kota, kategoriId, null, status, page, size, sortBy, sortDir, authentication);
    }

    @Override
    public PageResponse<AcaraRes> getAll(String search, String kota, Long kategoriId, String kategoriNama,
            String status, int page, int size, String sortBy, String sortDir, Authentication authentication) {
        String effectiveStatus = "PUBLISHED";
        if (authentication != null && authentication.isAuthenticated()) {
            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            if (isAdmin) {
                effectiveStatus = status;
            }
        }

        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Specification<Acara> spec = AcaraSpecification.filterAcara(search, kota, kategoriId, kategoriNama,
                effectiveStatus);

        Page<Acara> pageData = acaraRepository.findAll(spec, pageable);
        List<AcaraRes> content = pageData.getContent().stream().map(eventMapper::toAcaraRes).map(this::enrichWithRating)
                .collect(Collectors.toList());

        return PageResponse.<AcaraRes>builder()
                .content(content)
                .pageNumber(pageData.getNumber())
                .pageSize(pageData.getSize())
                .totalElements(pageData.getTotalElements())
                .totalPages(pageData.getTotalPages())
                .isLast(pageData.isLast())
                .build();
    }

    @Override
    public PageResponse<AcaraRes> getMyEvents(String search, String status, int page, int size, String sortBy,
            String sortDir, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Silakan login terlebih dahulu");
        }

        Long organizerId = (Long) authentication.getCredentials();
        if (organizerId == null) {
            throw new AccessDeniedException("Identitas pengguna tidak valid");
        }

        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Specification<Acara> spec = AcaraSpecification.filterAcara(search, null, null, null, status, organizerId);

        Page<Acara> pageData = acaraRepository.findAll(spec, pageable);
        List<AcaraRes> content = pageData.getContent().stream().map(eventMapper::toAcaraRes).map(this::enrichWithRating)
                .collect(Collectors.toList());

        return PageResponse.<AcaraRes>builder()
                .content(content)
                .pageNumber(pageData.getNumber())
                .pageSize(pageData.getSize())
                .totalElements(pageData.getTotalElements())
                .totalPages(pageData.getTotalPages())
                .isLast(pageData.isLast())
                .build();
    }

    @Override
    public OrganizerAnalyticsRes getOrganizerAnalytics(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Silakan login terlebih dahulu");
        }

        Long organizerId = (Long) authentication.getCredentials();
        if (organizerId == null) {
            throw new AccessDeniedException("Identitas pengguna tidak valid");
        }

        List<Acara> myEvents = acaraRepository
                .findAll(AcaraSpecification.filterAcara(null, null, null, null, null, organizerId));

        BigDecimal totalPendapatan = BigDecimal.ZERO;
        int totalTiketTerjual = 0;
        int totalKuota = 0;
        long eventAktif = 0;
        long eventDraft = 0;
        long eventSoldOut = 0;
        long eventSelesai = 0;

        List<EventSalesSummaryRes> breakdowns = new ArrayList<>();

        for (Acara ev : myEvents) {
            String st = ev.getStatus() != null ? ev.getStatus().toUpperCase() : "DRAFT";
            if ("PUBLISHED".equals(st))
                eventAktif++;
            else if ("DRAFT".equals(st) || "PENDING_REVIEW".equals(st))
                eventDraft++;
            else if ("SOLD_OUT".equals(st))
                eventSoldOut++;
            else if ("ENDED".equals(st))
                eventSelesai++;

            int evKuota = 0;
            int evTerjual = 0;
            BigDecimal evPendapatan = BigDecimal.ZERO;

            try {
                String ticketUrl = "http://localhost:8083/api/kategori-tiket/acara/" + ev.getId();
                ResponseEntity<List<Map<String, Object>>> res = restTemplate.exchange(
                        ticketUrl,
                        HttpMethod.GET,
                        null,
                        new ParameterizedTypeReference<List<Map<String, Object>>>() {
                        });

                if (res.getStatusCode().is2xxSuccessful() && res.getBody() != null) {
                    for (Map<String, Object> kt : res.getBody()) {
                        Number kuotaNum = (Number) kt.get("kuota");
                        Number terjualNum = (Number) kt.get("terjual");
                        Number hargaNum = (Number) kt.get("harga");

                        int k = kuotaNum != null ? kuotaNum.intValue() : 0;
                        int t = terjualNum != null ? terjualNum.intValue() : 0;
                        BigDecimal h = hargaNum != null ? new BigDecimal(hargaNum.toString()) : BigDecimal.ZERO;

                        evKuota += k;
                        evTerjual += t;
                        evPendapatan = evPendapatan.add(h.multiply(BigDecimal.valueOf(t)));
                    }
                }
            } catch (Exception e) {
            }

            totalKuota += evKuota;
            totalTiketTerjual += evTerjual;
            totalPendapatan = totalPendapatan.add(evPendapatan);

            int evSisa = Math.max(0, evKuota - evTerjual);
            double evPercent = evKuota > 0 ? ((double) evTerjual / evKuota) * 100.0 : 0.0;

            breakdowns.add(EventSalesSummaryRes.builder()
                    .id(ev.getId())
                    .nama(ev.getNama())
                    .venue(ev.getVenue())
                    .kota(ev.getKota())
                    .status(st)
                    .gambarUrl(ev.getGambarUrl())
                    .tanggalMulai(ev.getTanggalMulai())
                    .kuota(evKuota)
                    .terjual(evTerjual)
                    .sisa(evSisa)
                    .keterisianPersen(Math.round(evPercent * 10.0) / 10.0)
                    .pendapatan(evPendapatan)
                    .build());
        }

        double tingkatKeterisian = totalKuota > 0 ? ((double) totalTiketTerjual / totalKuota) * 100.0 : 0.0;

        return OrganizerAnalyticsRes.builder()
                .totalPendapatan(totalPendapatan)
                .totalTiketTerjual(totalTiketTerjual)
                .totalKuota(totalKuota)
                .tingkatKeterisian(Math.round(tingkatKeterisian * 10.0) / 10.0)
                .totalEvent((long) myEvents.size())
                .eventAktif(eventAktif)
                .eventDraft(eventDraft)
                .eventSoldOut(eventSoldOut)
                .eventSelesai(eventSelesai)
                .eventBreakdowns(breakdowns)
                .build();
    }

    @Override
    @Transactional
    public AcaraRes changeStatus(Long id, String status, Authentication authentication) {
        Acara acara = acaraRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new NotFoundException("Acara tidak ditemukan"));

        validateOwnershipOrAdmin(acara, authentication);

        String st = status != null ? status.trim().toUpperCase() : "DRAFT";
        acara.setStatus(st);
        acara.setUpdatedAt(LocalDateTime.now());

        Acara updated = acaraRepository.save(acara);
        return eventMapper.toAcaraRes(updated);
    }

    @Override
    public AcaraRes getById(Long id) {
        return getById(id, null);
    }

    @Override
    public AcaraRes getById(Long id, Authentication authentication) {
        Acara acara = acaraRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new NotFoundException("Acara tidak ditemukan"));

        if (!List.of("PUBLISHED", "SOLD_OUT", "ENDED").contains(acara.getStatus().toUpperCase())) {
            boolean allowed = false;
            if (authentication != null && authentication.isAuthenticated()) {
                boolean isAdmin = authentication.getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
                if (isAdmin) {
                    allowed = true;
                } else {
                    Long currentUserId = (Long) authentication.getCredentials();
                    if (currentUserId != null && currentUserId.equals(acara.getOrganizerId())) {
                        allowed = true;
                    }
                }
            }
            if (!allowed) {
                throw new NotFoundException("Acara tidak ditemukan");
            }
        }

        return enrichWithRating(eventMapper.toAcaraRes(acara));
    }

    @Override
    @Transactional
    public AcaraRes update(Long id, AcaraReq request, Authentication authentication) {
        Acara acara = acaraRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new NotFoundException("Acara tidak ditemukan"));

        validateOwnershipOrAdmin(acara, authentication);

        if (request.getTanggalMulai() != null && request.getTanggalSelesai() != null) {
            if (!request.getTanggalSelesai().isAfter(request.getTanggalMulai())) {
                throw new BadRequestException("Tanggal selesai konser harus setelah tanggal mulai");
            }
        }

        if (request.getKategoriIds() != null && !request.getKategoriIds().isEmpty()) {
            Set<Kategori> meKategori = new HashSet<>(kategoriRepository.findAllById(request.getKategoriIds()));
            acara.setMeKategori(meKategori);
        }

        acara.setNama(request.getNama());
        acara.setDeskripsi(request.getDeskripsi());
        acara.setVenue(request.getVenue());
        acara.setKota(request.getKota());
        if (request.getTanggalMulai() != null)
            acara.setTanggalMulai(request.getTanggalMulai());
        if (request.getTanggalSelesai() != null)
            acara.setTanggalSelesai(request.getTanggalSelesai());
        if (request.getStatus() != null && !request.getStatus().trim().isEmpty()) {
            acara.setStatus(request.getStatus().trim().toUpperCase());
        }
        if (request.getGambarUrl() != null)
            acara.setGambarUrl(request.getGambarUrl());
        acara.setUpdatedAt(LocalDateTime.now());

        Acara updated = acaraRepository.save(acara);
        return eventMapper.toAcaraRes(updated);
    }

    @Override
    @Transactional
    public void delete(Long id, Authentication authentication) {
        Acara acara = acaraRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new NotFoundException("Acara tidak ditemukan"));

        validateOwnershipOrAdmin(acara, authentication);

        boolean hasSoldTickets = false;
        try {
            String ticketUrl = "http://localhost:8083/api/kategori-tiket/acara/" + id;
            ResponseEntity<List<Map<String, Object>>> res = restTemplate.exchange(
                    ticketUrl,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<Map<String, Object>>>() {
                    });
            if (res.getStatusCode().is2xxSuccessful() && res.getBody() != null) {
                for (Map<String, Object> kt : res.getBody()) {
                    Number t = (Number) kt.get("terjual");
                    if (t != null && t.intValue() > 0) {
                        hasSoldTickets = true;
                        break;
                    }
                }
            }
        } catch (Exception e) {
        }

        if (hasSoldTickets) {
            acara.setStatus("CANCELLED");
            acara.setUpdatedAt(LocalDateTime.now());
            acaraRepository.save(acara);
            throw new BadRequestException(
                    "Konser tidak dapat dihapus permanen karena tiket sudah terjual. Status konser berhasil dialihkan menjadi CANCELLED (Dibatalkan).");
        }

        acara.setDeletedAt(LocalDateTime.now());
        acaraRepository.save(acara);
    }

    @Override
    @Transactional
    public AcaraRes restore(Long id) {
        Acara acara = acaraRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Acara tidak ditemukan"));

        acara.setDeletedAt(null);
        Acara restored = acaraRepository.save(acara);
        return eventMapper.toAcaraRes(restored);
    }

    @Override
    @Transactional
    public AcaraRes uploadPoster(Long id, MultipartFile file) {
        Acara acara = acaraRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new NotFoundException("Acara tidak ditemukan"));

        if (file.isEmpty()) {
            throw new BadRequestException("File poster tidak boleh kosong");
        }

        String fileName = "poster_" + id + "_" + System.currentTimeMillis() + "_"
                + file.getOriginalFilename().replaceAll("[^a-zA-Z0-9.-]", "_");
        acara.setGambarUrl("/uploads/posters/" + fileName);
        acara.setUpdatedAt(LocalDateTime.now());

        Acara updated = acaraRepository.save(acara);
        return eventMapper.toAcaraRes(updated);
    }

    @Override
    @Transactional
    public AcaraRes uploadFlyerPdf(Long id, MultipartFile file) {
        Acara acara = acaraRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new NotFoundException("Acara tidak ditemukan"));

        if (file.isEmpty()) {
            throw new BadRequestException("File flyer tidak boleh kosong");
        }

        String fileName = "flyer_" + id + "_" + System.currentTimeMillis() + "_"
                + file.getOriginalFilename().replaceAll("[^a-zA-Z0-9.-]", "_");
        acara.setUpdatedAt(LocalDateTime.now());

        Acara updated = acaraRepository.save(acara);
        return eventMapper.toAcaraRes(updated);
    }

    @Override
    @Transactional
    public AcaraRes moderate(Long id, String status, Authentication authentication) {
        Acara acara = acaraRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new NotFoundException("Acara tidak ditemukan"));

        acara.setStatus(status.toUpperCase());
        acara.setUpdatedAt(LocalDateTime.now());

        Acara updated = acaraRepository.save(acara);
        return eventMapper.toAcaraRes(updated);
    }

    private void validateOwnershipOrAdmin(Acara acara, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Silakan login terlebih dahulu");
        }

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin) {
            Long currentUserId = (Long) authentication.getCredentials();
            if (currentUserId == null || !currentUserId.equals(acara.getOrganizerId())) {
                throw new AccessDeniedException("Akses ditolak: Anda bukan pemilik acara ini.");
            }
        }
    }

    @Override
    public PageResponse<AcaraRes> getPendingReview(int page, int size, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Silakan login terlebih dahulu");
        }

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin) {
            throw new AccessDeniedException("Hanya Administrator yang berhak mengakses daftar moderasi");
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Acara> pageData = acaraRepository.findByStatusAndDeletedAtIsNull("PENDING_REVIEW", pageable);
        List<AcaraRes> content = pageData.getContent().stream()
                .map(eventMapper::toAcaraRes)
                .map(this::enrichWithRating)
                .collect(Collectors.toList());

        return PageResponse.<AcaraRes>builder()
                .content(content)
                .pageNumber(pageData.getNumber())
                .pageSize(pageData.getSize())
                .totalElements(pageData.getTotalElements())
                .totalPages(pageData.getTotalPages())
                .isLast(pageData.isLast())
                .build();
    }

    @Override
    @Transactional
    public AcaraRes moderateEvent(Long id, String action, String catatan, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Silakan login terlebih dahulu");
        }

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin) {
            throw new AccessDeniedException("Hanya Administrator yang berhak memoderasi event");
        }

        Acara acara = acaraRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new NotFoundException("Acara tidak ditemukan"));

        String act = action != null ? action.trim().toUpperCase() : "APPROVE";
        if ("APPROVE".equals(act)) {
            acara.setStatus("PUBLISHED");
            acara.setCatatanModerator(
                    catatan != null && !catatan.trim().isEmpty() ? catatan.trim() : "Disetujui oleh Administrator");
            acara.setModeratedAt(LocalDateTime.now());
            acara.setUpdatedAt(LocalDateTime.now());

            try {
                Map<String, Object> notif = Map.of(
                        "penggunaId", acara.getOrganizerId(),
                        "judul", "Event Disetujui!",
                        "pesan",
                        "Selamat! Event Anda '" + acara.getNama()
                                + "' telah diverifikasi dan kini PUBLISHED di platform TIKARA.",
                        "tipe", "IN_APP");
                restTemplate.postForEntity("http://localhost:8086/api/notifications/send", notif, Map.class);
            } catch (Exception e) {
                log.warn("Gagal mengirim notif persetujuan event: {}", e.getMessage());
            }
        } else if ("REJECT".equals(act)) {
            if (catatan == null || catatan.trim().length() < 5) {
                throw new BadRequestException("Alasan penolakan event wajib diisi minimal 5 karakter");
            }
            acara.setStatus("REJECTED");
            acara.setCatatanModerator(catatan.trim());
            acara.setModeratedAt(LocalDateTime.now());
            acara.setUpdatedAt(LocalDateTime.now());

            try {
                Map<String, Object> notif = Map.of(
                        "penggunaId", acara.getOrganizerId(),
                        "judul", "Event Ditolak",
                        "pesan",
                        "Mohon maaf, event Anda '" + acara.getNama() + "' tidak disetujui. Catatan Moderator: "
                                + catatan.trim(),
                        "tipe", "IN_APP");
                restTemplate.postForEntity("http://localhost:8086/api/notifications/send", notif, Map.class);
            } catch (Exception e) {
                log.warn("Gagal mengirim notif penolakan event: {}", e.getMessage());
            }
        } else if ("SUSPEND".equals(act)) {
            acara.setStatus("SUSPENDED");
            acara.setCatatanModerator(
                    catatan != null && !catatan.trim().isEmpty() ? catatan.trim() : "Ditangguhkan oleh Administrator");
            acara.setModeratedAt(LocalDateTime.now());
            acara.setUpdatedAt(LocalDateTime.now());

            try {
                Map<String, Object> notif = Map.of(
                        "penggunaId", acara.getOrganizerId(),
                        "judul", "Event Ditangguhkan (Suspended)",
                        "pesan",
                        "Event Anda '" + acara.getNama() + "' telah ditangguhkan oleh Administrator. Catatan: "
                                + (catatan != null ? catatan.trim() : "-"),
                        "tipe", "IN_APP");
                restTemplate.postForEntity("http://localhost:8086/api/notifications/send", notif, Map.class);
            } catch (Exception e) {
                log.warn("Gagal kirim notif suspend: {}", e.getMessage());
            }
        } else {
            throw new BadRequestException("Aksi moderasi tidak valid. Pilih APPROVE, REJECT, atau SUSPEND");
        }

        Acara saved = acaraRepository.save(acara);
        return enrichWithRating(eventMapper.toAcaraRes(saved));
    }

    @Override
    @Transactional
    public AcaraRes resubmitEvent(Long id, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Silakan login terlebih dahulu");
        }

        Acara acara = acaraRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new NotFoundException("Acara tidak ditemukan"));

        validateOwnershipOrAdmin(acara, authentication);

        String currentStatus = acara.getStatus() != null ? acara.getStatus().toUpperCase() : "DRAFT";
        if (!List.of("REJECTED", "DRAFT").contains(currentStatus)) {
            throw new BadRequestException("Hanya event dengan status REJECTED atau DRAFT yang dapat diajukan ulang");
        }

        acara.setStatus("PENDING_REVIEW");
        acara.setCatatanModerator("Menunggu verifikasi ulang oleh Administrator");
        acara.setUpdatedAt(LocalDateTime.now());

        Acara saved = acaraRepository.save(acara);

        try {
            Map<String, Object> adminNotif = Map.of(
                    "penggunaId", 1L,
                    "judul", "Event Diajukan Ulang",
                    "pesan",
                    "Penyelenggara telah mengajukan ulang event '" + saved.getNama() + "' untuk peninjauan moderasi.",
                    "tipe", "IN_APP");
            restTemplate.postForEntity("http://localhost:8086/api/notifications/send", adminNotif, Map.class);
        } catch (Exception ignored) {
        }

        return enrichWithRating(eventMapper.toAcaraRes(saved));
    }

    @Override
    public OrganizerEventDetailRes getEventDetailForOrganizer(Long acaraId, Authentication authentication,
            String authHeader) {
        Acara acara = acaraRepository.findByIdAndDeletedAtIsNull(acaraId)
                .orElseThrow(() -> new NotFoundException("Event tidak ditemukan"));

        Long organizerId = (Long) authentication.getCredentials();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin && !acara.getOrganizerId().equals(organizerId)) {
            throw new AccessDeniedException("Anda tidak memiliki akses ke event ini");
        }

        HttpHeaders headers = new HttpHeaders();
        if (authHeader != null && !authHeader.isBlank()) {
            headers.set("Authorization", authHeader);
        }
        HttpEntity<?> entity = new HttpEntity<>(headers);

        OrganizerEventDetailRes.KategoriStatsItem[] emptyStats = {};
        Integer totalKuota = 0, totalTerjual = 0, totalTersisa = 0, totalUsed = 0;
        Double persenTerjual = 0.0;
        java.util.List<OrganizerEventDetailRes.KategoriStatsItem> perKategoriTiket = new java.util.ArrayList<>();

        try {
            @SuppressWarnings("unchecked")
            ResponseEntity<Map> ticketRes = restTemplate.exchange(
                    "http://localhost:8083/api/tiket/acara/" + acaraId + "/stats",
                    HttpMethod.GET, entity, Map.class);
            if (ticketRes.getStatusCode().is2xxSuccessful() && ticketRes.getBody() != null) {
                Map body = ticketRes.getBody();
                totalKuota = body.get("totalKuota") instanceof Number n ? n.intValue() : 0;
                totalTerjual = body.get("totalTerjual") instanceof Number n ? n.intValue() : 0;
                totalTersisa = body.get("totalTersisa") instanceof Number n ? n.intValue() : 0;
                totalUsed = body.get("totalUsed") instanceof Number n ? n.intValue() : 0;
                persenTerjual = body.get("persentaseTerjual") instanceof Number n ? n.doubleValue() : 0.0;
                if (body.get("perKategori") instanceof java.util.List list) {
                    for (Object item : list) {
                        if (item instanceof Map m) {
                            perKategoriTiket.add(OrganizerEventDetailRes.KategoriStatsItem.builder()
                                    .kategoriId(m.get("kategoriId") instanceof Number n ? n.longValue() : null)
                                    .namaKategori(m.get("namaKategori") != null ? m.get("namaKategori").toString() : "")
                                    .harga(m.get("harga") instanceof Number n ? new BigDecimal(n.toString())
                                            : BigDecimal.ZERO)
                                    .kuota(m.get("kuota") instanceof Number n ? n.intValue() : 0)
                                    .terjual(m.get("terjual") instanceof Number n ? n.intValue() : 0)
                                    .tersisa(m.get("tersisa") instanceof Number n ? n.intValue() : 0)
                                    .digunakan(m.get("digunakan") instanceof Number n ? n.intValue() : 0)
                                    .persentaseTerjual(
                                            m.get("persentaseTerjual") instanceof Number n ? n.doubleValue() : 0.0)
                                    .status(m.get("status") != null ? m.get("status").toString() : "")
                                    .build());
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Failed to fetch ticket stats for acara {}: {}", acaraId, e.getMessage());
        }

        java.util.List<Long> kategoriIds = perKategoriTiket.stream()
                .map(OrganizerEventDetailRes.KategoriStatsItem::getKategoriId)
                .filter(id -> id != null)
                .collect(java.util.stream.Collectors.toList());

        BigDecimal totalRevenue = BigDecimal.ZERO;
        long totalOrder = 0L, totalTiketOrder = 0L;
        java.util.List<OrganizerEventDetailRes.KategoriRevenueItem> revenuePerKategori = new java.util.ArrayList<>();
        try {
            String ids = kategoriIds.stream().map(Object::toString).collect(java.util.stream.Collectors.joining(","));
            String revenueUrl = "http://localhost:8084/api/orders/acara/" + acaraId + "/revenue"
                    + (ids.isBlank() ? "" : "?kategoriIds=" + ids);
            @SuppressWarnings("unchecked")
            ResponseEntity<Map> revenueRes = restTemplate.exchange(revenueUrl, HttpMethod.GET, entity, Map.class);
            if (revenueRes.getStatusCode().is2xxSuccessful() && revenueRes.getBody() != null) {
                Map body = revenueRes.getBody();
                totalRevenue = body.get("totalRevenue") instanceof Number n ? new BigDecimal(n.toString())
                        : BigDecimal.ZERO;
                totalOrder = body.get("totalOrder") instanceof Number n ? n.longValue() : 0L;
                totalTiketOrder = body.get("totalTiket") instanceof Number n ? n.longValue() : 0L;
                if (body.get("perKategori") instanceof java.util.List list) {
                    for (Object item : list) {
                        if (item instanceof Map m) {
                            revenuePerKategori.add(OrganizerEventDetailRes.KategoriRevenueItem.builder()
                                    .kategoriTiketId(
                                            m.get("kategoriTiketId") instanceof Number n ? n.longValue() : null)
                                    .namaKategori(m.get("namaKategori") != null ? m.get("namaKategori").toString() : "")
                                    .jumlahTerjual(m.get("jumlahTerjual") instanceof Number n ? n.intValue() : 0)
                                    .hargaSatuan(m.get("hargaSatuan") instanceof Number n ? new BigDecimal(n.toString())
                                            : BigDecimal.ZERO)
                                    .subtotal(m.get("subtotal") instanceof Number n ? new BigDecimal(n.toString())
                                            : BigDecimal.ZERO)
                                    .build());
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Failed to fetch revenue for acara {}: {}", acaraId, e.getMessage());
        }

        long totalCheckinSukses = 0L, totalCheckinGagal = 0L, totalScan = 0L;
        java.util.List<OrganizerEventDetailRes.TimelineItem> timelineCheckin = new java.util.ArrayList<>();
        try {
            @SuppressWarnings("unchecked")
            ResponseEntity<Map> checkinRes = restTemplate.exchange(
                    "http://localhost:8087/api/checkin/acara/" + acaraId + "/timeline",
                    HttpMethod.GET, entity, Map.class);
            if (checkinRes.getStatusCode().is2xxSuccessful() && checkinRes.getBody() != null) {
                Map body = checkinRes.getBody();
                totalCheckinSukses = body.get("totalCheckinSuccess") instanceof Number n ? n.longValue() : 0L;
                totalCheckinGagal = body.get("totalCheckinFailed") instanceof Number n ? n.longValue() : 0L;
                totalScan = body.get("totalScanCount") instanceof Number n ? n.longValue() : 0L;
                if (body.get("timelinePerJam") instanceof java.util.List list) {
                    for (Object item : list) {
                        if (item instanceof Map m) {
                            timelineCheckin.add(OrganizerEventDetailRes.TimelineItem.builder()
                                    .jam(m.get("jam") != null ? m.get("jam").toString() : "")
                                    .sukses(m.get("sukses") instanceof Number n ? n.longValue() : 0L)
                                    .gagal(m.get("gagal") instanceof Number n ? n.longValue() : 0L)
                                    .build());
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Failed to fetch checkin stats for acara {}: {}", acaraId, e.getMessage());
        }

        Double rataRating = ulasanRepository.findAverageRatingByAcaraId(acaraId);
        Long jumlahUlasan = ulasanRepository.countByAcaraIdAndDeletedAtIsNull(acaraId);

        return OrganizerEventDetailRes.builder()
                .acaraId(acara.getId())
                .namaAcara(acara.getNama())
                .venue(acara.getVenue())
                .kota(acara.getKota())
                .status(acara.getStatus())
                .gambarUrl(acara.getGambarUrl())
                .tanggalMulai(acara.getTanggalMulai() != null ? acara.getTanggalMulai().toString() : null)
                .tanggalSelesai(acara.getTanggalSelesai() != null ? acara.getTanggalSelesai().toString() : null)
                .totalKuota(totalKuota)
                .totalTerjual(totalTerjual)
                .totalTersisa(totalTersisa)
                .totalUsed(totalUsed)
                .persentaseTerjual(persenTerjual)
                .perKategoriTiket(perKategoriTiket)
                .totalRevenue(totalRevenue)
                .totalOrder(totalOrder)
                .totalTiketOrder(totalTiketOrder)
                .revenuePerKategori(revenuePerKategori)
                .totalCheckinSukses(totalCheckinSukses)
                .totalCheckinGagal(totalCheckinGagal)
                .totalScan(totalScan)
                .timelineCheckin(timelineCheckin)
                .rataRating(rataRating != null ? Math.round(rataRating * 10.0) / 10.0 : 0.0)
                .jumlahUlasan(jumlahUlasan != null ? jumlahUlasan : 0L)
                .build();
    }
}
