package com.tiketkonser.ticket_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "tiket")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Tiket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kategori_tiket_id", nullable = false)
    private KategoriTiket kategoriTiket;

    @Column(name = "detail_pemesanan_id")
    private Long detailPemesananId;

    @Column(name = "pengguna_id", nullable = false)
    private Long penggunaId;

    @Column(name = "kode_qr", nullable = false, unique = true)
    private String kodeQr;

    @Column(name = "status", nullable = false)
    private String status; // ISSUED, USED, CANCELLED

    @Column(name = "diterbitkan_at", nullable = false)
    private LocalDateTime diterbitkanAt;

    @Column(name = "digunakan_at")
    private LocalDateTime digunakanAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}