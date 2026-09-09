import {
  Lightbulb,
  PiggyBank,
  Repeat,
  Scale,
  TrendingUpDown,
  type LucideIcon,
} from "lucide-react"
import { EmptyState } from "@/components/common/EmptyState"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Skeleton } from "@/components/ui/skeleton"
import type { Insight, InsightType } from "@/types/analytics"

const INSIGHT_ICON: Record<InsightType, LucideIcon> = {
  SPENDING_CHANGE: TrendingUpDown,
  INCOME_EXPENSE_RATIO: Scale,
  SUBSCRIPTION_ANNUAL_COST: Repeat,
  SAVINGS_TREND: PiggyBank,
  CATEGORY_SPENDING_CHANGE: TrendingUpDown,
}

interface InsightsListProps {
  insights: Insight[] | undefined
  isLoading: boolean
}

export function InsightsList({ insights, isLoading }: InsightsListProps) {
  return (
    <Card>
      <CardHeader>
        <CardTitle className="text-base">Smart Insights</CardTitle>
      </CardHeader>
      <CardContent>
        {isLoading ? (
          <div className="space-y-3">
            {Array.from({ length: 3 }).map((_, i) => (
              <Skeleton key={i} className="h-16 rounded-lg" />
            ))}
          </div>
        ) : insights && insights.length > 0 ? (
          <ul className="space-y-3">
            {insights.map((insight, i) => {
              const Icon = INSIGHT_ICON[insight.type] ?? Lightbulb
              return (
                <li key={i} className="flex items-start gap-3 rounded-lg border border-border p-3">
                  <div className="flex size-9 shrink-0 items-center justify-center rounded-full bg-primary/10 text-primary">
                    <Icon className="size-4.5" />
                  </div>
                  <div className="min-w-0">
                    <p className="text-sm font-medium">{insight.title}</p>
                    <p className="mt-0.5 text-sm text-muted-foreground">{insight.description}</p>
                  </div>
                </li>
              )
            })}
          </ul>
        ) : (
          <EmptyState
            icon={Lightbulb}
            title="No insights yet"
            description="Keep tracking your finances — insights appear once there's enough activity to analyze."
          />
        )}
      </CardContent>
    </Card>
  )
}
