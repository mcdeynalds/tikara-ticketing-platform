package com.tiketkonser.queue_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "antrean_pembeli", uniqueConstraints = {
    @UniqueConstraint(name = "uk_acara_pengguna_antrean", columnNames = {"acara_id", "pengguna_id"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AntreanPembeli {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "acara_id", nullable = false)
    private Long acaraId; // Referensi ID ke event-service

    @Column(name = "pengguna_id", nullable = false)
    private Long penggunaId; // Referensi ID ke user-service

    @Column(name = "nomor_antrean", nullable = false)
    private Integer nomorAntrean;

    @Column(name = "status", nullable = false)
    private String status; // WAITING, SERVING, COMPLETED, EXPIRED

    @Column(name = "masuk_at", nullable = false)
    private LocalDateTime masukAt;

    @Column(name = "diproses_at")
    private LocalDateTime diprosesAt;

    @Column(name = "selesai_at")
    private LocalDateTime selesaiAt;
}