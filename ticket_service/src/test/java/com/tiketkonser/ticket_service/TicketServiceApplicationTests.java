package com.tiketkonser.ticket_service;

import com.lowagie.text.pdf.PdfReader;
import com.tiketkonser.ticket_service.service.PdfTicketService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class TicketServiceApplicationTests {

	@Autowired
	private PdfTicketService pdfTicketService;

	@Test
	void contextLoads() {
	}

	@Test
	void testGenerateTicketPdf() throws Exception {
		byte[] pdfBytes = pdfTicketService.generateTicketPdf(5L, null, null);
		Assertions.assertNotNull(pdfBytes);
		Assertions.assertTrue(pdfBytes.length > 3000, "Ukuran PDF harus lebih dari 3KB");

		// Verifikasi bahwa PDF dapat dibaca dan valid secara struktur
		PdfReader reader = new PdfReader(pdfBytes);
		Assertions.assertEquals(1, reader.getNumberOfPages(), "Harus menghasilkan 1 halaman A4 e-tiket");
		reader.close();
	}
}

