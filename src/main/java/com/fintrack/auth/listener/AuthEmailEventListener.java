package com.fintrack.auth.listener;

import com.fintrack.auth.event.PasswordResetRequestedEvent;
import com.fintrack.auth.event.UserRegisteredEvent;
import com.fintrack.common.mail.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * AuthService'i EmailService'e doğrudan bağlamak yerine event dinleyerek
 * gevşek bağlılık sağlar (bkz. mimari dökümanındaki Event-Driven bölümü).
 * {@code AFTER_COMMIT}: kayıt/parola sıfırlama transaction'ı gerçekten
 * commit olmadan e-posta gönderilmez — örn. rollback olan bir kayıt için
 * doğrulama e-postası gitmesin diye.
 */
@Component
@RequiredArgsConstructor
public class AuthEmailEventListener {

    private final EmailService emailService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onUserRegistered(UserRegisteredEvent event) {
        emailService.sendVerificationEmail(event.email(), event.firstName(), event.rawVerificationToken());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPasswordResetRequested(PasswordResetRequestedEvent event) {
        emailService.sendPasswordResetEmail(event.email(), event.firstName(), event.rawToken());
    }
}
