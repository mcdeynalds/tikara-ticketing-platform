package com.tiketkonser.event_service.repository;

import com.tiketkonser.event_service.entity.Ulasan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UlasanRepository extends JpaRepository<Ulasan, Long> {
    Page<Ulasan> findByAcaraId(Long acaraId, Pageable pageable);
    boolean existsByAcaraIdAndPenggunaId(Long acaraId, Long penggunaId);
    Optional<Ulasan> findByAcaraIdAndPenggunaId(Long acaraId, Long penggunaId);

    @Query("SELECT AVG(u.rating) FROM Ulasan u WHERE u.acara.id = :acaraId")
    Double findAverageRatingByAcaraId(@Param("acaraId") Long acaraId);

    @Query("SELECT COUNT(u) FROM Ulasan u WHERE u.acara.id = :acaraId")
    Long countByAcaraIdAndDeletedAtIsNull(@Param("acaraId") Long acaraId);

    @Query("SELECT COUNT(u) FROM Ulasan u WHERE u.acara.id = :acaraId")
    Long countByAcaraId(@Param("acaraId") Long acaraId);
}

