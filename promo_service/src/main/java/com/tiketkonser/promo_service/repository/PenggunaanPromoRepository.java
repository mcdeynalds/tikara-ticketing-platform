package com.tiketkonser.promo_service.repository;

import com.tiketkonser.promo_service.entity.PenggunaanPromo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.Optional;

public interface PenggunaanPromoRepository extends JpaRepository<PenggunaanPromo, Long> {
    boolean existsByKodePromoIdAndPenggunaId(Long kodePromoId, Long penggunaId);

    Optional<PenggunaanPromo> findByPemesananId(Long pemesananId);

    void deleteByPemesananId(Long pemesananId);

    @Query("SELECT COALESCE(SUM(p.potonganDidapat), 0) FROM PenggunaanPromo p")
    BigDecimal sumTotalPotongan();
}
