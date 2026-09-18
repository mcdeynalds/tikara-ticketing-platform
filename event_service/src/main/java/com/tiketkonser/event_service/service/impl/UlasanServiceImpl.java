package com.tiketkonser.event_service.service.impl;

import com.tiketkonser.event_service.entity.Acara;
import com.tiketkonser.event_service.entity.Ulasan;
import com.tiketkonser.event_service.handler.BadRequestException;
import com.tiketkonser.event_service.handler.ConflictException;
import com.tiketkonser.event_service.handler.NotFoundException;
import com.tiketkonser.event_service.payload.req.UlasanReq;
import com.tiketkonser.event_service.payload.res.PageResponse;
import com.tiketkonser.event_service.payload.res.UlasanRes;
import com.tiketkonser.event_service.repository.AcaraRepository;
import com.tiketkonser.event_service.repository.UlasanRepository;
import com.tiketkonser.event_service.service.UlasanService;
import com.tiketkonser.event_service.utility.EventMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UlasanServiceImpl implements UlasanService {

    private final UlasanRepository ulasanRepository;
    private final AcaraRepository acaraRepository;
    private final EventMapper eventMapper;
    private final RestTemplate restTemplate;

    @Override
    @Transactional
    public UlasanRes create(Long acaraId, UlasanReq request, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Silakan login terlebih dahulu");
        }

        Long userId = (Long) authentication.getCredentials();
        String userEmail = authentication.getName();
        if (userId == null) {
            throw new AccessDeniedException("Identitas pengguna tidak valid");
        }

        Acara acara = acaraRepository.findByIdAndDeletedAtIsNull(acaraId)
                .orElseThrow(() -> new NotFoundException("Acara tidak ditemukan"));

        if (acara.getTanggalMulai() != null && acara.getTanggalMulai().isAfter(LocalDateTime.now())) {
            throw new BadRequestException("Ulasan hanya dapat diberikan setelah konser dimulai.");
        }

        if (ulasanRepository.existsByAcaraIdAndPenggunaId(acaraId, userId)) {
            throw new ConflictException("Anda sudah pernah memberikan ulasan untuk konser ini.");
        }

        boolean hasTicket = false;
        try {
            String ticketCheckUrl = "http://localhost:8083/api/tiket/has-ticket?penggunaId=" + userId + "&acaraId="
                    + acaraId;
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    ticketCheckUrl,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<Map<String, Object>>() {
                    });
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Object ht = response.getBody().get("hasTicket");
                if (ht instanceof Boolean) {
                    hasTicket = (Boolean) ht;
                }
            }
        } catch (Exception e) {
            log.warn("Tidak dapat memeriksa kepemilikan tiket di ticket_service: {}", e.getMessage());
        }

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!hasTicket && !isAdmin) {
            throw new BadRequestException(
                    "Anda belum memiliki tiket untuk konser ini. Ulasan hanya dapat diberikan oleh pembeli tiket resmi TIKARA.");
        }

        Ulasan ulasan = Ulasan.builder()
                .acara(acara)
                .penggunaId(userId)
                .penggunaNama(userEmail)
                .rating(request.getRating())
                .komentar(request.getKomentar() != null ? request.getKomentar().trim() : null)
                .createdAt(LocalDateTime.now())
                .build();

        Ulasan saved = ulasanRepository.save(ulasan);

        try {
            Map<String, Object> notifReq = Map.of(
                    "penggunaId", userId,
                    "judul", "Ulasan Terkirim!",
                    "pesan",
                    "Terima kasih telah memberikan ulasan untuk konser " + acara.getNama() + " (" + request.getRating()
                            + " Bintang).",
                    "tipe", "IN_APP");
            restTemplate.postForEntity("http://localhost:8086/api/notifications/send", notifReq, Map.class);
        } catch (Exception e) {
            log.warn("Gagal mengirim notifikasi ulasan: {}", e.getMessage());
        }

        return eventMapper.toUlasanRes(saved);
    }

    @Override
    public PageResponse<UlasanRes> getByAcaraId(Long acaraId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Ulasan> ulasanPage = ulasanRepository.findByAcaraId(acaraId, pageable);

        List<UlasanRes> content = ulasanPage.getContent().stream()
                .map(eventMapper::toUlasanRes)
                .collect(Collectors.toList());

        return PageResponse.<UlasanRes>builder()
                .content(content)
                .pageNumber(ulasanPage.getNumber())
                .pageSize(ulasanPage.getSize())
                .totalElements(ulasanPage.getTotalElements())
                .totalPages(ulasanPage.getTotalPages())
                .isLast(ulasanPage.isLast())
                .build();
    }

    @Override
    public Map<String, Object> checkEligibility(Long acaraId, Authentication authentication) {
        Map<String, Object> result = new HashMap<>();

        if (authentication == null || !authentication.isAuthenticated()) {
            result.put("canReview", false);
            result.put("reason", "NOT_LOGGED_IN");
            result.put("message", "Silakan login untuk memberikan ulasan.");
            return result;
        }

        Long userId = (Long) authentication.getCredentials();
        if (userId == null) {
            result.put("canReview", false);
            result.put("reason", "INVALID_USER");
            result.put("message", "Identitas login tidak valid.");
            return result;
        }

        Acara acara = acaraRepository.findByIdAndDeletedAtIsNull(acaraId).orElse(null);
        if (acara == null) {
            result.put("canReview", false);
            result.put("reason", "EVENT_NOT_FOUND");
            result.put("message", "Konser tidak ditemukan.");
            return result;
        }

        boolean eventStarted = acara.getTanggalMulai() != null && !acara.getTanggalMulai().isAfter(LocalDateTime.now());
        boolean alreadyReviewed = ulasanRepository.existsByAcaraIdAndPenggunaId(acaraId, userId);

        boolean hasTicket = false;
        try {
            String ticketCheckUrl = "http://localhost:8083/api/tiket/has-ticket?penggunaId=" + userId + "&acaraId="
                    + acaraId;
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    ticketCheckUrl,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<Map<String, Object>>() {
                    });
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Object ht = response.getBody().get("hasTicket");
                if (ht instanceof Boolean) {
                    hasTicket = (Boolean) ht;
                }
            }
        } catch (Exception ignored) {
        }

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (isAdmin)
            hasTicket = true;

        boolean canReview = eventStarted && !alreadyReviewed && hasTicket;

        String reason = "OK";
        String message = "Anda dapat memberikan ulasan untuk konser ini.";
        if (alreadyReviewed) {
            reason = "ALREADY_REVIEWED";
            message = "Anda sudah pernah memberikan ulasan untuk konser ini.";
        } else if (!eventStarted) {
            reason = "EVENT_NOT_STARTED";
            message = "Ulasan baru dapat diberikan setelah konser dimulai.";
        } else if (!hasTicket) {
            reason = "NO_TICKET";
            message = "Ulasan hanya dapat diberikan oleh penonton yang memiliki tiket resmi konser ini.";
        }

        result.put("canReview", canReview);
        result.put("reason", reason);
        result.put("message", message);
        result.put("hasTicket", hasTicket);
        result.put("alreadyReviewed", alreadyReviewed);
        result.put("eventStarted", eventStarted);

        if (alreadyReviewed) {
            ulasanRepository.findByAcaraIdAndPenggunaId(acaraId, userId).ifPresent(u -> {
                result.put("myRating", u.getRating());
                result.put("myKomentar", u.getKomentar());
                result.put("reviewedAt", u.getCreatedAt());
            });
        }

        return result;
    }

    @Override
    @Transactional
    public void deleteUlasan(Long ulasanId, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Silakan login terlebih dahulu");
        }

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin) {
            throw new AccessDeniedException("Hanya Administrator yang dapat memoderasi atau menghapus ulasan");
        }

        Ulasan ulasan = ulasanRepository.findById(ulasanId)
                .orElseThrow(() -> new NotFoundException("Ulasan tidak ditemukan"));

        ulasanRepository.delete(ulasan);
    }
}
