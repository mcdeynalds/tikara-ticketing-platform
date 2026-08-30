package com.tiketkonser.order_service.utility;

import com.tiketkonser.order_service.entity.DetailPemesanan;
import com.tiketkonser.order_service.entity.Pemesanan;
import com.tiketkonser.order_service.payload.res.DetailPemesananRes;
import com.tiketkonser.order_service.payload.res.PemesananRes;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class OrderMapper {

    public DetailPemesananRes toDetailPemesananRes(DetailPemesanan detail) {
        if (detail == null) return null;
        return DetailPemesananRes.builder()
                .id(detail.getId())
                .kategoriTiketId(detail.getKategoriTiketId())
                .jumlah(detail.getJumlah())
                .hargaSatuan(detail.getHargaSatuan())
                .subtotal(detail.getSubtotal())
                .build();
    }

    public PemesananRes toPemesananRes(Pemesanan pemesanan) {
        if (pemesanan == null) return null;
        return PemesananRes.builder()
                .id(pemesanan.getId())
                .penggunaId(pemesanan.getPenggunaId())
                .nomorPesanan(pemesanan.getNomorPesanan())
                .totalHarga(pemesanan.getTotalHarga())
                .status(pemesanan.getStatus())
                .tenggatPembayaran(pemesanan.getTenggatPembayaran())
                .items(pemesanan.getDetailPemesananList() != null ?
                        pemesanan.getDetailPemesananList().stream().map(this::toDetailPemesananRes).collect(Collectors.toList()) : null)
                .createdAt(pemesanan.getCreatedAt())
                .updatedAt(pemesanan.getUpdatedAt())
                .build();
    }
}