package com.tiketkonser.checkin_service.service.impl;

import com.tiketkonser.checkin_service.entity.RiwayatCheckin;
import com.tiketkonser.checkin_service.handler.BadRequestException;
import com.tiketkonser.checkin_service.payload.req.ScanTicketReq;
import com.tiketkonser.checkin_service.payload.res.CheckinStatsRes;
import com.tiketkonser.checkin_service.payload.res.CheckinTimelineRes;
import com.tiketkonser.checkin_service.payload.res.PageResponse;
import com.tiketkonser.checkin_service.payload.res.ScanTicketRes;
import com.tiketkonser.checkin_service.repository.RiwayatCheckinRepository;
import com.tiketkonser.checkin_service.service.CheckinService;
import com.tiketkonser.checkin_service.utility.CheckinMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CheckinServiceImpl implements CheckinService {

    private static final String TICKET_SERVICE_URL = "http://localhost:8083";

    private final RiwayatCheckinRepository riwayatCheckinRepository;
    private final CheckinMapper checkinMapper;
    private final RestTemplate restTemplate;

    @Override
    public ScanTicketRes scanTicket(ScanTicketReq request, Authentication authentication) {
        return scanTicket(request, authentication, null);
    }

    @Override
    public ScanTicketRes scanTicket(ScanTicketReq request, Authentication authentication, String authHeader) {
        validateOfficerOrAdmin(authentication);

        Long petugasId = (Long) authentication.getCredentials();
        if (petugasId == null)
            petugasId = 1L;

        HttpHeaders headers = new HttpHeaders();
        if (authHeader != null && !authHeader.isBlank()) {
            headers.set(HttpHeaders.AUTHORIZATION, authHeader);
        }
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Void> httpEntity = new HttpEntity<>(headers);

        String targetUrl = TICKET_SERVICE_URL + "/api/tiket/check-in/" + request.getKodeQr();
        if (request.getAcaraId() != null) {
            targetUrl += "?acaraId=" + request.getAcaraId();
        }

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    targetUrl,
                    HttpMethod.POST,
                    httpEntity,
                    Map.class);

            Map<String, Object> body = response.getBody();
            Long realTiketId = 0L;
            if (body != null && body.get("id") != null) {
                realTiketId = Long.valueOf(body.get("id").toString());
            }

            RiwayatCheckin log = RiwayatCheckin.builder()
                    .acaraId(request.getAcaraId())
                    .tiketId(realTiketId)
                    .kodeQr(request.getKodeQr())
                    .petugasId(petugasId)
                    .lokasiGate(request.getLokasiGate())
                    .statusCheckin("SUCCESS")
                    .waktuCheckin(LocalDateTime.now())
                    .build();

            RiwayatCheckin saved = riwayatCheckinRepository.save(log);
            String successMessage = "Berhasil: E-Tiket valid! Pengunjung dipersilakan masuk venue.";
            return checkinMapper.toScanTicketRes(saved, successMessage);

        } catch (HttpStatusCodeException e) {
            String errorMsg = extractErrorMessage(e);
            String statusCheckin = "FAILED_INVALID";
            if (errorMsg.toLowerCase().contains("sudah pernah digunakan")) {
                statusCheckin = "FAILED_ALREADY_USED";
            } else if (errorMsg.toLowerCase().contains("bukan untuk acara")) {
                statusCheckin = "FAILED_INVALID_EVENT";
            }

            RiwayatCheckin failLog = RiwayatCheckin.builder()
                    .acaraId(request.getAcaraId())
                    .tiketId(0L)
                    .kodeQr(request.getKodeQr())
                    .petugasId(petugasId)
                    .lokasiGate(request.getLokasiGate())
                    .statusCheckin(statusCheckin)
                    .waktuCheckin(LocalDateTime.now())
                    .build();

            riwayatCheckinRepository.save(failLog);
            throw new BadRequestException(errorMsg);
        } catch (Exception e) {
            RiwayatCheckin failLog = RiwayatCheckin.builder()
                    .acaraId(request.getAcaraId())
                    .tiketId(0L)
                    .kodeQr(request.getKodeQr())
                    .petugasId(petugasId)
                    .lokasiGate(request.getLokasiGate())
                    .statusCheckin("FAILED_ERROR")
                    .waktuCheckin(LocalDateTime.now())
                    .build();

            riwayatCheckinRepository.save(failLog);
            throw new BadRequestException("Gagal menghubungi ticket_service: " + e.getMessage());
        }
    }

    private String extractErrorMessage(HttpStatusCodeException e) {
        try {
            String responseBody = e.getResponseBodyAsString();
            if (responseBody != null && !responseBody.isBlank()) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode node = mapper.readTree(responseBody);
                if (node.has("message") && !node.get("message").asText().isBlank()) {
                    return node.get("message").asText();
                }
            }
        } catch (Exception ignored) {
        }
        return e.getStatusText() != null && !e.getStatusText().isBlank() ? e.getStatusText() : "Validasi tiket gagal";
    }

    @Override
    public PageResponse<ScanTicketRes> getHistoryByAcaraId(Long acaraId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<RiwayatCheckin> pageData = riwayatCheckinRepository.findByAcaraIdOrderByWaktuCheckinDesc(acaraId,
                pageable);

        List<ScanTicketRes> content = pageData.getContent().stream()
                .map(log -> checkinMapper.toScanTicketRes(log, "Riwayat Check-in"))
                .collect(Collectors.toList());

        return PageResponse.<ScanTicketRes>builder()
                .content(content)
                .pageNumber(pageData.getNumber())
                .pageSize(pageData.getSize())
                .totalElements(pageData.getTotalElements())
                .totalPages(pageData.getTotalPages())
                .isLast(pageData.isLast())
                .build();
    }

    @Override
    public CheckinStatsRes getStatsByAcaraId(Long acaraId) {
        long success = riwayatCheckinRepository.countByAcaraIdAndStatusCheckin(acaraId, "SUCCESS");
        long total = riwayatCheckinRepository.countByAcaraId(acaraId);
        long failed = total - success;
        if (failed < 0)
            failed = 0;

        return CheckinStatsRes.builder()
                .acaraId(acaraId)
                .totalCheckinSuccess(success)
                .totalCheckinFailed(failed)
                .totalScanCount(total)
                .build();
    }

    private void validateOfficerOrAdmin(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Silakan login terlebih dahulu");
        }

        boolean hasPermission = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_PENYELENGGARA"));

        if (!hasPermission) {
            throw new AccessDeniedException("Hanya Panitia/Admin yang berhak melakukan pemindaian check-in");
        }
    }

    @Override
    public CheckinTimelineRes getTimelineByAcaraId(Long acaraId) {
        List<com.tiketkonser.checkin_service.entity.RiwayatCheckin> records = riwayatCheckinRepository
                .findAllByAcaraIdOrderByWaktuAsc(acaraId);

        long totalSuccess = records.stream().filter(r -> "SUCCESS".equalsIgnoreCase(r.getStatusCheckin())).count();
        long totalFailed = records.stream().filter(r -> !"SUCCESS".equalsIgnoreCase(r.getStatusCheckin())).count();

        // Group by hour
        Map<String, long[]> byHour = new java.util.TreeMap<>();
        for (var r : records) {
            if (r.getWaktuCheckin() != null) {
                String jam = String.format("%02d:00", r.getWaktuCheckin().getHour());
                byHour.computeIfAbsent(jam, k -> new long[] { 0, 0 });
                if ("SUCCESS".equalsIgnoreCase(r.getStatusCheckin())) {
                    byHour.get(jam)[0]++;
                } else {
                    byHour.get(jam)[1]++;
                }
            }
        }

        List<CheckinTimelineRes.TimelineItem> timeline = byHour.entrySet().stream()
                .map(e -> CheckinTimelineRes.TimelineItem.builder()
                        .jam(e.getKey())
                        .sukses(e.getValue()[0])
                        .gagal(e.getValue()[1])
                        .build())
                .collect(java.util.stream.Collectors.toList());

        return CheckinTimelineRes.builder()
                .acaraId(acaraId)
                .totalCheckinSuccess(totalSuccess)
                .totalCheckinFailed(totalFailed)
                .totalScanCount((long) records.size())
                .timelinePerJam(timeline)
                .build();
    }
}
