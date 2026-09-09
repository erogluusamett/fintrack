import { api, unwrap } from "./axios"
import type { ApiResponse } from "@/types/api"
import type {
  ComparisonGranularity,
  ComparisonResponse,
  CategoryDistributionItem,
  DashboardResponse,
  Insight,
  TrendPoint,
} from "@/types/analytics"
import type { Currency } from "@/types/enums"

export const analyticsApi = {
  dashboard: (params?: { year?: number; month?: number; currency?: Currency }) =>
    unwrap(api.get<ApiResponse<DashboardResponse>>("/analytics/dashboard", { params })),

  categoryDistribution: (params?: { year?: number; month?: number; currency?: Currency }) =>
    unwrap(
      api.get<ApiResponse<CategoryDistributionItem[]>>("/analytics/category-distribution", { params }),
    ),

  trends: (params?: { months?: number; currency?: Currency }) =>
    unwrap(api.get<ApiResponse<TrendPoint[]>>("/analytics/trends", { params })),

  comparison: (params?: { granularity?: ComparisonGranularity; currency?: Currency }) =>
    unwrap(api.get<ApiResponse<ComparisonResponse>>("/analytics/comparison", { params })),

  insights: (params?: { currency?: Currency }) =>
    unwrap(api.get<ApiResponse<Insight[]>>("/analytics/insights", { params })),
}
