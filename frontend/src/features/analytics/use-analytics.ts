import { useQuery } from "@tanstack/react-query"
import { analyticsApi } from "@/api/analytics.api"
import { queryKeys } from "@/api/query-keys"
import type { ComparisonGranularity } from "@/types/analytics"
import type { Currency } from "@/types/enums"

export function useCategoryDistribution(currency: Currency) {
  return useQuery({
    queryKey: queryKeys.categoryDistribution({ currency }),
    queryFn: () => analyticsApi.categoryDistribution({ currency }),
  })
}

export function useTrends(currency: Currency, months = 6) {
  return useQuery({
    queryKey: queryKeys.trends({ currency, months }),
    queryFn: () => analyticsApi.trends({ currency, months }),
  })
}

export function useComparison(currency: Currency, granularity: ComparisonGranularity) {
  return useQuery({
    queryKey: queryKeys.comparison({ currency, granularity }),
    queryFn: () => analyticsApi.comparison({ currency, granularity }),
  })
}

export function useInsights(currency: Currency) {
  return useQuery({
    queryKey: queryKeys.insights({ currency }),
    queryFn: () => analyticsApi.insights({ currency }),
  })
}
