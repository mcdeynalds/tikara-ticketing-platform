package com.tiketkonser.admin_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "laporan_penjualan")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LaporanPenjualan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "acara_id", nullable = false)
    private Long acaraId;

    @Column(name = "total_tiket_terjual", nullable = false)
    private Integer totalTiketTerjual;

    @Column(name = "total_pendapatan", nullable = false, precision = 14, scale = 2)
    private BigDecimal totalPendapatan;

    @Column(name = "periode", nullable = false)
    private String periode;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}