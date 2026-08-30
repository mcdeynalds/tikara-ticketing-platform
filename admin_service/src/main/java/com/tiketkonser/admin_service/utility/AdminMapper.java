package com.tiketkonser.admin_service.utility;

import com.tiketkonser.admin_service.entity.AuditLog;
import com.tiketkonser.admin_service.entity.LaporanPenjualan;
import com.tiketkonser.admin_service.payload.res.AuditLogRes;
import com.tiketkonser.admin_service.payload.res.LaporanPenjualanRes;
import org.springframework.stereotype.Component;

@Component
public class AdminMapper {

    public AuditLogRes toAuditLogRes(AuditLog entity) {
        if (entity == null) return null;
        return AuditLogRes.builder()
                .id(entity.getId())
                .penggunaId(entity.getPenggunaId())
                .aksi(entity.getAksi())
                .modul(entity.getModul())
                .detail(entity.getDetail())
                .ipAddress(entity.getIpAddress())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public LaporanPenjualanRes toLaporanPenjualanRes(LaporanPenjualan entity) {
        if (entity == null) return null;
        return LaporanPenjualanRes.builder()
                .id(entity.getId())
                .acaraId(entity.getAcaraId())
                .totalTiketTerjual(entity.getTotalTiketTerjual())
                .totalPendapatan(entity.getTotalPendapatan())
                .periode(entity.getPeriode())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}