package com.tiketkonser.queue_service.repository;

import com.tiketkonser.queue_service.entity.AntreanPembeli;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AntreanPembeliRepository extends JpaRepository<AntreanPembeli, Long> {
    Optional<AntreanPembeli> findByAcaraIdAndPenggunaId(Long acaraId, Long penggunaId);
    
    @Query("SELECT MAX(a.nomorAntrean) FROM AntreanPembeli a WHERE a.acaraId = :acaraId")
    Integer findMaxNomorAntreanByAcaraId(@Param("acaraId") Long acaraId);

    long countByAcaraIdAndStatus(Long acaraId, String status);

    long countByAcaraIdAndNomorAntreanLessThanAndStatus(Long acaraId, Integer nomorAntrean, String status);

    List<AntreanPembeli> findByAcaraIdAndStatusOrderByNomorAntreanAsc(Long acaraId, String status, Pageable pageable);
}