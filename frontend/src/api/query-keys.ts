/**
 * Tüm query key'lerin tek kaynağı — mutation'lardan sonra invalidateQueries
 * çağrılarının doğru key'i tuttuğundan emin olmak için (bkz. örn.
 * transactions/use-transactions.ts: bir transaction oluşturulduğunda hem
 * transactions hem dashboard invalidate edilir).
 */
export const queryKeys = {
  me: ["me"] as const,
  categories: ["categories"] as const,
  transactions: (filters?: unknown) => ["transactions", filters] as const,
  transaction: (id: string) => ["transactions", id] as const,
  budgets: ["budgets"] as const,
  budgetStatus: (id: string) => ["budgets", id, "status"] as const,
  subscriptions: ["subscriptions"] as const,
  recurring: ["recurring-transactions"] as const,
  notifications: (page?: number) => ["notifications", page] as const,
  dashboard: (params?: unknown) => ["analytics", "dashboard", params] as const,
  categoryDistribution: (params?: unknown) => ["analytics", "category-distribution", params] as const,
  trends: (params?: unknown) => ["analytics", "trends", params] as const,
  comparison: (params?: unknown) => ["analytics", "comparison", params] as const,
  insights: (params?: unknown) => ["analytics", "insights", params] as const,
}
