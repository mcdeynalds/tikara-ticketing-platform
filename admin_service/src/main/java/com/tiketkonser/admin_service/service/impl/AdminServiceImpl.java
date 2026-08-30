package com.tiketkonser.admin_service.service.impl;

import com.tiketkonser.admin_service.entity.AuditLog;
import com.tiketkonser.admin_service.entity.LaporanPenjualan;
import com.tiketkonser.admin_service.payload.req.CreateAuditLogReq;
import com.tiketkonser.admin_service.payload.res.AuditLogRes;
import com.tiketkonser.admin_service.payload.res.DashboardSummaryRes;
import com.tiketkonser.admin_service.payload.res.LaporanPenjualanRes;
import com.tiketkonser.admin_service.payload.res.PageResponse;
import com.tiketkonser.admin_service.repository.AuditLogRepository;
import com.tiketkonser.admin_service.repository.LaporanPenjualanRepository;
import com.tiketkonser.admin_service.service.AdminService;
import com.tiketkonser.admin_service.utility.AdminMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final AuditLogRepository auditLogRepository;
    private final LaporanPenjualanRepository laporanPenjualanRepository;
    private final AdminMapper adminMapper;

    @Override
    @Transactional
    public AuditLogRes createAuditLog(CreateAuditLogReq request) {
        AuditLog log = AuditLog.builder()
                .penggunaId(request.getPenggunaId())
                .aksi(request.getAksi())
                .modul(request.getModul())
                .detail(request.getDetail())
                .ipAddress(request.getIpAddress())
                .createdAt(LocalDateTime.now())
                .build();

        return adminMapper.toAuditLogRes(auditLogRepository.save(log));
    }

    @Override
    public PageResponse<AuditLogRes> getAllAuditLogs(Authentication authentication, int page, int size) {
        validateAdmin(authentication);

        Pageable pageable = PageRequest.of(page, size);
        Page<AuditLog> pageData = auditLogRepository.findAllByOrderByCreatedAtDesc(pageable);

        List<AuditLogRes> content = pageData.getContent().stream()
                .map(adminMapper::toAuditLogRes)
                .collect(Collectors.toList());

        return PageResponse.<AuditLogRes>builder()
                .content(content)
                .pageNumber(pageData.getNumber())
                .pageSize(pageData.getSize())
                .totalElements(pageData.getTotalElements())
                .totalPages(pageData.getTotalPages())
                .isLast(pageData.isLast())
                .build();
    }

    @Override
    public DashboardSummaryRes getDashboardSummary(Authentication authentication) {
        validateAdmin(authentication);

        // Nilai agregasi dashboard executive (Real-time & Mock Data untuk Uji Coba)
        return DashboardSummaryRes.builder()
                .totalPengguna(150L)
                .totalAcara(25L)
                .totalTiketTerjual(1250L)
                .totalOmzetPendapatan(new BigDecimal("375000000.00"))
                .build();
    }

    @Override
    public PageResponse<LaporanPenjualanRes> getSalesReport(Authentication authentication, int page, int size) {
        validateAdmin(authentication);

        Pageable pageable = PageRequest.of(page, size);
        Page<LaporanPenjualan> pageData = laporanPenjualanRepository.findAllByOrderByCreatedAtDesc(pageable);

        List<LaporanPenjualanRes> content = pageData.getContent().stream()
                .map(adminMapper::toLaporanPenjualanRes)
                .collect(Collectors.toList());

        return PageResponse.<LaporanPenjualanRes>builder()
                .content(content)
                .pageNumber(pageData.getNumber())
                .pageSize(pageData.getSize())
                .totalElements(pageData.getTotalElements())
                .totalPages(pageData.getTotalPages())
                .isLast(pageData.isLast())
                .build();
    }

    private void validateAdmin(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Silakan login terlebih dahulu");
        }

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin) {
            throw new AccessDeniedException("Hanya ADMIN yang berhak mengakses dashboard admin ini");
        }
    }
}