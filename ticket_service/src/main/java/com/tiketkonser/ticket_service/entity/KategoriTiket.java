package com.tiketkonser.ticket_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "kategori_tiket")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KategoriTiket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "acara_id", nullable = false)
    private Long acaraId; // Referensi ID ke event-service

    @Column(name = "nama_kategori", nullable = false)
    private String namaKategori;

    @Column(name = "harga", nullable = false, precision = 12, scale = 2)
    private BigDecimal harga;

    @Column(name = "kuota", nullable = false)
    private Integer kuota;

    @Column(name = "terjual", nullable = false)
    private Integer terjual;

    @Column(name = "direservasi", nullable = false)
    private Integer direservasi;

    @Column(name = "mulai_jual", nullable = false)
    private LocalDateTime mulaiJual;

    @Column(name = "selesai_jual", nullable = false)
    private LocalDateTime selesaiJual;

    @Column(name = "status", nullable = false)
    private String status; // AVAILABLE, SOLD_OUT, ENDED

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}