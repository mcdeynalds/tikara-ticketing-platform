package com.tiketkonser.notification_service.payload.req;

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
public class SendNotifikasiReq {

    @NotNull(message = "Pengguna ID wajib diisi")
    private Long penggunaId;

    @NotBlank(message = "Judul notifikasi wajib diisi")
    private String judul;

    @NotBlank(message = "Pesan notifikasi wajib diisi")
    private String pesan;

    private String tipe;
}