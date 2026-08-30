package com.tiketkonser.notification_service.repository;

import com.tiketkonser.notification_service.entity.Notifikasi;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotifikasiRepository extends JpaRepository<Notifikasi, Long> {
    Page<Notifikasi> findByPenggunaIdOrderByCreatedAtDesc(Long penggunaId, Pageable pageable);
    long countByPenggunaIdAndDibacaFalse(Long penggunaId);
}