package com.tiketkonser.event_service.utility;

import com.tiketkonser.event_service.entity.Acara;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class AcaraSpecification {

    public static Specification<Acara> filterAcara(String keyword, String kota, String status) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Soft delete filter
            predicates.add(criteriaBuilder.isNull(root.get("deletedAt")));

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

            if (status != null && !status.trim().isEmpty()) {
                predicates.add(criteriaBuilder.equal(
                        criteriaBuilder.upper(root.get("status")),
                        status.trim().toUpperCase()
                ));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}