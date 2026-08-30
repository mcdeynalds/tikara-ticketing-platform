package com.tiketkonser.checkin_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "riwayat_checkin")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RiwayatCheckin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "acara_id", nullable = false)
    private Long acaraId; // Referensi ID ke event-service

    @Column(name = "tiket_id", nullable = false)
    private Long tiketId; // Referensi ID ke ticket-service

    @Column(name = "kode_qr", nullable = false)
    private String kodeQr;

    @Column(name = "petugas_id", nullable = false)
    private Long petugasId; // Referensi ID ke user-service

    @Column(name = "lokasi_gate", nullable = false)
    private String lokasiGate;

    @Column(name = "status_checkin", nullable = false)
    private String statusCheckin; // SUCCESS, FAILED_ALREADY_USED, INVALID_QR

    @Column(name = "waktu_checkin", nullable = false)
    private LocalDateTime waktuCheckin;
}