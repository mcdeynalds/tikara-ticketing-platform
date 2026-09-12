package com.tiketkonser.user_service.repository;

import com.tiketkonser.user_service.entity.ProfilPengguna;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProfilPenggunaRepository extends JpaRepository<ProfilPengguna, Long> {

    Optional<ProfilPengguna> findByPenggunaId(Long penggunaId);
}
