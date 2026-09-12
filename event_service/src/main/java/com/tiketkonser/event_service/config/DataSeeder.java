package com.tiketkonser.event_service.config;

import com.tiketkonser.event_service.entity.Kategori;
import com.tiketkonser.event_service.repository.KategoriRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final KategoriRepository kategoriRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // Seed standard genre categories if missing
        String[] katNames = {"Pop", "Rock", "Jazz", "Indie", "EDM", "Dangdut", "Klasik", "Hip-Hop"};
        for (String kn : katNames) {
            kategoriRepository.findByNamaKategoriIgnoreCase(kn)
                    .orElseGet(() -> kategoriRepository.save(Kategori.builder()
                            .namaKategori(kn)
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .build()));
        }
        System.out.println(">>> [event_service] Master categories verified. Zero dummy concerts seeded.");
    }
}
