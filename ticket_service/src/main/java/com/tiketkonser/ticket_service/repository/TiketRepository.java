package com.tiketkonser.ticket_service.repository;

import com.tiketkonser.ticket_service.entity.Tiket;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TiketRepository extends JpaRepository<Tiket, Long> {
    Page<Tiket> findByPenggunaIdAndDeletedAtIsNull(Long penggunaId, Pageable pageable);

    Optional<Tiket> findByKodeQrAndDeletedAtIsNull(String kodeQr);

    List<Tiket> findByDetailPemesananIdAndDeletedAtIsNull(Long detailPemesananId);

    boolean existsByPenggunaIdAndKategoriTiket_AcaraIdAndStatusInAndDeletedAtIsNull(Long penggunaId, Long acaraId,
            List<String> statuses);

    @Query("SELECT t FROM Tiket t JOIN t.kategoriTiket k WHERE k.acaraId = :acaraId AND t.deletedAt IS NULL ORDER BY t.diterbitkanAt DESC")
    List<Tiket> findAllByAcaraId(@Param("acaraId") Long acaraId);

    @Query("SELECT COUNT(t) FROM Tiket t JOIN t.kategoriTiket k WHERE k.acaraId = :acaraId AND t.status = :status AND t.deletedAt IS NULL")
    Long countByAcaraIdAndStatus(@Param("acaraId") Long acaraId, @Param("status") String status);
}
