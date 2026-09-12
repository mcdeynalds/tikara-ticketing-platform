package com.tiketkonser.user_service.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.tiketkonser.user_service.entity.Pengguna;

public interface PenggunaRepository extends JpaRepository<Pengguna, Long>, JpaSpecificationExecutor<Pengguna> {

    boolean existsByEmailIgnoreCaseAndDeletedAtIsNull(String email);

    Optional<Pengguna> findByEmailIgnoreCaseAndDeletedAtIsNull(String email);

    Optional<Pengguna> findByIdAndDeletedAtIsNull(Long id);
}