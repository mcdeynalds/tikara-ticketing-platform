package com.tiketkonser.order_service.repository;

import com.tiketkonser.order_service.entity.Pemesanan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PemesananRepository extends JpaRepository<Pemesanan, Long> {
    Page<Pemesanan> findByPenggunaIdAndDeletedAtIsNull(Long penggunaId, Pageable pageable);
    Optional<Pemesanan> findByIdAndDeletedAtIsNull(Long id);
    Optional<Pemesanan> findByNomorPesananAndDeletedAtIsNull(String nomorPesanan);
}