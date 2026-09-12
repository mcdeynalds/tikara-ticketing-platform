package com.tiketkonser.event_service.utility;

import com.tiketkonser.event_service.entity.Acara;
import com.tiketkonser.event_service.entity.Kategori;
import com.tiketkonser.event_service.entity.Ulasan;
import com.tiketkonser.event_service.payload.res.AcaraRes;
import com.tiketkonser.event_service.payload.res.KategoriRes;
import com.tiketkonser.event_service.payload.res.UlasanRes;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class EventMapper {

    public KategoriRes toKategoriRes(Kategori kategori) {
        if (kategori == null) return null;
        return KategoriRes.builder()
                .id(kategori.getId())
                .namaKategori(kategori.getNamaKategori())
                .createdAt(kategori.getCreatedAt())
                .updatedAt(kategori.getUpdatedAt())
                .build();
    }

    public AcaraRes toAcaraRes(Acara acara) {
        if (acara == null) return null;
        return AcaraRes.builder()
                .id(acara.getId())
                .organizerId(acara.getOrganizerId())
                .organizerNama(acara.getOrganizerNama())
                .nama(acara.getNama())
                .deskripsi(acara.getDeskripsi())
                .venue(acara.getVenue())
                .kota(acara.getKota())
                .tanggalMulai(acara.getTanggalMulai())
                .tanggalSelesai(acara.getTanggalSelesai())
                .status(acara.getStatus())
                .gambarUrl(acara.getGambarUrl())
                .meKategori(acara.getMeKategori() != null ?
                        acara.getMeKategori().stream().map(this::toKategoriRes).collect(Collectors.toSet()) : null)
                .createdAt(acara.getCreatedAt())
                .updatedAt(acara.getUpdatedAt())
                .build();
    }

    public UlasanRes toUlasanRes(Ulasan ulasan) {
        if (ulasan == null) return null;
        return UlasanRes.builder()
                .id(ulasan.getId())
                .acaraId(ulasan.getAcara() != null ? ulasan.getAcara().getId() : null)
                .acaraNama(ulasan.getAcara() != null ? ulasan.getAcara().getNama() : null)
                .penggunaId(ulasan.getPenggunaId())
                .penggunaNama(ulasan.getPenggunaNama())
                .rating(ulasan.getRating())
                .komentar(ulasan.getKomentar())
                .createdAt(ulasan.getCreatedAt())
                .build();
    }
}
