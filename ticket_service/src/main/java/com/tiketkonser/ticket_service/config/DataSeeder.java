package com.tiketkonser.ticket_service.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {
    @Override
    public void run(String... args) throws Exception {
        // Dummy data seeder disabled. Real tickets are created by organizers.
    }
}
