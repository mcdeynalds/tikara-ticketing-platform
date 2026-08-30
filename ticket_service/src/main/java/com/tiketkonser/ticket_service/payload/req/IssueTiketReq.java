package com.tiketkonser.ticket_service.payload.req;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IssueTiketReq {

    @NotNull(message = "Kategori tiket ID wajib diisi")
    private Long kategoriTiketId;

    private Long detailPemesananId;

    @NotNull(message = "Pengguna ID wajib diisi")
    private Long penggunaId;
}