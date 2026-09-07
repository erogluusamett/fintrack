-- FinTrack V4: notification domain

CREATE TABLE notifications (
    id                  UUID PRIMARY KEY,
    user_id             UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    type                VARCHAR(30) NOT NULL,
    title               VARCHAR(200) NOT NULL,
    message             VARCHAR(1000) NOT NULL,
    read                BOOLEAN NOT NULL DEFAULT false,
    related_entity_id   UUID,
    related_entity_type VARCHAR(30),
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Kullanıcının bildirim listesi hem user_id hem de okunmamış-önce sıralaması
-- (read, created_at) üzerinden filtrelenip sıralanacağı için composite index.
CREATE INDEX idx_notifications_user_read_created ON notifications (user_id, read, created_at DESC);

-- BudgetEventListener/SubscriptionReminderScheduler'ın dedup kontrolü
-- (existsByRelatedEntityIdAndTitle) için.
CREATE INDEX idx_notifications_related_entity ON notifications (related_entity_id, title);
