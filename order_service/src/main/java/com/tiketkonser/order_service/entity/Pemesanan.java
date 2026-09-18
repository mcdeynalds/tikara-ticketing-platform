package com.tiketkonser.order_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pemesanan")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Pemesanan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "pengguna_id", nullable = false)
    private Long penggunaId;

    @Column(name = "nomor_pesanan", nullable = false, unique = true)
    private String nomorPesanan;

    @Column(name = "subtotal_tiket", precision = 12, scale = 2)
    private BigDecimal subtotalTiket;

    @Column(name = "biaya_layanan", precision = 12, scale = 2)
    private BigDecimal biayaLayanan;

    @Column(name = "biaya_admin", precision = 12, scale = 2)
    private BigDecimal biayaAdmin;

    @Column(name = "diskon", precision = 12, scale = 2)
    private BigDecimal diskon;

    @Column(name = "kode_promo")
    private String kodePromo;

    @Column(name = "total_harga", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalHarga;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "tenggat_pembayaran", nullable = false)
    private LocalDateTime tenggatPembayaran;

    @OneToMany(mappedBy = "pemesanan", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<DetailPemesanan> detailPemesananList = new ArrayList<>();

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}