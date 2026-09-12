package com.tiketkonser.event_service.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "kategori")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Kategori {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "nama_kategori", nullable = false, unique = true) private String namaKategori;
    @Column(name = "created_at", nullable = false) private LocalDateTime createdAt;
    @Column(name = "updated_at", nullable = false) private LocalDateTime updatedAt;
}
