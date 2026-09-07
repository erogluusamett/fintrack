package com.fintrack.audit.entity;

/** Spec'in açıkça istediği kapsam: Transaction create/update/delete, Budget değişiklikleri, Subscription iptali. */
public enum AuditAction {
    TRANSACTION_CREATED,
    TRANSACTION_UPDATED,
    TRANSACTION_DELETED,
    BUDGET_CREATED,
    BUDGET_UPDATED,
    BUDGET_DELETED,
    SUBSCRIPTION_CANCELLED
}
