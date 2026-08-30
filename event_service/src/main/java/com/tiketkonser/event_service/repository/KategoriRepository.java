package com.tiketkonser.event_service.repository;

import com.tiketkonser.event_service.entity.Kategori;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface KategoriRepository extends JpaRepository<Kategori, Long> {
    Optional<Kategori> findByNamaKategoriIgnoreCase(String namaKategori);
    boolean existsByNamaKategoriIgnoreCase(String namaKategori);
}