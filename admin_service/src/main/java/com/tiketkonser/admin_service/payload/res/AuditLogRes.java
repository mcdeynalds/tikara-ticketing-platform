package com.tiketkonser.admin_service.payload.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogRes {
    private Long id;
    private Long penggunaId;
    private String aksi;
    private String modul;
    private String detail;
    private String ipAddress;
    private LocalDateTime createdAt;
}