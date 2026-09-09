import { useState } from "react"
import { PageHeader } from "@/components/common/PageHeader"
import { useAuthStore } from "@/store/auth-store"
import type { ComparisonGranularity } from "@/types/analytics"
import { CategoryDistributionChart } from "./CategoryDistributionChart"
import { ComparisonSummary } from "./ComparisonSummary"
import { InsightsList } from "./InsightsList"
import { TrendsChart } from "./TrendsChart"
import { useCategoryDistribution, useComparison, useInsights, useTrends } from "./use-analytics"

export function AnalyticsPage() {
  const user = useAuthStore((state) => state.user)
  const currency = user?.defaultCurrency ?? "TRY"
  const [granularity, setGranularity] = useState<ComparisonGranularity>("MONTH")

  const categoryDistributionQuery = useCategoryDistribution(currency)
  const trendsQuery = useTrends(currency)
  const comparisonQuery = useComparison(currency, granularity)
  const insightsQuery = useInsights(currency)

  return (
    <div className="space-y-6">
      <PageHeader title="Analytics" description="Understand where your money goes and how it's trending." />

      <ComparisonSummary
        data={comparisonQuery.data}
        isLoading={comparisonQuery.isLoading}
        currency={currency}
        granularity={granularity}
        onGranularityChange={setGranularity}
      />

      <TrendsChart data={trendsQuery.data} isLoading={trendsQuery.isLoading} currency={currency} />

      <div className="grid grid-cols-1 gap-4 lg:grid-cols-2">
        <CategoryDistributionChart
          data={categoryDistributionQuery.data}
          isLoading={categoryDistributionQuery.isLoading}
          currency={currency}
        />
        <InsightsList insights={insightsQuery.data} isLoading={insightsQuery.isLoading} />
      </div>
    </div>
  )
}
