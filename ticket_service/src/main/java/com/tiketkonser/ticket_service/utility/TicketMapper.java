package com.tiketkonser.ticket_service.utility;

import com.tiketkonser.ticket_service.entity.KategoriTiket;
import com.tiketkonser.ticket_service.entity.Tiket;
import com.tiketkonser.ticket_service.payload.res.KategoriTiketRes;
import com.tiketkonser.ticket_service.payload.res.TiketRes;
import org.springframework.stereotype.Component;

@Component
public class TicketMapper {

    public KategoriTiketRes toKategoriTiketRes(KategoriTiket entity) {
        if (entity == null) return null;
        int sisaStok = entity.getKuota() - entity.getTerjual() - entity.getDireservasi();
        if (sisaStok < 0) sisaStok = 0;

        return KategoriTiketRes.builder()
                .id(entity.getId())
                .acaraId(entity.getAcaraId())
                .namaKategori(entity.getNamaKategori())
                .harga(entity.getHarga())
                .kuota(entity.getKuota())
                .terjual(entity.getTerjual())
                .direservasi(entity.getDireservasi())
                .sisaStok(sisaStok)
                .mulaiJual(entity.getMulaiJual())
                .selesaiJual(entity.getSelesaiJual())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public TiketRes toTiketRes(Tiket entity) {
        if (entity == null) return null;
        return TiketRes.builder()
                .id(entity.getId())
                .kategoriTiketId(entity.getKategoriTiket() != null ? entity.getKategoriTiket().getId() : null)
                .namaKategoriTiket(entity.getKategoriTiket() != null ? entity.getKategoriTiket().getNamaKategori() : null)
                .detailPemesananId(entity.getDetailPemesananId())
                .penggunaId(entity.getPenggunaId())
                .kodeQr(entity.getKodeQr())
                .status(entity.getStatus())
                .diterbitkanAt(entity.getDiterbitkanAt())
                .digunakanAt(entity.getDigunakanAt())
                .build();
    }
}