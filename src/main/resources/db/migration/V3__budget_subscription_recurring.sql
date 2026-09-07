-- FinTrack V3: budget, subscription & recurring transaction domain

CREATE TABLE budgets (
    id            UUID PRIMARY KEY,
    user_id       UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    category_id   UUID REFERENCES categories (id) ON DELETE CASCADE,
    period        VARCHAR(20) NOT NULL,
    amount_limit  NUMERIC(19, 2) NOT NULL,
    currency      VARCHAR(3) NOT NULL,
    start_date    DATE NOT NULL,
    end_date      DATE NOT NULL,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_budgets_user_id ON budgets (user_id);

CREATE TABLE subscriptions (
    id                 UUID PRIMARY KEY,
    user_id            UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    category_id        UUID REFERENCES categories (id) ON DELETE SET NULL,
    name               VARCHAR(150) NOT NULL,
    amount             NUMERIC(19, 2) NOT NULL,
    currency           VARCHAR(3) NOT NULL,
    billing_cycle      VARCHAR(20) NOT NULL,
    next_billing_date  DATE NOT NULL,
    active             BOOLEAN NOT NULL DEFAULT true,
    created_at         TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at         TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_subscriptions_user_id ON subscriptions (user_id);

CREATE TABLE recurring_transactions (
    id                   UUID PRIMARY KEY,
    user_id              UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    category_id          UUID REFERENCES categories (id) ON DELETE SET NULL,
    type                 VARCHAR(20) NOT NULL,
    amount               NUMERIC(19, 2) NOT NULL,
    currency             VARCHAR(3) NOT NULL,
    frequency            VARCHAR(20) NOT NULL,
    next_execution_date  DATE NOT NULL,
    active               BOOLEAN NOT NULL DEFAULT true,
    description          VARCHAR(500),
    created_at           TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at           TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_recurring_transactions_user_id ON recurring_transactions (user_id);
CREATE INDEX idx_recurring_transactions_next_execution ON recurring_transactions (next_execution_date) WHERE active = true;
