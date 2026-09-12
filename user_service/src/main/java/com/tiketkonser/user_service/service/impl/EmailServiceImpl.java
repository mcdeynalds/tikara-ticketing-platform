package com.tiketkonser.user_service.service.impl;

import com.tiketkonser.user_service.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:deafriliart@gmail.com}")
    private String fromEmail;

    @Override
    public void sendOtpEmail(String toEmail, String otpCode) {
        // ALWAYS LOG TO CONSOLE FOR INSTANT TESTING & DEBUGGING
        System.out.println("=================================================");
        System.out.println(">>> [TIKARA EMAIL SERVICE] OTP RESET PASSWORD <<<");
        System.out.println(">>> SENDER  : " + fromEmail);
        System.out.println(">>> TO      : " + toEmail);
        System.out.println(">>> OTP CODE: " + otpCode);
        System.out.println(">>> EXPIRY  : 10 Menit");
        System.out.println("=================================================");

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, "TIKARA");
            helper.setTo(toEmail);
            helper.setSubject("[TIKARA] Kode Verifikasi Reset Password");

            String htmlBody = "<div style=\"font-family:Arial,sans-serif; max-width:600px; margin:0 auto; border:1px solid #E2E8F0; border-radius:12px; padding:2rem; background:#ffffff;\">"
                    + "<div style=\"text-align:center; margin-bottom:1.5rem;\">"
                    + "<h1 style=\"color:#FF5A5F; font-size:2rem; margin:0;\">&#127915; TIKARA</h1>"
                    + "<p style=\"color:#64748B; font-size:0.9rem;\">Platform Pemesanan Tiket Konser #1 Indonesia</p>"
                    + "</div>"
                    + "<hr style=\"border:none; border-top:1px solid #E2E8F0; margin-bottom:1.5rem;\"/>"
                    + "<h3 style=\"color:#0F172A;\">Halo,</h3>"
                    + "<p style=\"color:#334155; line-height:1.6;\">Kami menerima permintaan untuk mengatur ulang kata sandi akun TIKARA Anda.</p>"
                    + "<p style=\"color:#334155;\">Gunakan kode verifikasi OTP 6-digit berikut untuk melanjutkan:</p>"
                    + "<div style=\"text-align:center; margin:2rem 0;\">"
                    + "<span style=\"font-family:monospace; font-size:2.5rem; font-weight:bold; letter-spacing:8px; color:#1B365D; background:#F1F5F9; padding:0.8rem 1.5rem; border-radius:8px; border:2px dashed #CBD5E1; display:inline-block;\">"
                    + otpCode + "</span>"
                    + "</div>"
                    + "<p style=\"color:#EF4444; font-weight:bold; text-align:center; font-size:0.9rem;\">&#9888;&#65039; Kode ini berlaku selama 10 MENIT. Jangan berikan kode ini kepada siapapun!</p>"
                    + "<p style=\"color:#64748B; font-size:0.85rem; margin-top:2rem;\">Jika Anda tidak merasa meminta reset password, silakan abaikan email ini.</p>"
                    + "</div>";

            helper.setText(htmlBody, true);
            mailSender.send(message);
            System.out.println(">>> [EMAIL SMTP SENT SUCCESSFULLY TO " + toEmail + "]");
        } catch (Exception e) {
            System.err.println(">>> [SMTP WARNING] Gagal mengirim email via SMTP Server (" + e.getMessage() + "). OTP tetap valid di console!");
            e.printStackTrace();
        }
    }
}
