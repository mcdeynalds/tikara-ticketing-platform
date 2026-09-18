package com.tiketkonser.ticket_service.service;

import org.springframework.security.core.Authentication;

public interface PdfTicketService {

    /**
     * Menghasilkan dokumen PDF E-Tiket resmi dalam format biner byte array.
     *
     * @param ticketId ID tiket
     * @param authentication Informasi login user pemanggil
     * @param authHeader Header Authorization (Bearer token) untuk komunikasi antar microservice
     * @return byte[] biner PDF siap diunduh
     */
    byte[] generateTicketPdf(Long ticketId, Authentication authentication, String authHeader);
}
