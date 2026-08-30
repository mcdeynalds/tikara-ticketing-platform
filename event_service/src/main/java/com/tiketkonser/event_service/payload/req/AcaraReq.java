package com.tiketkonser.event_service.payload.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AcaraReq {

    @NotBlank(message = "Nama acara wajib diisi")
    @Size(min = 3, max = 255, message = "Nama acara minimal 3 karakter")
    private String nama;

    @NotBlank(message = "Deskripsi acara wajib diisi")
    private String deskripsi;

    @NotBlank(message = "Venue wajib diisi")
    private String venue;

    @NotBlank(message = "Kota wajib diisi")
    private String kota;

    @NotNull(message = "Tanggal mulai wajib diisi")
    private LocalDateTime tanggalMulai;

    @NotNull(message = "Tanggal selesai wajib diisi")
    private LocalDateTime tanggalSelesai;

    private String gambarUrl;

    private Set<Long> kategoriIds;
}