import type { Currency, RecurringFrequency, TransactionType } from "./enums"

export interface RecurringTransaction {
  id: string
  categoryId: string | null
  categoryName: string | null
  type: TransactionType
  amount: number
  currency: Currency
  frequency: RecurringFrequency
  nextExecutionDate: string
  active: boolean
  description: string | null
}

export interface RecurringTransactionRequest {
  categoryId: string | null
  type: TransactionType
  amount: number
  currency: Currency
  frequency: RecurringFrequency
  nextExecutionDate: string
  description?: string | null
}
