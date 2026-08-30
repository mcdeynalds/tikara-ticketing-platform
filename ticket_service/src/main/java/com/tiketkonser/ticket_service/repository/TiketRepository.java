package com.tiketkonser.ticket_service.repository;

import com.tiketkonser.ticket_service.entity.Tiket;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TiketRepository extends JpaRepository<Tiket, Long> {
    Page<Tiket> findByPenggunaIdAndDeletedAtIsNull(Long penggunaId, Pageable pageable);
    Optional<Tiket> findByKodeQrAndDeletedAtIsNull(String kodeQr);
}