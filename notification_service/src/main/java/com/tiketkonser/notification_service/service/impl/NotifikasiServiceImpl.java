package com.tiketkonser.notification_service.service.impl;

import com.tiketkonser.notification_service.entity.Notifikasi;
import com.tiketkonser.notification_service.handler.NotFoundException;
import com.tiketkonser.notification_service.payload.req.SendNotifikasiReq;
import com.tiketkonser.notification_service.payload.res.NotifikasiRes;
import com.tiketkonser.notification_service.payload.res.PageResponse;
import com.tiketkonser.notification_service.repository.NotifikasiRepository;
import com.tiketkonser.notification_service.service.NotifikasiService;
import com.tiketkonser.notification_service.utility.NotificationMapper;
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
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotifikasiServiceImpl implements NotifikasiService {

    private final NotifikasiRepository notifikasiRepository;
    private final NotificationMapper notificationMapper;

    @Override
    @Transactional
    public NotifikasiRes sendNotification(SendNotifikasiReq request) {
        String tipe = request.getTipe() != null ? request.getTipe() : "EMAIL";

        Notifikasi notifikasi = Notifikasi.builder()
                .penggunaId(request.getPenggunaId())
                .judul(request.getJudul())
                .pesan(request.getPesan())
                .tipe(tipe)
                .status("SENT")
                .dibaca(false)
                .createdAt(LocalDateTime.now())
                .build();

        return notificationMapper.toNotifikasiRes(notifikasiRepository.save(notifikasi));
    }

    @Override
    public PageResponse<NotifikasiRes> getMyNotifications(Authentication authentication, int page, int size) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Silakan login terlebih dahulu");
        }

        Long userId = (Long) authentication.getCredentials();
        if (userId == null) userId = 1L;

        Pageable pageable = PageRequest.of(page, size);
        Page<Notifikasi> pageData = notifikasiRepository.findByPenggunaIdOrderByCreatedAtDesc(userId, pageable);

        List<NotifikasiRes> content = pageData.getContent().stream()
                .map(notificationMapper::toNotifikasiRes)
                .collect(Collectors.toList());

        return PageResponse.<NotifikasiRes>builder()
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
    public NotifikasiRes markAsRead(Long id, Authentication authentication) {
        Notifikasi notifikasi = notifikasiRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Notifikasi tidak ditemukan"));

        validateOwnershipOrAdmin(notifikasi.getPenggunaId(), authentication);

        notifikasi.setDibaca(true);
        return notificationMapper.toNotifikasiRes(notifikasiRepository.save(notifikasi));
    }

    @Override
    public Map<String, Object> getUnreadCount(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Silakan login terlebih dahulu");
        }

        Long userId = (Long) authentication.getCredentials();
        if (userId == null) userId = 1L;

        long unreadCount = notifikasiRepository.countByPenggunaIdAndDibacaFalse(userId);
        return Map.of("unreadCount", unreadCount);
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
            throw new AccessDeniedException("Anda tidak memiliki hak akses untuk notifikasi ini");
        }
    }
}