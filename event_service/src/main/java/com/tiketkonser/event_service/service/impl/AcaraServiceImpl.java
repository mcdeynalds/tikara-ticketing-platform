package com.tiketkonser.event_service.service.impl;

import com.tiketkonser.event_service.entity.Acara;
import com.tiketkonser.event_service.entity.Kategori;
import com.tiketkonser.event_service.handler.BadRequestException;
import com.tiketkonser.event_service.handler.NotFoundException;
import com.tiketkonser.event_service.payload.req.AcaraReq;
import com.tiketkonser.event_service.payload.res.AcaraRes;
import com.tiketkonser.event_service.payload.res.EventSalesSummaryRes;
import com.tiketkonser.event_service.payload.res.OrganizerAnalyticsRes;
import com.tiketkonser.event_service.payload.res.PageResponse;
import com.tiketkonser.event_service.repository.AcaraRepository;
import com.tiketkonser.event_service.repository.KategoriRepository;
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

@Service
@RequiredArgsConstructor
public class AcaraServiceImpl implements AcaraService {

    private final AcaraRepository acaraRepository;
    private final KategoriRepository kategoriRepository;
    private final EventMapper eventMapper;
    private final RestTemplate restTemplate;

    @Override
    @Transactional
    public AcaraRes create(AcaraReq request, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Silakan login terlebih dahulu");
        }

        // Authoritative Organizer ID from Security Context (JWT credentials)
        Long organizerId = (Long) authentication.getCredentials();
        String organizerEmail = authentication.getName();
        if (organizerId == null) {
            throw new AccessDeniedException("Identitas penyelenggara tidak valid");
        }

        // Strict Date & Time Validation
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

