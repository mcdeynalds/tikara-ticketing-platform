package com.tiketkonser.ticket_service.service.impl;

import com.tiketkonser.ticket_service.entity.KategoriTiket;
import com.tiketkonser.ticket_service.entity.Tiket;
import com.tiketkonser.ticket_service.handler.BadRequestException;
import com.tiketkonser.ticket_service.handler.NotFoundException;
import com.tiketkonser.ticket_service.repository.TiketRepository;
import com.tiketkonser.ticket_service.service.EmailTicketService;
import com.tiketkonser.ticket_service.service.PdfTicketService;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailTicketServiceImpl implements EmailTicketService {

    private final JavaMailSender mailSender;
    private final PdfTicketService pdfTicketService;
    private final TiketRepository tiketRepository;
    private final RestTemplate restTemplate;

    @Value("${spring.mail.username:deafriliart@gmail.com}")
    private String fromEmail;

    @Override
    @Transactional(readOnly = true)
    public void sendTicketEmail(Long ticketId, Authentication authentication, String authHeader, String targetEmail) {
        Tiket tiket = tiketRepository.findById(ticketId)
                .orElseThrow(() -> new NotFoundException("Tiket tidak ditemukan dengan ID: " + ticketId));

        String recipientEmail = resolveRecipientEmail(tiket, authentication, authHeader, targetEmail);
        if (recipientEmail == null || recipientEmail.trim().isEmpty() || !recipientEmail.contains("@")) {
            throw new BadRequestException(
                    "Alamat email penerima tidak valid atau akun login tidak memiliki email terdaftar.");
        }
        recipientEmail = recipientEmail.trim();

        String eventTitle = "Konser Musik TIKARA";
        String eventVenue = "Stadion Utama Gelora Bung Karno";
        String eventDateStr = "14 November 2026";
        KategoriTiket kategori = tiket.getKategoriTiket();
        String tierName = kategori != null && kategori.getNamaKategori() != null ? kategori.getNamaKategori()
                : "REGULAR";

        if (kategori != null && kategori.getAcaraId() != null) {
            try {
                HttpHeaders headers = new HttpHeaders();
                if (authHeader != null && !authHeader.trim().isEmpty()) {
                    headers.set(HttpHeaders.AUTHORIZATION, authHeader.trim());
                }
                HttpEntity<?> entity = new HttpEntity<>(headers);
                ResponseEntity<Map> res = restTemplate.exchange(
                        "http://localhost:8082/api/acara/" + kategori.getAcaraId(),
                        HttpMethod.GET,
                        entity,
                        Map.class);
                if (res.getStatusCode().is2xxSuccessful() && res.getBody() != null) {
                    Map body = res.getBody();
                    if (body.get("nama") != null)
                        eventTitle = body.get("nama").toString();
                    if (body.get("venue") != null)
                        eventVenue = body.get("venue").toString();
                    if (body.get("tanggalMulai") != null) {
                        try {
                            LocalDateTime dt = LocalDateTime.parse(body.get("tanggalMulai").toString());
                            eventDateStr = dt
                                    .format(DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy", new Locale("id", "ID")));
                        } catch (Exception ignore) {
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("Gagal mengambil info acara untuk email: {}", e.getMessage());
            }
        }

        log.info("Membuat dokumen PDF e-tiket resmi untuk dikirimkan ke email: {}", recipientEmail);
        byte[] pdfBytes = pdfTicketService.generateTicketPdf(ticketId, authentication, authHeader);
        if (pdfBytes == null || pdfBytes.length == 0) {
            throw new RuntimeException("Gagal meng-generate berkas PDF e-tiket untuk dilampirkan.");
        }

        String safeEventTitle = eventTitle.replaceAll("[^a-zA-Z0-9_-]", "_");
        String attachmentFilename = "TIKARA_ETicket_" + safeEventTitle + "_TKT" + ticketId + ".pdf";

        log.info("Mengirim e-tiket PDF ke {} dengan subjek tiket #TKT-{}", recipientEmail, ticketId);
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, "TIKARA Tiket Konser");
            helper.setTo(recipientEmail);
            helper.setSubject("[TIKARA E-Tiket Resmi] " + eventTitle + " (#TKT-" + ticketId + ")");

            String htmlBody = "<div style=\"font-family:'Segoe UI',Roboto,Helvetica,Arial,sans-serif; max-width:620px; margin:0 auto; border:1px solid #E2E8F0; border-radius:16px; overflow:hidden; background:#ffffff;\">"
                    + "<div style=\"background:linear-gradient(135deg,#0F172A 0%,#1B365D 100%); color:#ffffff; padding:28px 24px; text-align:center;\">"
                    + "<h1 style=\"color:#FF5A5F; font-size:24px; margin:0 0 6px; letter-spacing:1px;\">&#127915; TIKARA OFFICIAL PASS</h1>"
                    + "<p style=\"color:#94A3B8; font-size:12px; margin:0; text-transform:uppercase; letter-spacing:0.5px;\">TIKET KONSER RESMI INDONESIA • TERVERIFIKASI</p>"
                    + "</div>"
                    + "<div style=\"padding:24px;\">"
                    + "<h3 style=\"color:#0F172A; margin-top:0;\">Halo Penonton Konser,</h3>"
                    + "<p style=\"color:#334155; line-height:1.6;\">Selamat! E-Tiket resmi Anda untuk <strong>"
                    + eventTitle
                    + "</strong> telah berhasil diterbitkan dan siap digunakan untuk memasuki gate venue.</p>"
                    + "<div style=\"background:#F8FAFC; border:1px solid #E2E8F0; border-radius:12px; padding:16px; margin:20px 0;\">"
                    + "<table style=\"width:100%; border-collapse:collapse; font-size:14px; color:#1E293B;\">"
                    + "<tr><td style=\"color:#64748B; padding:6px 0;\">Nomor Tiket:</td><td style=\"font-weight:bold; font-family:monospace; color:#6D28D9;\">#TKT-"
                    + tiket.getId() + "</td></tr>"
                    + "<tr><td style=\"color:#64748B; padding:6px 0;\">Kategori / Tier:</td><td style=\"font-weight:bold;\">"
                    + tierName + "</td></tr>"
                    + "<tr><td style=\"color:#64748B; padding:6px 0;\">Kode Gerbang:</td><td style=\"font-weight:bold; font-family:monospace; color:#0F172A;\">"
                    + tiket.getKodeQr() + "</td></tr>"
                    + "<tr><td style=\"color:#64748B; padding:6px 0;\">Jadwal Konser:</td><td>" + eventDateStr
                    + "</td></tr>"
                    + "<tr><td style=\"color:#64748B; padding:6px 0;\">Lokasi Venue:</td><td>" + eventVenue
                    + "</td></tr>"
                    + "</table>"
                    + "</div>"
                    + "<div style=\"background:#DCFCE7; border:1px solid #BBF7D0; border-radius:10px; padding:14px; margin-bottom:20px; color:#15803D; font-size:13px; line-height:1.5;\">"
                    + "<strong>&#9989; Berkas E-Tiket PDF Resmi Terlampir:</strong><br/>"
                    + "Kami telah melampirkan berkas PDF resmi ber-QR Code resolusi tinggi pada email ini. Anda dapat mencetak atau menyimpannya di ponsel Anda untuk dipindai oleh petugas scanner pintu masuk venue."
                    + "</div>"
                    + "<p style=\"color:#64748B; font-size:12px; line-height:1.5;\">Pastikan membawa kartu identitas asli (e-KTP/SIM/Paspor) saat check-in di gate venue. Jangan membagikan barcode atau QR Code tiket kepada orang lain untuk menghindari duplikasi.</p>"
                    + "</div>"
                    + "<div style=\"background:#F1F5F9; padding:16px; text-align:center; font-size:11px; color:#94A3B8; border-top:1px solid #E2E8F0;\">"
                    + "&copy; 2026 TIKARA Tiket Konser Indonesia. Seluruh hak cipta dilindungi undang-undang."
                    + "</div>"
                    + "</div>";

            helper.setText(htmlBody, true);
            helper.addAttachment(attachmentFilename, new ByteArrayResource(pdfBytes), "application/pdf");

            mailSender.send(message);
            log.info("Email e-tiket PDF berhasil dikirimkan ke: {}", recipientEmail);

        } catch (Exception mailErr) {
            log.error("Gagal mengirim email e-tiket ke {}: {}", recipientEmail, mailErr.getMessage(), mailErr);
            throw new RuntimeException("Gagal mengirimkan email e-tiket: " + mailErr.getMessage(), mailErr);
        }

        try {
            Long userId = tiket.getPenggunaId();
            if (userId != null) {
                Map<String, Object> notifReq = Map.of(
                        "penggunaId", userId,
                        "judul", "E-Tiket PDF Terkirim ke Email",
                        "pesan",
                        "E-Tiket resmi (#TKT-" + ticketId + ") telah berhasil dikirimkan ke alamat email "
                                + recipientEmail + " beserta lampiran PDF ber-QR Code.",
                        "tipe", "IN_APP");
                restTemplate.postForEntity("http://localhost:8086/api/notifications/send", notifReq, Map.class);
            }
        } catch (Exception ignore) {
        }
    }

    private String resolveRecipientEmail(Tiket tiket, Authentication authentication, String authHeader,
            String targetEmail) {
        if (targetEmail != null && targetEmail.contains("@")) {
            return targetEmail.trim();
        }

        if (authentication != null && authentication.getName() != null && authentication.getName().contains("@")) {
            return authentication.getName().trim();
        }

        if (tiket.getPenggunaId() != null) {
            try {
                HttpHeaders headers = new HttpHeaders();
                if (authHeader != null && !authHeader.trim().isEmpty()) {
                    headers.set(HttpHeaders.AUTHORIZATION, authHeader.trim());
                }
                HttpEntity<?> entity = new HttpEntity<>(headers);
                ResponseEntity<Map> res = restTemplate.exchange(
                        "http://localhost:8081/api/users/" + tiket.getPenggunaId(),
                        HttpMethod.GET,
                        entity,
                        Map.class);
                if (res.getStatusCode().is2xxSuccessful() && res.getBody() != null) {
                    Map body = res.getBody();
                    if (body.get("email") != null) {
                        return body.get("email").toString().trim();
                    }
                }
            } catch (Exception e) {
                log.warn("Tidak dapat mengambil email pengguna dari user_service: {}", e.getMessage());
            }
        }

        return null;
    }
}
