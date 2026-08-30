package com.tiketkonser.ticket_service.repository;

import com.tiketkonser.ticket_service.entity.KategoriTiket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface KategoriTiketRepository extends JpaRepository<KategoriTiket, Long> {
    List<KategoriTiket> findByAcaraId(Long acaraId);
}