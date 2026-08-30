package com.tiketkonser.promo_service.repository;

import com.tiketkonser.promo_service.entity.PenggunaanPromo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PenggunaanPromoRepository extends JpaRepository<PenggunaanPromo, Long> {
    boolean existsByKodePromoIdAndPenggunaId(Long kodePromoId, Long penggunaId);
}