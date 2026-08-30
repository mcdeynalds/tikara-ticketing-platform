package com.tiketkonser.admin_service.controller;

import com.tiketkonser.admin_service.payload.req.CreateAuditLogReq;
import com.tiketkonser.admin_service.payload.res.AuditLogRes;
import com.tiketkonser.admin_service.payload.res.DashboardSummaryRes;
import com.tiketkonser.admin_service.payload.res.LaporanPenjualanRes;
import com.tiketkonser.admin_service.payload.res.PageResponse;
import com.tiketkonser.admin_service.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @PostMapping("/audit-logs")
    public ResponseEntity<AuditLogRes> createAuditLog(@Valid @RequestBody CreateAuditLogReq request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminService.createAuditLog(request));
    }

    @GetMapping("/audit-logs")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PageResponse<AuditLogRes>> getAllAuditLogs(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(adminService.getAllAuditLogs(authentication, page, size));
    }

    @GetMapping("/dashboard/summary")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DashboardSummaryRes> getDashboardSummary(Authentication authentication) {
        return ResponseEntity.ok(adminService.getDashboardSummary(authentication));
    }

    @GetMapping("/reports/sales")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PageResponse<LaporanPenjualanRes>> getSalesReport(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(adminService.getSalesReport(authentication, page, size));
    }
}