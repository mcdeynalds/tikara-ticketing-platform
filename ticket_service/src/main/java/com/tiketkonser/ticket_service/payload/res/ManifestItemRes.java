package com.tiketkonser.ticket_service.payload.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ManifestItemRes {
    private Long tiketId;
    private String kodeQr;
    private Long penggunaId;
    private Long kategoriTiketId;
    private String namaKategori;
    private BigDecimal harga;
    private String status;         // ISSUED, USED, CANCELLED, REFUNDED, TRANSFERRED
    private String diterbitkanAt;
    private String digunakanAt;
}
