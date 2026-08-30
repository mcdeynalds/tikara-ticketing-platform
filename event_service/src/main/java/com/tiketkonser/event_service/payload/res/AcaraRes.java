package com.tiketkonser.event_service.payload.res;

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
public class AcaraRes {
    private Long id;
    private Long organizerId;
    private String organizerNama;
    private String nama;
    private String deskripsi;
    private String venue;
    private String kota;
    private LocalDateTime tanggalMulai;
    private LocalDateTime tanggalSelesai;
    private String status;
    private String gambarUrl;
    private Set<KategoriRes> meKategori;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}