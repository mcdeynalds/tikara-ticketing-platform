package com.tiketkonser.ticket_service.payload.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KategoriTiketRes {
    private Long id;
    private Long acaraId;
    private String namaKategori;
    private BigDecimal harga;
    private Integer kuota;
    private Integer terjual;
    private Integer direservasi;
    private Integer sisaStok;
    private LocalDateTime mulaiJual;
    private LocalDateTime selesaiJual;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}