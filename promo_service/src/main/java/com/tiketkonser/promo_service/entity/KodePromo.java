package com.tiketkonser.promo_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "kode_promo")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KodePromo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "kode", nullable = false, unique = true)
    private String kode;

    @Column(name = "persen_diskon", nullable = false, precision = 5, scale = 2)
    private BigDecimal persenDiskon;

    @Column(name = "potongan_maksimal", nullable = false, precision = 12, scale = 2)
    private BigDecimal potonganMaksimal;

    @Column(name = "minimal_transaksi", nullable = false, precision = 12, scale = 2)
    private BigDecimal minimalTransaksi;

    @Column(name = "kuota_promo", nullable = false)
    private Integer kuotaPromo;

    @Column(name = "terpakai", nullable = false)
    private Integer terpakai;

    @Column(name = "mulai_berlaku", nullable = false)
    private LocalDateTime mulaiBerlaku;

    @Column(name = "selesai_berlaku", nullable = false)
    private LocalDateTime selesaiBerlaku;

    @Column(name = "status", nullable = false)
    private String status; // ACTIVE, INACTIVE

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}