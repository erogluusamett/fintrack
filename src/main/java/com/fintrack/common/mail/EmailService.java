package com.fintrack.common.mail;

/**
 * E-posta gönderiminin arkasındaki port. Şimdilik tek implementasyonu SMTP
 * (bkz. {@link SmtpEmailService}); sağlayıcı değişirse (SendGrid, SES vb.)
 * çağıran kodun (event listener'lar) haberi olmadan sadece bu arayüzün yeni
 * bir implementasyonu eklenir.
 */
public interface EmailService {

    void sendVerificationEmail(String toEmail, String firstName, String rawToken);

    void sendPasswordResetEmail(String toEmail, String firstName, String rawToken);
}
