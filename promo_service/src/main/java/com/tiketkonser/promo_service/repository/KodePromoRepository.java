package com.tiketkonser.promo_service.repository;

import com.tiketkonser.promo_service.entity.KodePromo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface KodePromoRepository extends JpaRepository<KodePromo, Long> {
    Optional<KodePromo> findByKodeIgnoreCase(String kode);
    boolean existsByKodeIgnoreCase(String kode);
    List<KodePromo> findByStatusIgnoreCase(String status);
    List<KodePromo> findAllByOrderByCreatedAtDesc();
    long countByStatusIgnoreCase(String status);

    @Query("SELECT COALESCE(SUM(k.terpakai), 0) FROM KodePromo k")
    Integer sumTotalTerpakai();
}
