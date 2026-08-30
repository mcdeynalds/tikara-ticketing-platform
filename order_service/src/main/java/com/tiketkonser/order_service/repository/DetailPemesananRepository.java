package com.tiketkonser.order_service.repository;

import com.tiketkonser.order_service.entity.DetailPemesanan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DetailPemesananRepository extends JpaRepository<DetailPemesanan, Long> {
    List<DetailPemesanan> findByPemesananId(Long pemesananId);
}