        // Lifecycle status: DRAFT or PUBLISHED (default PUBLISHED if valid)
        String initialStatus = "PUBLISHED";
        if (request.getStatus() != null && !request.getStatus().trim().isEmpty()) {
            String requested = request.getStatus().trim().toUpperCase();
            if (List.of("DRAFT", "PUBLISHED", "PENDING_REVIEW").contains(requested)) {
                initialStatus = requested;
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
        return eventMapper.toAcaraRes(saved);
    }

    @Override
    public PageResponse<AcaraRes> getAll(String search, String kota, String status, int page, int size, String sortBy, String sortDir) {
        return getAll(search, kota, null, status, page, size, sortBy, sortDir, null);
    }

    @Override
    public PageResponse<AcaraRes> getAll(String search, String kota, Long kategoriId, String status, int page, int size, String sortBy, String sortDir, Authentication authentication) {
        return getAll(search, kota, kategoriId, null, status, page, size, sortBy, sortDir, authentication);
    }

    @Override
    public PageResponse<AcaraRes> getAll(String search, String kota, Long kategoriId, String kategoriNama, String status, int page, int size, String sortBy, String sortDir, Authentication authentication) {
        // Enforce authoritative visibility rule for public catalog:
        // Guest & PEMBELI can ONLY view PUBLISHED or SOLD_OUT events.
        // DRAFT, PENDING_REVIEW, CANCELLED are hidden from public catalog.
        String effectiveStatus = "PUBLISHED";
        if (authentication != null && authentication.isAuthenticated()) {
            boolean isAdmin = authentication.getAuthorities().stream().anyMatch(a ->
                    a.getAuthority().equals("ROLE_ADMIN")
            );
            if (isAdmin) {
                effectiveStatus = status; // Admin can inspect any status in admin moderation
            }
        }

        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Specification<Acara> spec = AcaraSpecification.filterAcara(search, kota, kategoriId, kategoriNama, effectiveStatus);

        Page<Acara> pageData = acaraRepository.findAll(spec, pageable);
        List<AcaraRes> content = pageData.getContent().stream().map(eventMapper::toAcaraRes).collect(Collectors.toList());

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
    public PageResponse<AcaraRes> getMyEvents(String search, String status, int page, int size, String sortBy, String sortDir, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Silakan login terlebih dahulu");
        }

        // Strictly isolated: Only events belonging to the logged-in organizer
        Long organizerId = (Long) authentication.getCredentials();
        if (organizerId == null) {
            throw new AccessDeniedException("Identitas pengguna tidak valid");
        }

        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        // Pass organizerId explicitly to ensure Organizer A never sees Organizer B's events
        Specification<Acara> spec = AcaraSpecification.filterAcara(search, null, null, null, status, organizerId);

        Page<Acara> pageData = acaraRepository.findAll(spec, pageable);
        List<AcaraRes> content = pageData.getContent().stream().map(eventMapper::toAcaraRes).collect(Collectors.toList());

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

        // Fetch all non-deleted events belonging strictly to this organizer
        List<Acara> myEvents = acaraRepository.findAll(AcaraSpecification.filterAcara(null, null, null, null, null, organizerId));

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
            if ("PUBLISHED".equals(st)) eventAktif++;
            else if ("DRAFT".equals(st) || "PENDING_REVIEW".equals(st)) eventDraft++;
            else if ("SOLD_OUT".equals(st)) eventSoldOut++;
            else if ("ENDED".equals(st)) eventSelesai++;

            int evKuota = 0;
            int evTerjual = 0;
            BigDecimal evPendapatan = BigDecimal.ZERO;

            // Safe query to ticket_service on port 8083
            try {
                String ticketUrl = "http://localhost:8083/api/kategori-tiket/acara/" + ev.getId();
                ResponseEntity<List<Map<String, Object>>> res = restTemplate.exchange(
                        ticketUrl,
                        HttpMethod.GET,
                        null,
                        new ParameterizedTypeReference<List<Map<String, Object>>>() {}
                );

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
                        // Source of Truth: Realized Revenue = confirmed sold tickets * price
                        evPendapatan = evPendapatan.add(h.multiply(BigDecimal.valueOf(t)));
                    }
                }
            } catch (Exception e) {
                // ticket_service might be offline or empty for this draft event
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

        // Public visibility rule:
        // DRAFT, PENDING_REVIEW, CANCELLED can only be viewed by owner or Admin.
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

        return eventMapper.toAcaraRes(acara);
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
        if (request.getTanggalMulai() != null) acara.setTanggalMulai(request.getTanggalMulai());
        if (request.getTanggalSelesai() != null) acara.setTanggalSelesai(request.getTanggalSelesai());
        if (request.getStatus() != null && !request.getStatus().trim().isEmpty()) {
            acara.setStatus(request.getStatus().trim().toUpperCase());
        }
        if (request.getGambarUrl() != null) acara.setGambarUrl(request.getGambarUrl());
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

        // Safe Cancellation rule: Check if tickets were already sold
        boolean hasSoldTickets = false;
        try {
            String ticketUrl = "http://localhost:8083/api/kategori-tiket/acara/" + id;
            ResponseEntity<List<Map<String, Object>>> res = restTemplate.exchange(
                    ticketUrl,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<Map<String, Object>>>() {}
            );
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
            // ignore
        }

        if (hasSoldTickets) {
            // Cannot hard-delete event with sold tickets! Must transition to CANCELLED
            acara.setStatus("CANCELLED");
            acara.setUpdatedAt(LocalDateTime.now());
            acaraRepository.save(acara);
            throw new BadRequestException("Konser tidak dapat dihapus permanen karena tiket sudah terjual. Status konser berhasil dialihkan menjadi CANCELLED (Dibatalkan).");
        }

        // If no tickets sold, soft delete
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

        String fileName = "poster_" + id + "_" + System.currentTimeMillis() + "_" + file.getOriginalFilename().replaceAll("[^a-zA-Z0-9.-]", "_");
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

        String fileName = "flyer_" + id + "_" + System.currentTimeMillis() + "_" + file.getOriginalFilename().replaceAll("[^a-zA-Z0-9.-]", "_");
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
}
