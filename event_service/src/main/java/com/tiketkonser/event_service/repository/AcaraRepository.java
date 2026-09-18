package com.tiketkonser.event_service.repository;

import com.tiketkonser.event_service.entity.Acara;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.Optional;

public interface AcaraRepository extends JpaRepository<Acara, Long>, JpaSpecificationExecutor<Acara> {
    Optional<Acara> findByIdAndDeletedAtIsNull(Long id);
    org.springframework.data.domain.Page<Acara> findByStatusAndDeletedAtIsNull(String status, org.springframework.data.domain.Pageable pageable);
}