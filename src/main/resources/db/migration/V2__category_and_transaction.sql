-- FinTrack V2: category & transaction domain

CREATE TABLE categories (
    id          UUID PRIMARY KEY,
    user_id     UUID REFERENCES users (id) ON DELETE CASCADE,
    name        VARCHAR(100) NOT NULL,
    type        VARCHAR(20) NOT NULL,
    is_default  BOOLEAN NOT NULL DEFAULT false,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_categories_user_name_type UNIQUE (user_id, name, type)
);

CREATE INDEX idx_categories_user_id ON categories (user_id);

CREATE TABLE transactions (
    id                UUID PRIMARY KEY,
    user_id           UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    category_id       UUID REFERENCES categories (id) ON DELETE RESTRICT,
    type              VARCHAR(20) NOT NULL,
    amount            NUMERIC(19, 2) NOT NULL,
    currency          VARCHAR(3) NOT NULL,
    transaction_date  DATE NOT NULL,
    description       VARCHAR(500),
    deleted           BOOLEAN NOT NULL DEFAULT false,
    deleted_at        TIMESTAMPTZ,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at        TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_transactions_category_id ON transactions (category_id);
-- Listeleme/filtreleme sorgularının çoğu user_id + tarih aralığı + deleted
-- kombinasyonu üzerinden çalışacağı için composite index:
CREATE INDEX idx_transactions_user_date ON transactions (user_id, transaction_date, deleted);

-- Sistem varsayılan kategorileri (user_id NULL = herkese görünür)
INSERT INTO categories (id, user_id, name, type, is_default) VALUES
    (gen_random_uuid(), NULL, 'Food', 'EXPENSE', true),
    (gen_random_uuid(), NULL, 'Rent', 'EXPENSE', true),
    (gen_random_uuid(), NULL, 'Transportation', 'EXPENSE', true),
    (gen_random_uuid(), NULL, 'Bills', 'EXPENSE', true),
    (gen_random_uuid(), NULL, 'Entertainment', 'EXPENSE', true),
    (gen_random_uuid(), NULL, 'Shopping', 'EXPENSE', true),
    (gen_random_uuid(), NULL, 'Health', 'EXPENSE', true),
    (gen_random_uuid(), NULL, 'Education', 'EXPENSE', true),
    (gen_random_uuid(), NULL, 'Other', 'EXPENSE', true),
    (gen_random_uuid(), NULL, 'Salary', 'INCOME', true),
    (gen_random_uuid(), NULL, 'Freelance', 'INCOME', true),
    (gen_random_uuid(), NULL, 'Investment', 'INCOME', true),
    (gen_random_uuid(), NULL, 'Gift', 'INCOME', true),
    (gen_random_uuid(), NULL, 'Other', 'INCOME', true);
