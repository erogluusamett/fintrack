import { ArrowDownLeft, ArrowUpRight, PiggyBank } from "lucide-react"
import { StatCard } from "@/components/common/StatCard"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Skeleton } from "@/components/ui/skeleton"
import { Tabs, TabsList, TabsTrigger } from "@/components/ui/tabs"
import type { ComparisonGranularity, ComparisonResponse } from "@/types/analytics"
import type { Currency } from "@/types/enums"
import { formatCurrency } from "@/utils/currency"

interface ComparisonSummaryProps {
  data: ComparisonResponse | undefined
  isLoading: boolean
  currency: Currency
  granularity: ComparisonGranularity
  onGranularityChange: (granularity: ComparisonGranularity) => void
}

export function ComparisonSummary({
  data,
  isLoading,
  currency,
  granularity,
  onGranularityChange,
}: ComparisonSummaryProps) {
  return (
    <Card>
      <CardHeader className="flex flex-row items-center justify-between gap-3">
        <CardTitle className="text-base">Period Comparison</CardTitle>
        <Tabs value={granularity} onValueChange={(v) => onGranularityChange(v as ComparisonGranularity)}>
          <TabsList>
            <TabsTrigger value="MONTH">Month</TabsTrigger>
            <TabsTrigger value="YEAR">Year</TabsTrigger>
          </TabsList>
        </Tabs>
      </CardHeader>
      <CardContent>
        {isLoading ? (
          <div className="grid grid-cols-1 gap-4 sm:grid-cols-3">
            {Array.from({ length: 3 }).map((_, i) => (
              <Skeleton key={i} className="h-[104px] rounded-xl" />
            ))}
          </div>
        ) : data ? (
          <div className="grid grid-cols-1 gap-4 sm:grid-cols-3">
            <StatCard
              label="Income"
              value={formatCurrency(data.currentPeriod.income, currency)}
              icon={ArrowDownLeft}
              iconClassName="bg-success/10 text-success"
              changePercentage={data.incomeChangePercentage}
              changeIsGood="increase"
            />
            <StatCard
              label="Expense"
              value={formatCurrency(data.currentPeriod.expense, currency)}
              icon={ArrowUpRight}
              iconClassName="bg-destructive/10 text-destructive"
              changePercentage={data.expenseChangePercentage}
              changeIsGood="decrease"
            />
            <StatCard
              label="Savings"
              value={formatCurrency(data.currentPeriod.savings, currency)}
              icon={PiggyBank}
              iconClassName="bg-primary/10 text-primary"
              changePercentage={data.savingsChangePercentage}
              changeIsGood="increase"
            />
          </div>
        ) : null}
      </CardContent>
    </Card>
  )
}
