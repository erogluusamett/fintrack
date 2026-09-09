import type { Currency, TransactionType } from "./enums"

export interface Transaction {
  id: string
  categoryId: string | null
  categoryName: string | null
  type: TransactionType
  amount: number
  currency: Currency
  transactionDate: string
  description: string | null
  createdAt: string
  updatedAt: string
}

export interface TransactionRequest {
  categoryId: string | null
  type: TransactionType
  amount: number
  currency: Currency
  transactionDate: string
  description?: string | null
}

export interface TransactionFilters {
  type?: TransactionType
  categoryId?: string
  from?: string
  to?: string
  page?: number
  size?: number
  sort?: string
}
