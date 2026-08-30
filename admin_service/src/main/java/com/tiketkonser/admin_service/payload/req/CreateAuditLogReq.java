package com.tiketkonser.admin_service.payload.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateAuditLogReq {

    @NotNull(message = "Pengguna ID wajib diisi")
    private Long penggunaId;

    @NotBlank(message = "Aksi wajib diisi")
    private String aksi;

    @NotBlank(message = "Modul wajib diisi")
    private String modul;

    private String detail;
    private String ipAddress;
}