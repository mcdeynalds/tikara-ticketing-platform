package com.tiketkonser.checkin_service.repository;

import com.tiketkonser.checkin_service.entity.RiwayatCheckin;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RiwayatCheckinRepository extends JpaRepository<RiwayatCheckin, Long> {
    Page<RiwayatCheckin> findByAcaraIdOrderByWaktuCheckinDesc(Long acaraId, Pageable pageable);

    long countByAcaraIdAndStatusCheckin(Long acaraId, String statusCheckin);

    long countByAcaraId(Long acaraId);

    boolean existsByKodeQrAndStatusCheckin(String kodeQr, String statusCheckin);

    @Query("SELECT r FROM RiwayatCheckin r WHERE r.acaraId = :acaraId ORDER BY r.waktuCheckin ASC")
    List<RiwayatCheckin> findAllByAcaraIdOrderByWaktuAsc(@Param("acaraId") Long acaraId);
}
