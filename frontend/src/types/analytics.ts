import type { BudgetStatus, Currency, TransactionType } from "./enums"

export interface CategoryAmount {
  categoryId: string
  categoryName: string
  amount: number
}

export interface DashboardResponse {
  period: string
  currency: Currency
  monthlyIncome: number
  monthlyExpense: number
  savings: number
  savingsRate: number
  subscriptionCost: number
  topSpendingCategory: CategoryAmount | null
  highestExpense: {
    transactionId: string
    categoryName: string | null
    amount: number
    date: string
    description: string | null
  } | null
  budgetUsage: {
    budgetId: string
    categoryName: string
    usagePercentage: number
    status: BudgetStatus
  }[]
}

export interface CategoryDistributionItem {
  categoryId: string
  categoryName: string
  amount: number
  percentage: number
}

export interface TrendPoint {
  period: string
  income: number
  expense: number
  savings: number
}

export interface PeriodSummary {
  period: string
  income: number
  expense: number
  savings: number
}

export interface ComparisonResponse {
  currentPeriod: PeriodSummary
  previousPeriod: PeriodSummary
  incomeChangePercentage: number
  expenseChangePercentage: number
  savingsChangePercentage: number
}

export const INSIGHT_TYPES = [
  "SPENDING_CHANGE",
  "INCOME_EXPENSE_RATIO",
  "SUBSCRIPTION_ANNUAL_COST",
  "SAVINGS_TREND",
  "CATEGORY_SPENDING_CHANGE",
] as const
export type InsightType = (typeof INSIGHT_TYPES)[number]

export interface Insight {
  type: InsightType
  title: string
  description: string
}

export type ComparisonGranularity = "MONTH" | "YEAR"

// Yalnızca TRANSFER'de kategori opsiyonel — bkz. backend CategoryAccessService.
export type { TransactionType }
