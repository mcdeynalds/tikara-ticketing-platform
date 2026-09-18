package com.tiketkonser.admin_service.controller;

import com.tiketkonser.admin_service.payload.res.AuditLogRes;
import com.tiketkonser.admin_service.payload.res.DashboardSummaryRes;
import com.tiketkonser.admin_service.payload.res.LaporanPenjualanRes;
import com.tiketkonser.admin_service.payload.res.PageResponse;
import com.tiketkonser.admin_service.service.AdminService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Controller
@RequiredArgsConstructor
@Slf4j
public class WebViewController {

    private final AdminService adminService;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FormattedSalesReportView {
        private Long id;
        private Long acaraId;
        private String periode;
        private Integer totalTiketTerjual;
        private String formattedPendapatan;
        private String formattedDate;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FormattedAuditLogView {
        private Long id;
        private String formattedDate;
        private String modul;
        private String badgeClass;
        private String badgeStyle;
        private String aksi;
        private String detail;
        private String ipAddress;
    }

    private String formatRupiah(BigDecimal amount) {
        if (amount == null)
            return "Rp 0";
        return "Rp " + String.format(Locale.GERMANY, "%,d", amount.longValue());
    }

    @GetMapping({ "/admin-dashboard", "/admin-dashboard.html" })
    public String adminDashboard(
            HttpServletRequest request,
            Authentication authentication,
            Model model) {
        if (authentication == null || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            log.warn("Akses ditolak ke admin-dashboard: Pengguna belum terautentikasi.");
            String redirectUrl = "http://localhost:8081/login.html?redirect="
                    + URLEncoder.encode("http://localhost:8089/admin-dashboard.html", StandardCharsets.UTF_8);
            return "redirect:" + redirectUrl;
        }

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin) {
            log.warn("Akses ditolak ke admin-dashboard: Pengguna {} bukan ROLE_ADMIN.", authentication.getName());
            return "redirect:http://localhost:8082/index.html";
        }

        DashboardSummaryRes summary = adminService.getDashboardSummary(authentication);
        PageResponse<LaporanPenjualanRes> salesReportPage = adminService.getSalesReport(authentication, 0, 10);
        PageResponse<AuditLogRes> auditLogPage = adminService.getAllAuditLogs(authentication, 0, 10);

        String formattedOmzet = formatRupiah(summary.getTotalOmzetPendapatan());

        List<FormattedSalesReportView> formattedReports = new ArrayList<>();
        if (salesReportPage != null && salesReportPage.getContent() != null) {
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy", new Locale("id", "ID"));
            for (LaporanPenjualanRes r : salesReportPage.getContent()) {
                String dt = r.getCreatedAt() != null ? r.getCreatedAt().format(dateFormatter) : "-";
                formattedReports.add(new FormattedSalesReportView(
                        r.getId(),
                        r.getAcaraId(),
                        r.getPeriode() != null ? r.getPeriode() : "-",
                        r.getTotalTiketTerjual() != null ? r.getTotalTiketTerjual() : 0,
                        formatRupiah(r.getTotalPendapatan()),
                        dt));
            }
        }

        List<FormattedAuditLogView> formattedLogs = new ArrayList<>();
        if (auditLogPage != null && auditLogPage.getContent() != null) {
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm", new Locale("id", "ID"));
            for (AuditLogRes al : auditLogPage.getContent()) {
                String dt = al.getCreatedAt() != null ? al.getCreatedAt().format(dtf) : "-";
                String modul = al.getModul() != null ? al.getModul() : "SISTEM";
                String badgeStyle = "font-size:0.75rem;";
                String badgeClass = "pembeli";
                if ("PEMBAYARAN".equalsIgnoreCase(modul)
                        || (al.getAksi() != null && al.getAksi().contains("SUCCESS"))) {
                    badgeStyle = "background:#DCFCE7; color:#15803D; font-size:0.75rem;";
                } else if ("EVENT".equalsIgnoreCase(modul)) {
                    badgeClass = "penyelenggara";
                }

                formattedLogs.add(new FormattedAuditLogView(
                        al.getId(),
                        dt,
                        modul,
                        badgeClass,
                        badgeStyle,
                        al.getAksi() != null ? al.getAksi() : "-",
                        al.getDetail() != null ? al.getDetail() : "-",
                        al.getIpAddress() != null ? al.getIpAddress() : "127.0.0.1"));
            }
        }

        model.addAttribute("summary", summary);
        model.addAttribute("formattedOmzet", formattedOmzet);
        model.addAttribute("salesReports", formattedReports);
        model.addAttribute("auditLogs", formattedLogs);
        model.addAttribute("adminEmail", authentication.getName());
        model.addAttribute("adminName", "Admin Tikara");

        return "admin-dashboard";
    }

    @GetMapping("/admin/logout")
    public String adminLogout(HttpServletRequest request, HttpServletResponse response) {
        Cookie tokenCookie = new Cookie("tikara_token", "");
        tokenCookie.setPath("/");
        tokenCookie.setMaxAge(0);
        response.addCookie(tokenCookie);

        Cookie userCookie = new Cookie("tikara_user", "");
        userCookie.setPath("/");
        userCookie.setMaxAge(0);
        response.addCookie(userCookie);

        Cookie roleCookie = new Cookie("tikara_role", "");
        roleCookie.setPath("/");
        roleCookie.setMaxAge(0);
        response.addCookie(roleCookie);

        SecurityContextHolder.clearContext();
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        return "redirect:http://localhost:8081/login.html";
    }

    @GetMapping("/promo-management.html")
    public String promo_management(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return "redirect:http://localhost:8081/login.html?redirect="
                    + URLEncoder.encode("http://localhost:8089/promo-management.html", StandardCharsets.UTF_8);
        }
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin) {
            return "redirect:http://localhost:8082/index.html";
        }
        return "promo-management";
    }

    @GetMapping("/event-moderation.html")
    public String event_moderation(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return "redirect:http://localhost:8081/login.html?redirect="
                    + URLEncoder.encode("http://localhost:8089/event-moderation.html", StandardCharsets.UTF_8);
        }
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin) {
            return "redirect:http://localhost:8082/index.html";
        }
        return "event-moderation";
    }

    @GetMapping({ "/organizer/dashboard", "/organizer-dashboard.html" })
    public String organizerDashboard() {
        return "organizer-dashboard";
    }

    @GetMapping({ "/organizer/event/{acaraId}", "/organizer-event-detail.html" })
    public String organizerEventDetail(@PathVariable(required = false) Long acaraId) {
        return "organizer-event-detail";
    }
}
