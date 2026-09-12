package com.tiketkonser.event_service.utility;

import com.tiketkonser.event_service.entity.Acara;
import com.tiketkonser.event_service.entity.Kategori;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class AcaraSpecification {

    public static Specification<Acara> filterAcara(String keyword, String kota, Long kategoriId, String kategoriNama, String status, Long organizerId) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Soft delete filter: only non-deleted events
            predicates.add(criteriaBuilder.isNull(root.get("deletedAt")));

            if (organizerId != null) {
                predicates.add(criteriaBuilder.equal(root.get("organizerId"), organizerId));
            }

            if (keyword != null && !keyword.trim().isEmpty()) {
                String pattern = "%" + keyword.trim().toLowerCase() + "%";
                Predicate namaMatch = criteriaBuilder.like(criteriaBuilder.lower(root.get("nama")), pattern);
                Predicate venueMatch = criteriaBuilder.like(criteriaBuilder.lower(root.get("venue")), pattern);
                predicates.add(criteriaBuilder.or(namaMatch, venueMatch));
            }

            if (kota != null && !kota.trim().isEmpty()) {
                predicates.add(criteriaBuilder.equal(
                        criteriaBuilder.lower(root.get("kota")),
                        kota.trim().toLowerCase()
                ));
            }

            if (kategoriId != null) {
                Join<Acara, Kategori> kategoriJoin = root.join("meKategori", JoinType.INNER);
                predicates.add(criteriaBuilder.equal(kategoriJoin.get("id"), kategoriId));
                if (query != null) {
                    query.distinct(true);
                }
            } else if (kategoriNama != null && !kategoriNama.trim().isEmpty()) {
                Join<Acara, Kategori> kategoriJoin = root.join("meKategori", JoinType.INNER);
                predicates.add(criteriaBuilder.equal(
                        criteriaBuilder.lower(kategoriJoin.get("namaKategori")),
                        kategoriNama.trim().toLowerCase()
                ));
                if (query != null) {
                    query.distinct(true);
                }
            }

            if (status != null && !status.trim().isEmpty() && !status.equalsIgnoreCase("ALL") && !status.equalsIgnoreCase("SEMUA")) {
                predicates.add(criteriaBuilder.equal(
                        criteriaBuilder.upper(root.get("status")),
                        status.trim().toUpperCase()
                ));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<Acara> filterAcara(String keyword, String kota, Long kategoriId, String kategoriNama, String status) {
        return filterAcara(keyword, kota, kategoriId, kategoriNama, status, null);
    }

    public static Specification<Acara> filterAcara(String keyword, String kota, Long kategoriId, String status) {
        return filterAcara(keyword, kota, kategoriId, null, status, null);
    }

    public static Specification<Acara> filterAcara(String keyword, String kota, String status) {
        return filterAcara(keyword, kota, null, null, status, null);
    }
}
