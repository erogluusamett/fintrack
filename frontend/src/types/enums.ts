/** Backend enum'larıyla birebir eşleşir (bkz. fintrack backend com.fintrack.*.entity paketleri). */

export const CURRENCIES = ["TRY", "USD", "EUR", "GBP"] as const
export type Currency = (typeof CURRENCIES)[number]

export const TRANSACTION_TYPES = ["INCOME", "EXPENSE", "TRANSFER"] as const
export type TransactionType = (typeof TRANSACTION_TYPES)[number]

export const CATEGORY_TYPES = ["INCOME", "EXPENSE"] as const
export type CategoryType = (typeof CATEGORY_TYPES)[number]

export const BUDGET_PERIODS = ["WEEKLY", "MONTHLY", "YEARLY"] as const
export type BudgetPeriod = (typeof BUDGET_PERIODS)[number]

export const BUDGET_STATUSES = ["OK", "WARNING", "EXCEEDED"] as const
export type BudgetStatus = (typeof BUDGET_STATUSES)[number]

export const BILLING_CYCLES = ["WEEKLY", "MONTHLY", "YEARLY"] as const
export type BillingCycle = (typeof BILLING_CYCLES)[number]

export const RECURRING_FREQUENCIES = ["DAILY", "WEEKLY", "MONTHLY", "YEARLY"] as const
export type RecurringFrequency = (typeof RECURRING_FREQUENCIES)[number]

export const NOTIFICATION_TYPES = [
  "BUDGET_WARNING",
  "SUBSCRIPTION_REMINDER",
  "PAYMENT_REMINDER",
  "FINANCIAL_INSIGHT",
  "SYSTEM",
] as const
export type NotificationType = (typeof NOTIFICATION_TYPES)[number]
