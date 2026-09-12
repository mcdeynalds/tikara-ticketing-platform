package com.tiketkonser.event_service.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ulasan")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Ulasan {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "acara_id", nullable = false) private Acara acara;
    @Column(name = "pengguna_id", nullable = false) private Long penggunaId;
    @Column(name = "pengguna_nama") private String penggunaNama;
    @Column(name = "rating", nullable = false) private Integer rating;
    @Column(name = "komentar", columnDefinition = "TEXT") private String komentar;
    @Column(name = "created_at", nullable = false) private LocalDateTime createdAt;
}
