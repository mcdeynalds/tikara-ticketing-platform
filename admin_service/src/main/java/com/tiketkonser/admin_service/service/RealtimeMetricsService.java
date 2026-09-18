package com.tiketkonser.admin_service.service;

import com.tiketkonser.admin_service.entity.AuditLog;
import com.tiketkonser.admin_service.entity.LaporanPenjualan;
import com.tiketkonser.admin_service.payload.res.DashboardSummaryRes;
import com.tiketkonser.admin_service.payload.res.LaporanPenjualanRes;
import com.tiketkonser.admin_service.payload.res.PageResponse;
import com.tiketkonser.admin_service.repository.AuditLogRepository;
import com.tiketkonser.admin_service.repository.LaporanPenjualanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class RealtimeMetricsService {

    private final LaporanPenjualanRepository laporanPenjualanRepository;
    private final AuditLogRepository auditLogRepository;

    @Value("${metrics.datasource.user.url:jdbc:postgresql://localhost:5432/db_user_service}")
    private String userDbUrl;

    @Value("${metrics.datasource.event.url:jdbc:postgresql://localhost:5432/db_event_service}")
    private String eventDbUrl;

    @Value("${metrics.datasource.order.url:jdbc:postgresql://localhost:5432/db_order_service}")
    private String orderDbUrl;

    @Value("${metrics.datasource.ticket.url:jdbc:postgresql://localhost:5432/db_ticket_service}")
    private String ticketDbUrl;

    @Value("${metrics.datasource.username:postgres}")
    private String dbUsername;

    @Value("${metrics.datasource.password:postgres}")
    private String dbPassword;

    public DashboardSummaryRes getRealtimeSummary() {
        Long totalPengguna = fetchCount(userDbUrl, "SELECT COUNT(*) FROM pengguna WHERE deleted_at IS NULL");
        Long totalAcara = fetchCount(eventDbUrl,
                "SELECT COUNT(*) FROM acara WHERE deleted_at IS NULL AND status = 'PUBLISHED'");
        Long totalAcaraPending = fetchCount(eventDbUrl,
                "SELECT COUNT(*) FROM acara WHERE deleted_at IS NULL AND status = 'PENDING_REVIEW'");
        Long totalTiketTerjual = fetchCount(orderDbUrl,
                "SELECT COALESCE(SUM(dp.jumlah), 0) FROM detail_pemesanan dp " +
                        "JOIN pemesanan p ON dp.pemesanan_id = p.id " +
                        "WHERE p.status = 'PAID' AND p.deleted_at IS NULL");
        BigDecimal totalOmzet = fetchSum(orderDbUrl,
                "SELECT COALESCE(SUM(total_harga), 0) FROM pemesanan " +
                        "WHERE status = 'PAID' AND deleted_at IS NULL");

        return DashboardSummaryRes.builder()
                .totalPengguna(totalPengguna)
                .totalAcara(totalAcara)
                .totalAcaraPending(totalAcaraPending)
                .totalTiketTerjual(totalTiketTerjual)
                .totalOmzetPendapatan(totalOmzet)
                .build();
    }

    public PageResponse<LaporanPenjualanRes> getRealtimeSalesReport(int page, int size) {
        syncRealtimeSalesReportFromDb();

        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size);
        org.springframework.data.domain.Page<LaporanPenjualan> pageData = laporanPenjualanRepository
                .findAllByOrderByCreatedAtDesc(pageable);

        List<LaporanPenjualanRes> content = pageData.getContent().stream()
                .map(item -> LaporanPenjualanRes.builder()
                        .id(item.getId())
                        .acaraId(item.getAcaraId())
                        .totalTiketTerjual(item.getTotalTiketTerjual())
                        .totalPendapatan(item.getTotalPendapatan())
                        .periode(item.getPeriode())
                        .createdAt(item.getCreatedAt())
                        .build())
                .toList();

        return PageResponse.<LaporanPenjualanRes>builder()
                .content(content)
                .pageNumber(pageData.getNumber())
                .pageSize(pageData.getSize())
                .totalElements(pageData.getTotalElements())
                .totalPages(pageData.getTotalPages())
                .isLast(pageData.isLast())
                .build();
    }

    public void syncRealtimeSalesReportFromDb() {
        String sql = "SELECT acara_id, " +
                "COALESCE(SUM(terjual), 0) AS total_tiket, " +
                "COALESCE(SUM(terjual * harga), 0) AS total_omzet " +
                "FROM kategori_tiket " +
                "GROUP BY acara_id " +
                "HAVING SUM(terjual) > 0 " +
                "ORDER BY acara_id ASC";

        try (Connection conn = DriverManager.getConnection(ticketDbUrl, dbUsername, dbPassword);
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            String periodeSekarang = LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMMM yyyy"));

            while (rs.next()) {
                Long acaraId = rs.getLong("acara_id");
                int totalTiket = rs.getInt("total_tiket");
                BigDecimal totalOmzet = rs.getBigDecimal("total_omzet");

                List<LaporanPenjualan> existing = laporanPenjualanRepository.findByAcaraId(acaraId);
                if (existing.isEmpty()) {
                    LaporanPenjualan lp = LaporanPenjualan.builder()
                            .acaraId(acaraId)
                            .totalTiketTerjual(totalTiket)
                            .totalPendapatan(totalOmzet)
                            .periode(periodeSekarang)
                            .createdAt(LocalDateTime.now())
                            .build();
                    laporanPenjualanRepository.save(lp);
                } else {
                    LaporanPenjualan lp = existing.get(0);
                    lp.setTotalTiketTerjual(totalTiket);
                    lp.setTotalPendapatan(totalOmzet);
                    lp.setPeriode(periodeSekarang);
                    laporanPenjualanRepository.save(lp);
                }
            }
        } catch (Exception e) {
            log.error("Gagal sinkronisasi laporan penjualan realtime: {}", e.getMessage());
        }
    }

    public void syncRealtimeAuditLogsIfEmpty() {
        try {
            if (auditLogRepository.count() > 0) {
                return;
            }

            List<AuditLog> initialLogs = new ArrayList<>();

            try (Connection conn = DriverManager.getConnection(orderDbUrl, dbUsername, dbPassword);
                    PreparedStatement ps = conn.prepareStatement(
                            "SELECT id, pengguna_id, nomor_pesanan, total_harga, status, created_at " +
                                    "FROM pemesanan WHERE deleted_at IS NULL ORDER BY created_at DESC LIMIT 15");
                    ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    Long pId = rs.getLong("pengguna_id");
                    String noPesanan = rs.getString("nomor_pesanan");
                    BigDecimal total = rs.getBigDecimal("total_harga");
                    String status = rs.getString("status");
                    Timestamp ts = rs.getTimestamp("created_at");
                    LocalDateTime ldt = ts != null ? ts.toLocalDateTime() : LocalDateTime.now();

                    initialLogs.add(AuditLog.builder()
                            .penggunaId(pId)
                            .modul("PEMESANAN")
                            .aksi("PAID".equalsIgnoreCase(status) ? "PAYMENT_SUCCESS" : ("ORDER_" + status))
                            .detail("Pesanan #" + noPesanan + " total Rp " + total.toPlainString() + " status: "
                                    + status)
                            .ipAddress("127.0.0.1")
                            .createdAt(ldt)
                            .build());
                }
            } catch (Exception ex) {
                log.warn("Tidak dapat menarik log pesanan: {}", ex.getMessage());
            }

            try (Connection conn = DriverManager.getConnection(eventDbUrl, dbUsername, dbPassword);
                    PreparedStatement ps = conn.prepareStatement(
                            "SELECT id, organizer_id, nama, status, created_at " +
                                    "FROM acara WHERE deleted_at IS NULL ORDER BY created_at DESC LIMIT 10");
                    ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    Long orgId = rs.getLong("organizer_id");
                    String nama = rs.getString("nama");
                    String status = rs.getString("status");
                    Timestamp ts = rs.getTimestamp("created_at");
                    LocalDateTime ldt = ts != null ? ts.toLocalDateTime() : LocalDateTime.now();

                    initialLogs.add(AuditLog.builder()
                            .penggunaId(orgId)
                            .modul("EVENT")
                            .aksi("PUBLISH_EVENT")
                            .detail("Publikasi konser: \"" + nama + "\" (Status: " + status + ")")
                            .ipAddress("127.0.0.1")
                            .createdAt(ldt)
                            .build());
                }
            } catch (Exception ex) {
                log.warn("Tidak dapat menarik log event: {}", ex.getMessage());
            }

            try (Connection conn = DriverManager.getConnection(userDbUrl, dbUsername, dbPassword);
                    PreparedStatement ps = conn.prepareStatement(
                            "SELECT id, email, peran, created_at " +
                                    "FROM pengguna WHERE deleted_at IS NULL ORDER BY created_at DESC LIMIT 10");
                    ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    Long uId = rs.getLong("id");
                    String email = rs.getString("email");
                    String peran = rs.getString("peran");
                    Timestamp ts = rs.getTimestamp("created_at");
                    LocalDateTime ldt = ts != null ? ts.toLocalDateTime() : LocalDateTime.now();

                    initialLogs.add(AuditLog.builder()
                            .penggunaId(uId)
                            .modul("AUTH")
                            .aksi("USER_REGISTER")
                            .detail("Registrasi akun pengguna: " + email + " (Peran: " + peran + ")")
                            .ipAddress("127.0.0.1")
                            .createdAt(ldt)
                            .build());
                }
            } catch (Exception ex) {
                log.warn("Tidak dapat menarik log user: {}", ex.getMessage());
            }

            if (!initialLogs.isEmpty()) {
                initialLogs.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
                auditLogRepository.saveAll(initialLogs);
                log.info("Berhasil mengimpor {} aktivitas riil dari database ke audit_log", initialLogs.size());
            }
        } catch (Exception e) {
            log.error("Gagal sinkronisasi audit logs dari riwayat DB: {}", e.getMessage());
        }
    }

    private Long fetchCount(String dbUrl, String sql) {
        try (Connection conn = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (Exception e) {
            log.error("Error query count ke {}: {}", dbUrl, e.getMessage());
        }
        return 0L;
    }

    private BigDecimal fetchSum(String dbUrl, String sql) {
        try (Connection conn = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                BigDecimal sum = rs.getBigDecimal(1);
                return sum != null ? sum : BigDecimal.ZERO;
            }
        } catch (Exception e) {
            log.error("Error query sum ke {}: {}", dbUrl, e.getMessage());
        }
        return BigDecimal.ZERO;
    }
}
