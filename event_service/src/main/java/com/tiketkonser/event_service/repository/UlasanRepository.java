package com.tiketkonser.event_service.repository;

import com.tiketkonser.event_service.entity.Ulasan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UlasanRepository extends JpaRepository<Ulasan, Long> {
    Page<Ulasan> findByAcaraId(Long acaraId, Pageable pageable);
}