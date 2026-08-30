package com.tiketkonser.queue_service.service.impl;

import com.tiketkonser.queue_service.entity.AntreanPembeli;
import com.tiketkonser.queue_service.handler.ConflictException;
import com.tiketkonser.queue_service.handler.NotFoundException;
import com.tiketkonser.queue_service.payload.req.JoinQueueReq;
import com.tiketkonser.queue_service.payload.res.QueueStatusRes;
import com.tiketkonser.queue_service.repository.AntreanPembeliRepository;
import com.tiketkonser.queue_service.service.QueueService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class QueueServiceImpl implements QueueService {

    private final AntreanPembeliRepository antreanPembeliRepository;

    @Override
    @Transactional
    public QueueStatusRes joinQueue(JoinQueueReq request, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Silakan login terlebih dahulu");
        }

        Long userId = (Long) authentication.getCredentials();
        if (userId == null) userId = 1L;

        Optional<AntreanPembeli> existing = antreanPembeliRepository.findByAcaraIdAndPenggunaId(request.getAcaraId(), userId);
        if (existing.isPresent()) {
            AntreanPembeli a = existing.get();
            return buildStatusRes(a);
        }

        Integer maxNo = antreanPembeliRepository.findMaxNomorAntreanByAcaraId(request.getAcaraId());
        int nextNo = (maxNo != null) ? maxNo + 1 : 1;

        AntreanPembeli antrean = AntreanPembeli.builder()
                .acaraId(request.getAcaraId())
                .penggunaId(userId)
                .nomorAntrean(nextNo)
                .status("WAITING")
                .masukAt(LocalDateTime.now())
                .build();

        AntreanPembeli saved = antreanPembeliRepository.save(antrean);
        return buildStatusRes(saved);
    }

    @Override
    public QueueStatusRes getQueueStatus(Long acaraId, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Silakan login terlebih dahulu");
        }

        Long userId = (Long) authentication.getCredentials();
        if (userId == null) userId = 1L;

        AntreanPembeli antrean = antreanPembeliRepository.findByAcaraIdAndPenggunaId(acaraId, userId)
                .orElseThrow(() -> new NotFoundException("Anda belum terdaftar dalam antrean acara ini"));

        return buildStatusRes(antrean);
    }

    @Override
    @Transactional
    public List<QueueStatusRes> processNextBatch(Long acaraId, int batchSize, Authentication authentication) {
        validateAdminOrOrganizer(authentication);

        List<AntreanPembeli> waitingList = antreanPembeliRepository
                .findByAcaraIdAndStatusOrderByNomorAntreanAsc(acaraId, "WAITING", PageRequest.of(0, batchSize));

        List<QueueStatusRes> result = new ArrayList<>();
        for (AntreanPembeli a : waitingList) {
            a.setStatus("SERVING");
            a.setDiprosesAt(LocalDateTime.now());
            antreanPembeliRepository.save(a);
            result.add(buildStatusRes(a));
        }

        return result;
    }

    private QueueStatusRes buildStatusRes(AntreanPembeli antrean) {
        long aheadCount = antreanPembeliRepository.countByAcaraIdAndNomorAntreanLessThanAndStatus(
                antrean.getAcaraId(), antrean.getNomorAntrean(), "WAITING"
        );
        long totalWaiting = antreanPembeliRepository.countByAcaraIdAndStatus(antrean.getAcaraId(), "WAITING");

        int posisi = "WAITING".equals(antrean.getStatus()) ? (int) aheadCount + 1 : 0;
        int estimasi = (int) Math.ceil((double) posisi / 5); // Estimasi 5 user per menit

        return QueueStatusRes.builder()
                .acaraId(antrean.getAcaraId())
                .penggunaId(antrean.getPenggunaId())
                .nomorAntrean(antrean.getNomorAntrean())
                .posisiAntrean(posisi)
                .totalMenunggu(totalWaiting)
                .status(antrean.getStatus())
                .estimasiMenit(estimasi)
                .masukAt(antrean.getMasukAt())
                .build();
    }

    private void validateAdminOrOrganizer(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Silakan login terlebih dahulu");
        }

        boolean hasPermission = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_PENYELENGGARA"));

        if (!hasPermission) {
            throw new AccessDeniedException("Hanya ADMIN / PENYELENGGARA yang dapat memproses antrean");
        }
    }
}