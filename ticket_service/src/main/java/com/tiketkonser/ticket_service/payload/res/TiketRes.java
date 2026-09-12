package com.tiketkonser.ticket_service.payload.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TiketRes {
    private Long id;
    private Long acaraId;
    private Long kategoriTiketId;
    private String namaKategoriTiket;
    private Long detailPemesananId;
    private Long penggunaId;
    private String kodeQr;
    private String status;
    private LocalDateTime diterbitkanAt;
    private LocalDateTime digunakanAt;
}