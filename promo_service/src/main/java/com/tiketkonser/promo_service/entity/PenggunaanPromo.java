package com.tiketkonser.promo_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "penggunaan_promo")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PenggunaanPromo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kode_promo_id", nullable = false)
    private KodePromo kodePromo;

    @Column(name = "pengguna_id", nullable = false)
    private Long penggunaId;

    @Column(name = "pemesanan_id", nullable = false)
    private Long pemesananId;

    @Column(name = "potongan_didapat", nullable = false, precision = 12, scale = 2)
    private BigDecimal potonganDidapat;

    @Column(name = "digunakan_at", nullable = false)
    private LocalDateTime digunakanAt;
}