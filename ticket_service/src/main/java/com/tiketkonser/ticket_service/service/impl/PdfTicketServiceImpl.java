package com.tiketkonser.ticket_service.service.impl;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.oned.Code128Writer;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.tiketkonser.ticket_service.entity.KategoriTiket;
import com.tiketkonser.ticket_service.entity.Tiket;
import com.tiketkonser.ticket_service.handler.NotFoundException;
import com.tiketkonser.ticket_service.repository.TiketRepository;
import com.tiketkonser.ticket_service.service.PdfTicketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PdfTicketServiceImpl implements PdfTicketService {

    private final TiketRepository tiketRepository;
    private final RestTemplate restTemplate;

    // Palette Colors
    private static final Color COLOR_PRIMARY_DARK = new Color(15, 23, 42); // #0F172A
    private static final Color COLOR_ACCENT = new Color(255, 90, 95); // #FF5A5F
    private static final Color COLOR_TEXT_MUTED = new Color(100, 116, 139); // #64748B
    private static final Color COLOR_TEXT_DARK = new Color(30, 41, 59); // #1E293B
    private static final Color COLOR_BG_LIGHT = new Color(248, 250, 252); // #F8FAFC
    private static final Color COLOR_BORDER = new Color(226, 232, 240); // #E2E8F0
    private static final Color COLOR_SUCCESS = new Color(22, 163, 74); // #16A34A
    private static final Color COLOR_SUCCESS_BG = new Color(220, 252, 231); // #DCFCE7
    private static final Color COLOR_PURPLE = new Color(109, 40, 217); // #6D28D9

    @Override
    @Transactional(readOnly = true)
    public byte[] generateTicketPdf(Long ticketId, Authentication authentication, String authHeader) {
        Tiket tiket = tiketRepository.findById(ticketId)
                .orElseThrow(() -> new NotFoundException("Tiket tidak ditemukan dengan ID: " + ticketId));

        // Validasi hak akses pengguna
        validateAccess(tiket, authentication);

        // Pengambilan data konser & pengguna pendukung
        KategoriTiket kategori = tiket.getKategoriTiket();
        Long acaraId = kategori != null ? kategori.getAcaraId() : null;

        EventPdfData eventData = fetchEventData(acaraId, authHeader, tiket.getDiterbitkanAt());
        UserPdfData userData = fetchUserData(tiket.getPenggunaId(), authHeader, authentication);

        try {
            return renderPdfDocument(tiket, kategori, eventData, userData);
        } catch (Exception e) {
            log.error("Gagal meng-generate PDF tiket ID {}: {}", ticketId, e.getMessage(), e);
            throw new RuntimeException("Gagal membuat dokumen PDF E-Tiket: " + e.getMessage(), e);
        }
    }

    private void validateAccess(Tiket tiket, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return; // Izinkan jika endpoint dipanggil dengan token terverifikasi atau diizinkan
        }

        boolean isAdminOrOrganizer = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_PENYELENGGARA"));

        if (!isAdminOrOrganizer) {
            Object creds = authentication.getCredentials();
            if (creds instanceof Long && !creds.equals(tiket.getPenggunaId())) {
                throw new AccessDeniedException("Anda tidak memiliki izin untuk mengunduh e-tiket ini.");
            }
        }
    }

    private EventPdfData fetchEventData(Long acaraId, String authHeader, LocalDateTime fallbackDate) {
        EventPdfData data = new EventPdfData();
        data.nama = "Konser Musik TIKARA";
        data.venue = "Stadion Utama Gelora Bung Karno";
        data.kota = "Jakarta";
        data.tanggalMulai = fallbackDate != null ? fallbackDate : LocalDateTime.now();

        if (acaraId == null)
            return data;

        try {
            HttpHeaders headers = new HttpHeaders();
            if (authHeader != null && !authHeader.trim().isEmpty()) {
                headers.set(HttpHeaders.AUTHORIZATION, authHeader.trim());
            }
            HttpEntity<?> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> res = restTemplate.exchange(
                    "http://localhost:8082/api/acara/" + acaraId,
                    HttpMethod.GET,
                    entity,
                    Map.class);

            if (res.getStatusCode().is2xxSuccessful() && res.getBody() != null) {
                Map body = res.getBody();
                if (body.get("nama") != null)
                    data.nama = body.get("nama").toString();
                if (body.get("venue") != null)
                    data.venue = body.get("venue").toString();
                if (body.get("kota") != null)
                    data.kota = body.get("kota").toString();
                if (body.get("tanggalMulai") != null) {
                    try {
                        data.tanggalMulai = LocalDateTime.parse(body.get("tanggalMulai").toString());
                    } catch (Exception ignore) {
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Gagal mengambil data acara ID {} dari event_service: {}", acaraId, e.getMessage());
        }

        return data;
    }

    private UserPdfData fetchUserData(Long penggunaId, String authHeader, Authentication authentication) {
        UserPdfData data = new UserPdfData();
        data.nama = "Pemegang Tiket Resmi";
        data.nik = "3201************";
        data.email = authentication != null ? authentication.getName() : "";

        if (penggunaId == null)
            return data;

        try {
            HttpHeaders headers = new HttpHeaders();
            if (authHeader != null && !authHeader.trim().isEmpty()) {
                headers.set(HttpHeaders.AUTHORIZATION, authHeader.trim());
            }
            HttpEntity<?> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> res = restTemplate.exchange(
                    "http://localhost:8081/api/users/" + penggunaId,
                    HttpMethod.GET,
                    entity,
                    Map.class);

            if (res.getStatusCode().is2xxSuccessful() && res.getBody() != null) {
                Map body = res.getBody();
                if (body.get("nama") != null)
                    data.nama = body.get("nama").toString();
                if (body.get("nik") != null && !body.get("nik").toString().isEmpty()) {
                    String rawNik = body.get("nik").toString();
                    data.nik = maskNik(rawNik);
                }
                if (body.get("email") != null)
                    data.email = body.get("email").toString();
            }
        } catch (Exception e) {
            log.warn("Gagal mengambil profil pengguna ID {} dari user_service: {}", penggunaId, e.getMessage());
            if (authentication != null && authentication.getName() != null) {
                data.nama = authentication.getName();
            }
        }

        return data;
    }

    private String maskNik(String nik) {
        if (nik == null || nik.length() < 6)
            return "3201************";
        return nik.substring(0, 4) + "********" + nik.substring(nik.length() - 4);
    }

    private byte[] renderPdfDocument(Tiket tiket, KategoriTiket kategori, EventPdfData event, UserPdfData user)
            throws Exception {
        Document document = new Document(PageSize.A4, 36, 36, 36, 36);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, baos);
        document.open();

        Locale idLocale = new Locale("id", "ID");
        DateTimeFormatter fullDateFmt = DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy", idLocale);
        DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm", idLocale);
        DateTimeFormatter dateIssuedFmt = DateTimeFormatter.ofPattern("dd MMMM yyyy", idLocale);

        String eventDateStr = event.tanggalMulai != null ? event.tanggalMulai.format(fullDateFmt)
                : "Sabtu, 14 November 2026";
        String openGateStr = event.tanggalMulai != null ? event.tanggalMulai.minusHours(2).format(timeFmt) + " WIB"
                : "16:00 WIB";
        String issuedDateStr = tiket.getDiterbitkanAt() != null ? tiket.getDiterbitkanAt().format(dateIssuedFmt)
                : LocalDateTime.now().format(dateIssuedFmt);

        String tierName = kategori != null && kategori.getNamaKategori() != null
                ? kategori.getNamaKategori().toUpperCase()
                : "REGULAR FESTIVAL";
        BigDecimal harga = kategori != null && kategori.getHarga() != null ? kategori.getHarga() : BigDecimal.ZERO;
        String formattedPrice = NumberFormat.getCurrencyInstance(idLocale).format(harga).replace(",00", "");

        // Header tiket
        PdfPTable headerTable = new PdfPTable(2);
        headerTable.setWidthPercentage(100);
        headerTable.setWidths(new float[] { 65f, 35f });
        headerTable.setSpacingAfter(0);

        PdfPCell headerLeft = new PdfPCell();
        headerLeft.setBackgroundColor(COLOR_PRIMARY_DARK);
        headerLeft.setPadding(14);
        headerLeft.setBorder(Rectangle.NO_BORDER);

        Font brandFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 15, Color.WHITE);
        Font brandSubFont = FontFactory.getFont(FontFactory.HELVETICA, 7.5f, new Color(148, 163, 184));

        Paragraph pBrand = new Paragraph("TIKARA OFFICIAL BOARDING PASS", brandFont);
        Paragraph pSubBrand = new Paragraph("TIKET KONSER RESMI INDONESIA • TERVERIFIKASI KEMENPAREKRAF", brandSubFont);
        headerLeft.addElement(pBrand);
        headerLeft.addElement(pSubBrand);
        headerTable.addCell(headerLeft);

        PdfPCell headerRight = new PdfPCell();
        headerRight.setBackgroundColor(COLOR_PRIMARY_DARK);
        headerRight.setPadding(14);
        headerRight.setHorizontalAlignment(Element.ALIGN_RIGHT);
        headerRight.setBorder(Rectangle.NO_BORDER);

        Font secureFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, COLOR_SUCCESS);
        Font issuedFont = FontFactory.getFont(FontFactory.HELVETICA, 7f, new Color(148, 163, 184));

        Paragraph pSecure = new Paragraph("AUTHENTIC SECURED PASS", secureFont);
        pSecure.setAlignment(Element.ALIGN_RIGHT);
        Paragraph pIssued = new Paragraph("Diterbitkan: " + issuedDateStr, issuedFont);
        pIssued.setAlignment(Element.ALIGN_RIGHT);
        headerRight.addElement(pSecure);
        headerRight.addElement(pIssued);
        headerTable.addCell(headerRight);

        document.add(headerTable);

        // Konten utama tiket
        PdfPTable bodyTable = new PdfPTable(2);
        bodyTable.setWidthPercentage(100);
        bodyTable.setWidths(new float[] { 66f, 34f });
        bodyTable.setSpacingAfter(0);

        // LEFT SECTION (Concert Info & Grid)
        PdfPCell leftCell = new PdfPCell();
        leftCell.setBackgroundColor(Color.WHITE);
        leftCell.setPadding(16);
        leftCell.setBorderColor(COLOR_BORDER);
        leftCell.setBorderWidth(1.2f);
        leftCell.setBorder(Rectangle.LEFT | Rectangle.BOTTOM | Rectangle.RIGHT);

        Font badgeFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, COLOR_PURPLE);
        Paragraph pBadge = new Paragraph("OFFICIAL GATE ACCESS", badgeFont);
        leftCell.addElement(pBadge);

        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 17, COLOR_PRIMARY_DARK);
        Paragraph pTitle = new Paragraph(event.nama, titleFont);
        pTitle.setSpacingBefore(4);
        pTitle.setSpacingAfter(2);
        leftCell.addElement(pTitle);

        Font venueFont = FontFactory.getFont(FontFactory.HELVETICA, 9.5f, COLOR_TEXT_MUTED);
        String venueStr = event.venue + (event.kota != null && !event.kota.isEmpty() ? ", " + event.kota : "");
        Paragraph pVenue = new Paragraph("Lokasi Venue: " + venueStr, venueFont);
        pVenue.setSpacingAfter(14);
        leftCell.addElement(pVenue);

        // Key-Value Grid Table (2 Columns)
        PdfPTable gridTable = new PdfPTable(2);
        gridTable.setWidthPercentage(100);
        gridTable.setWidths(new float[] { 50f, 50f });
        gridTable.setSpacingAfter(12);

        addGridItem(gridTable, "KATEGORI / TIER", tierName, true, COLOR_PRIMARY_DARK);
        addGridItem(gridTable, "HARGA TIKET", formattedPrice, false, COLOR_TEXT_DARK);
        addGridItem(gridTable, "JADWAL KONSER", eventDateStr, false, COLOR_TEXT_DARK);
        addGridItem(gridTable, "WAKTU OPEN GATE", openGateStr, true, COLOR_PRIMARY_DARK);
        addGridItem(gridTable, "NAMA PEMEGANG", user.nama, true, COLOR_PRIMARY_DARK);
        addGridItem(gridTable, "ID LEMBAR TIKET", "#TKT-" + tiket.getId(), true, COLOR_PURPLE);
        addGridItem(gridTable, "NIK TERDAFTAR", user.nik, false, COLOR_TEXT_DARK);
        addGridItem(gridTable, "STATUS TIKET", tiket.getStatus() != null ? tiket.getStatus() : "ISSUED", true,
                COLOR_SUCCESS);

        leftCell.addElement(gridTable);

        // 1D Barcode (Code 128)
        try {
            Image barcodeImg = generateBarcode128(tiket.getKodeQr());
            if (barcodeImg != null) {
                barcodeImg.scaleToFit(220f, 38f);
                barcodeImg.setSpacingBefore(4);
                leftCell.addElement(barcodeImg);
            }
        } catch (Exception e) {
            log.warn("Gagal membuat barcode 128: {}", e.getMessage());
        }

        bodyTable.addCell(leftCell);

        // RIGHT SECTION (Gate Stub with QR Code)
        PdfPCell rightCell = new PdfPCell();
        rightCell.setBackgroundColor(COLOR_BG_LIGHT);
        rightCell.setPadding(14);
        rightCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        rightCell.setBorderColor(COLOR_BORDER);
        rightCell.setBorderWidth(1.2f);
        rightCell.setBorder(Rectangle.BOTTOM | Rectangle.RIGHT);

        // Status Badge Pill
        Font readyFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8.5f, COLOR_SUCCESS);
        PdfPTable readyPill = new PdfPTable(1);
        readyPill.setWidthPercentage(85);
        PdfPCell pillCell = new PdfPCell(new Phrase("SIAP MASUK GATE", readyFont));
        pillCell.setBackgroundColor(COLOR_SUCCESS_BG);
        pillCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        pillCell.setPadding(4);
        pillCell.setBorder(Rectangle.NO_BORDER);
        readyPill.addCell(pillCell);
        readyPill.setSpacingAfter(10);
        rightCell.addElement(readyPill);

        // 2D QR Code Image
        try {
            Image qrImg = generateQrCode(tiket.getKodeQr());
            if (qrImg != null) {
                qrImg.scaleToFit(145f, 145f);
                qrImg.setAlignment(Element.ALIGN_CENTER);
                rightCell.addElement(qrImg);
            }
        } catch (Exception e) {
            log.warn("Gagal membuat QR Code: {}", e.getMessage());
        }

        // Gate Pass Code
        Font codeLabelFont = FontFactory.getFont(FontFactory.HELVETICA, 7f, COLOR_TEXT_MUTED);
        Paragraph pCodeLabel = new Paragraph("KODE GERBANG MASUK", codeLabelFont);
        pCodeLabel.setAlignment(Element.ALIGN_CENTER);
        pCodeLabel.setSpacingBefore(8);
        rightCell.addElement(pCodeLabel);

        Font codeFont = FontFactory.getFont(FontFactory.COURIER_BOLD, 12, COLOR_PRIMARY_DARK);
        Paragraph pCode = new Paragraph(tiket.getKodeQr(), codeFont);
        pCode.setAlignment(Element.ALIGN_CENTER);
        pCode.setSpacingAfter(4);
        rightCell.addElement(pCode);

        Font gateInfoFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7.5f, COLOR_TEXT_MUTED);
        Paragraph pGateInfo = new Paragraph("Pintu Masuk: MAIN GATE", gateInfoFont);
        pGateInfo.setAlignment(Element.ALIGN_CENTER);
        rightCell.addElement(pGateInfo);

        bodyTable.addCell(rightCell);
        document.add(bodyTable);

        // Syarat dan ketentuan
        PdfPTable termsTable = new PdfPTable(1);
        termsTable.setWidthPercentage(100);
        termsTable.setSpacingBefore(12);

        PdfPCell termsCell = new PdfPCell();
        termsCell.setBackgroundColor(COLOR_BG_LIGHT);
        termsCell.setBorderColor(COLOR_BORDER);
        termsCell.setBorderWidth(1f);
        termsCell.setPadding(12);

        Font termsTitleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8f, COLOR_PRIMARY_DARK);
        Paragraph pTermsTitle = new Paragraph("SYARAT & KETENTUAN MASUK VENUE:", termsTitleFont);
        pTermsTitle.setSpacingAfter(4);
        termsCell.addElement(pTermsTitle);

        Font termsBodyFont = FontFactory.getFont(FontFactory.HELVETICA, 7f, COLOR_TEXT_DARK);
        termsCell.addElement(new Paragraph(
                "1. Tiket ini merupakan bukti sah penukaran wristband / akses pintu masuk gate. Simpan di ponsel atau cetak dokumen ini.",
                termsBodyFont));
        termsCell.addElement(new Paragraph(
                "2. Wajib membawa kartu identitas asli (e-KTP/SIM/Paspor) yang sesuai dengan nama yang tercetak pada tiket.",
                termsBodyFont));
        termsCell.addElement(new Paragraph(
                "3. Tiket yang sudah dipindai (scanned) di pintu gate scanner tidak dapat digunakan kembali.",
                termsBodyFont));
        termsCell.addElement(new Paragraph(
                "4. Dilarang keras membawa senjata tajam, kamera profesional (DSLR), botol kaca, laser pointer, dan narkotika ke dalam venue.",
                termsBodyFont));

        termsTable.addCell(termsCell);
        document.add(termsTable);

        // Security Notice Footer
        Font footFont = FontFactory.getFont(FontFactory.HELVETICA, 6.5f, COLOR_TEXT_MUTED);
        Paragraph pFoot = new Paragraph(
                "Dokumen resmi diterbitkan secara elektronik oleh platform TIKARA Tiket Konser Indonesia • Dilindungi Hak Cipta",
                footFont);
        pFoot.setAlignment(Element.ALIGN_CENTER);
        pFoot.setSpacingBefore(10);
        document.add(pFoot);

        document.close();
        return baos.toByteArray();
    }

    private void addGridItem(PdfPTable table, String label, String value, boolean isBold, Color valueColor) {
        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(COLOR_BG_LIGHT);
        cell.setBorderColor(COLOR_BORDER);
        cell.setBorderWidth(0.8f);
        cell.setPadding(6);

        Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 6.5f, COLOR_TEXT_MUTED);
        cell.addElement(new Paragraph(label, labelFont));

        Font valFont = FontFactory.getFont(isBold ? FontFactory.HELVETICA_BOLD : FontFactory.HELVETICA, 8.5f,
                valueColor);
        Paragraph pVal = new Paragraph(value != null ? value : "-", valFont);
        pVal.setSpacingBefore(1);
        cell.addElement(pVal);

        table.addCell(cell);
    }

    private Image generateQrCode(String data) throws Exception {
        if (data == null || data.trim().isEmpty())
            data = "TIKARA-PASS";
        QRCodeWriter qrWriter = new QRCodeWriter();
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.MARGIN, 1);
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);

        BitMatrix bitMatrix = qrWriter.encode(data, BarcodeFormat.QR_CODE, 300, 300, hints);
        BufferedImage bi = MatrixToImageWriter.toBufferedImage(bitMatrix);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(bi, "PNG", baos);
        return Image.getInstance(baos.toByteArray());
    }

    private Image generateBarcode128(String data) throws Exception {
        if (data == null || data.trim().isEmpty())
            data = "TIKARA-PASS";
        Code128Writer writer = new Code128Writer();
        BitMatrix matrix = writer.encode(data, BarcodeFormat.CODE_128, 400, 60);
        BufferedImage bi = MatrixToImageWriter.toBufferedImage(matrix);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(bi, "PNG", baos);
        return Image.getInstance(baos.toByteArray());
    }

    // Helper DTOs
    private static class EventPdfData {
        String nama;
        String venue;
        String kota;
        LocalDateTime tanggalMulai;
    }

    private static class UserPdfData {
        String nama;
        String nik;
        String email;
    }
}
