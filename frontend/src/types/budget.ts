import type { BudgetPeriod, BudgetStatus, Currency } from "./enums"

export interface Budget {
  id: string
  categoryId: string | null
  categoryName: string | null
  period: BudgetPeriod
  amountLimit: number
  currency: Currency
  startDate: string
  endDate: string
}

export interface BudgetRequest {
  categoryId: string | null
  period: BudgetPeriod
  amountLimit: number
  currency: Currency
  startDate: string
}

export interface BudgetStatusResponse {
  budgetId: string
  limit: number
  spent: number
  remaining: number
  usagePercentage: number
  status: BudgetStatus
}
