package com.tiketkonser.event_service.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "acara")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Acara {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "organizer_id", nullable = false)
    private Long organizerId;
    @Column(name = "organizer_nama")
    private String organizerNama;
    @Column(name = "nama", nullable = false)
    private String nama;
    @Column(name = "deskripsi", nullable = false, columnDefinition = "TEXT")
    private String deskripsi;
    @Column(name = "venue", nullable = false)
    private String venue;
    @Column(name = "kota", nullable = false)
    private String kota;
    @Column(name = "tanggal_mulai", nullable = false)
    private LocalDateTime tanggalMulai;
    @Column(name = "tanggal_selesai", nullable = false)
    private LocalDateTime tanggalSelesai;
    @Column(name = "status", nullable = false)
    private String status;
    @Column(name = "gambar_url", columnDefinition = "TEXT")
    private String gambarUrl;

    @Column(name = "catatan_moderator", columnDefinition = "TEXT")
    private String catatanModerator;

    @Column(name = "moderated_at")
    private LocalDateTime moderatedAt;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "acara_kategori", joinColumns = @JoinColumn(name = "acara_id"), inverseJoinColumns = @JoinColumn(name = "kategori_id"))
    @Builder.Default
    private Set<Kategori> meKategori = new HashSet<>();

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
