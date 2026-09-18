package com.tiketkonser.ticket_service.service;

import org.springframework.security.core.Authentication;

public interface EmailTicketService {

    /**
     * Mengirimkan e-tiket resmi dalam bentuk lampiran (attachment) PDF ke email pengguna.
     *
     * @param ticketId ID tiket yang akan dikirimkan
     * @param authentication Informasi login pengguna
     * @param authHeader Token Authorization pengguna
     * @param targetEmail Alamat email penerima (opsional, jika kosong akan diambil dari akun login)
     */
    void sendTicketEmail(Long ticketId, Authentication authentication, String authHeader, String targetEmail);
}
