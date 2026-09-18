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
    private Long acaraId; 

    @Column(name = "tiket_id", nullable = false)
    private Long tiketId; 

    @Column(name = "kode_qr", nullable = false)
    private String kodeQr;

    @Column(name = "petugas_id", nullable = false)
    private Long petugasId; 
    
    @Column(name = "lokasi_gate", nullable = false)
    private String lokasiGate;

    @Column(name = "status_checkin", nullable = false)
    private String statusCheckin; 

    @Column(name = "waktu_checkin", nullable = false)
    private LocalDateTime waktuCheckin;
}