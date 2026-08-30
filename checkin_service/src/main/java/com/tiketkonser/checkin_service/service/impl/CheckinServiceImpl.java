package com.tiketkonser.checkin_service.service.impl;

import com.tiketkonser.checkin_service.entity.RiwayatCheckin;
import com.tiketkonser.checkin_service.handler.BadRequestException;
import com.tiketkonser.checkin_service.payload.req.ScanTicketReq;
import com.tiketkonser.checkin_service.payload.res.CheckinStatsRes;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CheckinServiceImpl implements CheckinService {

    private final RiwayatCheckinRepository riwayatCheckinRepository;
    private final CheckinMapper checkinMapper;

    @Override
    @Transactional
    public ScanTicketRes scanTicket(ScanTicketReq request, Authentication authentication) {
        validateOfficerOrAdmin(authentication);

        Long petugasId = (Long) authentication.getCredentials();
        if (petugasId == null) petugasId = 1L;

        boolean alreadyUsed = riwayatCheckinRepository.existsByKodeQrAndStatusCheckin(request.getKodeQr(), "SUCCESS");

        String statusCheckin;
        String message;
        if (alreadyUsed) {
            statusCheckin = "FAILED_ALREADY_USED";
            message = "Gagal: E-Tiket ini sudah pernah dipindai untuk check-in sebelumnya!";
        } else {
            statusCheckin = "SUCCESS";
            message = "Berhasil: E-Tiket valid! Pengunjung dipersilakan masuk venue.";
        }

        RiwayatCheckin log = RiwayatCheckin.builder()
                .acaraId(request.getAcaraId())
                .tiketId(1L) // Referensi ID tiket dari ticket-service
                .kodeQr(request.getKodeQr())
                .petugasId(petugasId)
                .lokasiGate(request.getLokasiGate())
                .statusCheckin(statusCheckin)
                .waktuCheckin(LocalDateTime.now())
                .build();

        RiwayatCheckin saved = riwayatCheckinRepository.save(log);

        if (alreadyUsed) {
            throw new BadRequestException(message);
        }

        return checkinMapper.toScanTicketRes(saved, message);
    }

    @Override
    public PageResponse<ScanTicketRes> getHistoryByAcaraId(Long acaraId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<RiwayatCheckin> pageData = riwayatCheckinRepository.findByAcaraIdOrderByWaktuCheckinDesc(acaraId, pageable);

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
        long failed = riwayatCheckinRepository.countByAcaraIdAndStatusCheckin(acaraId, "FAILED_ALREADY_USED");
        long total = riwayatCheckinRepository.countByAcaraId(acaraId);

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
}