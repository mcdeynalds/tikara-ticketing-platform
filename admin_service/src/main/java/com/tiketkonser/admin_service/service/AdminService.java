package com.tiketkonser.admin_service.service;

import com.tiketkonser.admin_service.payload.req.CreateAuditLogReq;
import com.tiketkonser.admin_service.payload.res.AuditLogRes;
import com.tiketkonser.admin_service.payload.res.DashboardSummaryRes;
import com.tiketkonser.admin_service.payload.res.LaporanPenjualanRes;
import com.tiketkonser.admin_service.payload.res.PageResponse;
import org.springframework.security.core.Authentication;

public interface AdminService {
    AuditLogRes createAuditLog(CreateAuditLogReq request);
    PageResponse<AuditLogRes> getAllAuditLogs(Authentication authentication, int page, int size);
    DashboardSummaryRes getDashboardSummary(Authentication authentication);
    PageResponse<LaporanPenjualanRes> getSalesReport(Authentication authentication, int page, int size);
}