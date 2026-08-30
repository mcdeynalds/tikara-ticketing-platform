package com.tiketkonser.order_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "detail_pemesanan")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DetailPemesanan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pemesanan_id", nullable = false)
    private Pemesanan pemesanan;

    @Column(name = "kategori_tiket_id", nullable = false)
    private Long kategoriTiketId; // Referensi ID ke ticket-service

    @Column(name = "jumlah", nullable = false)
    private Integer jumlah;

    @Column(name = "harga_satuan", nullable = false, precision = 12, scale = 2)
    private BigDecimal hargaSatuan;

    @Column(name = "subtotal", nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;
}