package com.tiketkonser.user_service.utility;

import com.tiketkonser.user_service.entity.Pengguna;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class PenggunaSpecification {

    public static Specification<Pengguna> filterPengguna(String keyword, String peran) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 1. Filter soft delete (hanya data yang deletedAt-nya null)
            predicates.add(criteriaBuilder.isNull(root.get("deletedAt")));

            // 2. Filter search keyword (nama atau email)
            if (keyword != null && !keyword.trim().isEmpty()) {
                String searchPattern = "%" + keyword.trim().toLowerCase() + "%";
                Predicate namaMatch = criteriaBuilder.like(criteriaBuilder.lower(root.get("nama")), searchPattern);
                Predicate emailMatch = criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), searchPattern);
                predicates.add(criteriaBuilder.or(namaMatch, emailMatch));
            }

            // 3. Filter peran (ADMIN / PENYELENGGARA / PEMBELI)
            if (peran != null && !peran.trim().isEmpty()) {
                predicates.add(criteriaBuilder.equal(
                        criteriaBuilder.upper(root.get("peran")),
                        peran.trim().toUpperCase()
                ));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
