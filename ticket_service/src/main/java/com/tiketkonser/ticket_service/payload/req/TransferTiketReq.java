package com.tiketkonser.ticket_service.payload.req;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferTiketReq {

    @NotBlank(message = "Email penerima wajib diisi")
    @Email(message = "Format email penerima tidak valid")
    private String penerimaEmail;

    @NotBlank(message = "NIK penerima wajib diisi")
    @Size(min = 16, max = 16, message = "NIK penerima harus tepat 16 digit")
    private String penerimaNik;

    private String alasan;
}
