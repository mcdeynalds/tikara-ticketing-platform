package com.tiketkonser.order_service.repository;

import com.tiketkonser.order_service.entity.Pemesanan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PemesananRepository extends JpaRepository<Pemesanan, Long> {
    Page<Pemesanan> findByPenggunaIdAndDeletedAtIsNull(Long penggunaId, Pageable pageable);

    Optional<Pemesanan> findByIdAndDeletedAtIsNull(Long id);

    Optional<Pemesanan> findByNomorPesananAndDeletedAtIsNull(String nomorPesanan);

    @Query("SELECT p FROM Pemesanan p JOIN p.detailPemesananList d WHERE d.kategoriTiketId IN :kategoriIds AND p.status = 'PAID' AND p.deletedAt IS NULL")
    List<Pemesanan> findPaidOrdersByKategoriIds(@Param("kategoriIds") List<Long> kategoriIds);

    @Query("SELECT DISTINCT p FROM Pemesanan p LEFT JOIN FETCH p.detailPemesananList WHERE p.status = 'PENDING' AND p.tenggatPembayaran < :now AND p.deletedAt IS NULL")
    List<Pemesanan> findExpiredPendingOrders(@Param("now") LocalDateTime now);
}
