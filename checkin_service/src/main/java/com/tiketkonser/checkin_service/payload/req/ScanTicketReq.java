package com.tiketkonser.checkin_service.payload.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScanTicketReq {

    @NotNull(message = "Acara ID wajib diisi")
    private Long acaraId;

    @NotBlank(message = "Kode QR tiket wajib diisi")
    private String kodeQr;

    @NotBlank(message = "Lokasi gate wajib diisi")
    private String lokasiGate;
}