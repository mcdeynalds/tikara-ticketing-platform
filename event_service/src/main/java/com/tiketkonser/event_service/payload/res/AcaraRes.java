package com.tiketkonser.event_service.payload.res;

import lombok.*;
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
    private String catatanModerator;
    private LocalDateTime moderatedAt;
    private Double averageRating;
    private Integer totalUlasan;
    private Set<KategoriRes> meKategori;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
