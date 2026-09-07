package com.fintrack.common.mail;

import com.fintrack.config.AppProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * {@code spring.mail.host} ayarlanmadıysa (yerel geliştirmede olduğu gibi)
 * gönderim başarısız olur ama uygulamayı çökertmez — hata loglanır ve link
 * DEBUG seviyesinde yazdırılır ki geliştirici SMTP kurmadan da akışı test
 * edebilsin. Prod'da {@code MAIL_HOST/MAIL_USERNAME/MAIL_PASSWORD} env
 * değişkenleriyle gerçek bir sağlayıcı (Mailtrap, SES, SendGrid SMTP relay vb.)
 * bağlanır.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SmtpEmailService implements EmailService {

    private final JavaMailSender mailSender;
    private final AppProperties appProperties;

    @Override
    public void sendVerificationEmail(String toEmail, String firstName, String rawToken) {
        String link = appProperties.frontendUrl() + "/verify-email?token=" + rawToken;
        send(toEmail, "FinTrack — E-posta adresini doğrula",
                "Merhaba %s,\n\nHesabını doğrulamak için: %s\n\nBu bağlantı 24 saat geçerlidir."
                        .formatted(firstName, link),
                link);
    }

    @Override
    public void sendPasswordResetEmail(String toEmail, String firstName, String rawToken) {
        String link = appProperties.frontendUrl() + "/reset-password?token=" + rawToken;
        send(toEmail, "FinTrack — Şifre sıfırlama",
                "Merhaba %s,\n\nŞifreni sıfırlamak için: %s\n\nBu bağlantı 1 saat geçerlidir. Bu isteği sen yapmadıysan bu e-postayı yok sayabilirsin."
                        .formatted(firstName, link),
                link);
    }

    private void send(String toEmail, String subject, String body, String link) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(appProperties.mailFrom());
            message.setTo(toEmail);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
        } catch (Exception ex) {
            log.warn("E-posta gönderilemedi (SMTP yapılandırılmamış olabilir), bağlantı log'a yazılıyor: {}", link);
            log.debug("E-posta gönderim hatası", ex);
        }
    }
}
