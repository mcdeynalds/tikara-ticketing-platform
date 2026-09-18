package com.tiketkonser.admin_service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class AdminServiceApplicationTests {

	@org.springframework.beans.factory.annotation.Autowired
	private com.tiketkonser.admin_service.service.RealtimeMetricsService realtimeMetricsService;

	@Test
	void contextLoads() {
	}

	@Test
	void testRealtimeMetrics() {
		var summary = realtimeMetricsService.getRealtimeSummary();
		System.out.println("=== REALTIME SUMMARY TEST ===");
		System.out.println("Total Pengguna: " + summary.getTotalPengguna());
		System.out.println("Total Acara: " + summary.getTotalAcara());
		System.out.println("Total Acara Pending: " + summary.getTotalAcaraPending());
		System.out.println("Total Tiket: " + summary.getTotalTiketTerjual());
		System.out.println("Total Omzet: " + summary.getTotalOmzetPendapatan());
		org.junit.jupiter.api.Assertions.assertNotNull(summary);
		org.junit.jupiter.api.Assertions.assertTrue(summary.getTotalPengguna() > 0);

		var sales = realtimeMetricsService.getRealtimeSalesReport(0, 10);
		System.out.println("=== REALTIME SALES REPORT TEST ===");
		System.out.println("Total Sales Reports: " + sales.getContent().size());
		sales.getContent().forEach(r -> {
			System.out.println("Acara #" + r.getAcaraId() + " -> " + r.getTotalTiketTerjual() + " tiket, Rp " + r.getTotalPendapatan());
		});
		org.junit.jupiter.api.Assertions.assertFalse(sales.getContent().isEmpty());

		realtimeMetricsService.syncRealtimeAuditLogsIfEmpty();
	}

	@org.springframework.beans.factory.annotation.Autowired
	private com.tiketkonser.admin_service.controller.WebViewController webViewController;

	@Test
	void testAdminDashboardControllerAuth() {
		var model = new org.springframework.ui.ConcurrentModel();
		
		// 1. Unauthenticated test -> Redirect to login
		String viewUnauth = webViewController.adminDashboard(null, null, model);
		org.junit.jupiter.api.Assertions.assertTrue(viewUnauth.startsWith("redirect:http://localhost:8081/login.html"));

		// 2. Non-admin test -> Redirect to index
		var userAuth = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
				"user@test.com", 1L, java.util.List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_PEMBELI"))
		);
		String viewNonAdmin = webViewController.adminDashboard(null, userAuth, model);
		org.junit.jupiter.api.Assertions.assertEquals("redirect:http://localhost:8082/index.html", viewNonAdmin);

		// 3. Admin authenticated test -> Returns admin-dashboard view with all populated model attributes
		var adminAuth = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
				"admin@tikara.com", 99L, java.util.List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_ADMIN"))
		);
		String viewAdmin = webViewController.adminDashboard(null, adminAuth, model);
		org.junit.jupiter.api.Assertions.assertEquals("admin-dashboard", viewAdmin);
		org.junit.jupiter.api.Assertions.assertNotNull(model.getAttribute("summary"));
		org.junit.jupiter.api.Assertions.assertNotNull(model.getAttribute("formattedOmzet"));
		org.junit.jupiter.api.Assertions.assertNotNull(model.getAttribute("salesReports"));
		org.junit.jupiter.api.Assertions.assertNotNull(model.getAttribute("auditLogs"));
		System.out.println("Formatted Omzet in Java Model: " + model.getAttribute("formattedOmzet"));
		System.out.println("Total Sales Reports in Model: " + ((java.util.List<?>) model.getAttribute("salesReports")).size());
		System.out.println("Total Audit Logs in Model: " + ((java.util.List<?>) model.getAttribute("auditLogs")).size());
	}
}
