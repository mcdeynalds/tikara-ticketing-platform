package com.tiketkonser.notification_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifikasi")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notifikasi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "pengguna_id", nullable = false)
    private Long penggunaId; // Referensi ID ke user-service

    @Column(name = "judul", nullable = false)
    private String judul;

    @Column(name = "pesan", nullable = false, columnDefinition = "TEXT")
    private String pesan;

    @Column(name = "tipe", nullable = false)
    private String tipe; // EMAIL, IN_APP, SMS

    @Column(name = "status", nullable = false)
    private String status; // SENT, PENDING, FAILED

    @Column(name = "dibaca", nullable = false)
    private Boolean dibaca;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}