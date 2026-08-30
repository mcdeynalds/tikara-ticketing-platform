package com.tiketkonser.admin_service.repository;

import com.tiketkonser.admin_service.entity.LaporanPenjualan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LaporanPenjualanRepository extends JpaRepository<LaporanPenjualan, Long> {
    List<LaporanPenjualan> findByAcaraId(Long acaraId);
    Page<LaporanPenjualan> findAllByOrderByCreatedAtDesc(Pageable pageable);
}