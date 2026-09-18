package com.tiketkonser.order_service.scheduler;

import com.tiketkonser.order_service.entity.DetailPemesanan;
import com.tiketkonser.order_service.entity.Pemesanan;
import com.tiketkonser.order_service.repository.PemesananRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderCleanupScheduler {

    private static final String TICKET_SERVICE_URL = "http://localhost:8083";

    private final PemesananRepository pemesananRepository;
    private final RestTemplate restTemplate;

    @Scheduled(fixedRate = 10000)
    @Transactional
    public void cleanupExpiredOrders() {
        LocalDateTime now = LocalDateTime.now();
        List<Pemesanan> expiredOrders = pemesananRepository.findExpiredPendingOrders(now);

        if (expiredOrders.isEmpty()) {
            return;
        }

        log.info("Ditemukan {} pesanan PENDING yang telah melewati batas tenggat pembayaran untuk dibersihkan.",
                expiredOrders.size());

        for (Pemesanan order : expiredOrders) {
            boolean allReleased = true;

            if (order.getDetailPemesananList() != null) {
                for (DetailPemesanan item : order.getDetailPemesananList()) {
                    try {
                        Map<String, Object> releasePayload = Map.of(
                                "kategoriTiketId", item.getKategoriTiketId(),
                                "jumlah", item.getJumlah());
                        restTemplate.postForEntity(TICKET_SERVICE_URL + "/api/inventory/release", releasePayload,
                                Map.class);
                    } catch (Exception e) {
                        allReleased = false;
                        log.error("Gagal melepaskan reservasi stok kategori ID {} untuk order #{}: {}",
                                item.getKategoriTiketId(), order.getNomorPesanan(), e.getMessage());
                    }
                }
            }

            if (allReleased) {
                order.setStatus("CANCELLED");
                order.setUpdatedAt(LocalDateTime.now());
                pemesananRepository.save(order);
                log.info("Pesanan #{} berhasil dibatalkan otomatis dan reservasi stok tiket telah dilepas.",
                        order.getNomorPesanan());
            } else {
                log.warn(
                        "Pesanan #{} tidak diubah menjadi CANCELLED karena pelepasan stok tiket gagal sebagian. Akan dicoba kembali di interval berikutnya.",
                        order.getNomorPesanan());
            }
        }
    }
}
