import { useQuery } from "@tanstack/react-query"
import {
  ArrowDownLeft,
  ArrowUpRight,
  PiggyBank,
  Receipt,
  Percent,
  CalendarClock,
} from "lucide-react"
import { analyticsApi } from "@/api/analytics.api"
import { subscriptionApi } from "@/api/subscription.api"
import { transactionApi } from "@/api/transaction.api"
import { queryKeys } from "@/api/query-keys"
import { BudgetProgressBar } from "@/components/common/BudgetProgressBar"
import { EmptyState } from "@/components/common/EmptyState"
import { PageHeader } from "@/components/common/PageHeader"
import { StatCard } from "@/components/common/StatCard"
import { Badge } from "@/components/ui/badge"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Skeleton } from "@/components/ui/skeleton"
import { useAuthStore } from "@/store/auth-store"
import { formatCurrency, formatDate } from "@/utils/currency"

export function DashboardPage() {
  const user = useAuthStore((state) => state.user)
  const currency = user?.defaultCurrency ?? "TRY"

  const dashboardQuery = useQuery({
    queryKey: queryKeys.dashboard({ currency }),
    queryFn: () => analyticsApi.dashboard({ currency }),
  })

  const comparisonQuery = useQuery({
    queryKey: queryKeys.comparison({ currency }),
    queryFn: () => analyticsApi.comparison({ currency, granularity: "MONTH" }),
  })

  const recentTransactionsQuery = useQuery({
    queryKey: queryKeys.transactions({ page: 0, size: 5, sort: "transactionDate,desc" }),
    queryFn: () => transactionApi.list({ page: 0, size: 5, sort: "transactionDate,desc" }),
  })

  const subscriptionsQuery = useQuery({
    queryKey: queryKeys.subscriptions,
    queryFn: () => subscriptionApi.list(),
  })

  const upcomingSubscriptions = (subscriptionsQuery.data ?? [])
    .filter((s) => s.active)
    .sort((a, b) => a.nextBillingDate.localeCompare(b.nextBillingDate))
    .slice(0, 4)

  const dashboard = dashboardQuery.data
  const comparison = comparisonQuery.data

  return (
    <div className="space-y-6">
      <PageHeader
        title={`Welcome back${user ? `, ${user.firstName}` : ""}`}
        description="Here's what's happening with your money this month."
      />

      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
        {dashboardQuery.isLoading ? (
          Array.from({ length: 4 }).map((_, i) => <Skeleton key={i} className="h-[104px] rounded-xl" />)
        ) : (
          <>
            <StatCard
              label="Monthly Income"
              value={formatCurrency(dashboard?.monthlyIncome ?? 0, currency)}
              icon={ArrowDownLeft}
              iconClassName="bg-success/10 text-success"
              changePercentage={comparison?.incomeChangePercentage}
              changeIsGood="increase"
            />
            <StatCard
              label="Monthly Expenses"
              value={formatCurrency(dashboard?.monthlyExpense ?? 0, currency)}
              icon={ArrowUpRight}
              iconClassName="bg-destructive/10 text-destructive"
              changePercentage={comparison?.expenseChangePercentage}
              changeIsGood="decrease"
            />
            <StatCard
              label="Monthly Savings"
              value={formatCurrency(dashboard?.savings ?? 0, currency)}
              icon={PiggyBank}
              iconClassName="bg-primary/10 text-primary"
              changePercentage={comparison?.savingsChangePercentage}
              changeIsGood="increase"
            />
            <StatCard
              label="Savings Rate"
              value={`${(dashboard?.savingsRate ?? 0).toFixed(1)}%`}
              icon={Percent}
              iconClassName="bg-warning/10 text-warning"
            />
          </>
        )}
      </div>

      <div className="grid grid-cols-1 gap-4 lg:grid-cols-3">
        <Card className="lg:col-span-2">
          <CardHeader>
            <CardTitle className="text-base">Recent Transactions</CardTitle>
          </CardHeader>
          <CardContent>
            {recentTransactionsQuery.isLoading ? (
              <div className="space-y-3">
                {Array.from({ length: 4 }).map((_, i) => (
                  <Skeleton key={i} className="h-12 rounded-lg" />
                ))}
              </div>
            ) : recentTransactionsQuery.data?.content.length ? (
              <ul className="divide-y divide-border">
                {recentTransactionsQuery.data.content.map((tx) => (
                  <li key={tx.id} className="flex items-center justify-between gap-3 py-3">
                    <div className="flex items-center gap-3 min-w-0">
                      <div
                        className={
                          tx.type === "EXPENSE"
                            ? "flex size-9 shrink-0 items-center justify-center rounded-full bg-destructive/10 text-destructive"
                            : "flex size-9 shrink-0 items-center justify-center rounded-full bg-success/10 text-success"
                        }
                      >
                        {tx.type === "EXPENSE" ? (
                          <ArrowUpRight className="size-4" />
                        ) : (
                          <ArrowDownLeft className="size-4" />
                        )}
                      </div>
                      <div className="min-w-0">
                        <p className="truncate text-sm font-medium">
                          {tx.description || tx.categoryName || tx.type}
                        </p>
                        <p className="text-xs text-muted-foreground">
                          {tx.categoryName ?? "Uncategorized"} · {formatDate(tx.transactionDate)}
                        </p>
                      </div>
                    </div>
                    <span
                      className={
                        tx.type === "EXPENSE"
                          ? "shrink-0 text-sm font-medium text-destructive"
                          : "shrink-0 text-sm font-medium text-success"
                      }
                    >
                      {tx.type === "EXPENSE" ? "-" : "+"}
                      {formatCurrency(tx.amount, tx.currency)}
                    </span>
                  </li>
                ))}
              </ul>
            ) : (
              <EmptyState
                icon={Receipt}
                title="No transactions yet"
                description="Start tracking your finances by adding your first transaction."
              />
            )}
          </CardContent>
        </Card>

        <div className="space-y-4">
          <Card>
            <CardHeader>
              <CardTitle className="text-base">Budget Overview</CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              {dashboardQuery.isLoading ? (
                <Skeleton className="h-20 rounded-lg" />
              ) : dashboard?.budgetUsage.length ? (
                dashboard.budgetUsage.map((budget) => (
                  <div key={budget.budgetId} className="space-y-1.5">
                    <div className="flex items-center justify-between text-sm">
                      <span className="font-medium">{budget.categoryName}</span>
                      <span className="text-muted-foreground">{budget.usagePercentage.toFixed(0)}%</span>
                    </div>
                    <BudgetProgressBar usagePercentage={budget.usagePercentage} status={budget.status} />
                  </div>
                ))
              ) : (
                <p className="text-sm text-muted-foreground">No active budgets for this period.</p>
              )}
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle className="text-base">Upcoming Payments</CardTitle>
            </CardHeader>
            <CardContent className="space-y-3">
              {subscriptionsQuery.isLoading ? (
                <Skeleton className="h-16 rounded-lg" />
              ) : upcomingSubscriptions.length ? (
                upcomingSubscriptions.map((sub) => (
                  <div key={sub.id} className="flex items-center justify-between gap-3">
                    <div className="flex items-center gap-2.5 min-w-0">
                      <div className="flex size-8 shrink-0 items-center justify-center rounded-full bg-muted text-muted-foreground">
                        <CalendarClock className="size-4" />
                      </div>
                      <div className="min-w-0">
                        <p className="truncate text-sm font-medium">{sub.name}</p>
                        <p className="text-xs text-muted-foreground">{formatDate(sub.nextBillingDate)}</p>
                      </div>
                    </div>
                    <Badge variant="secondary" className="shrink-0">
                      {formatCurrency(sub.amount, sub.currency)}
                    </Badge>
                  </div>
                ))
              ) : (
                <p className="text-sm text-muted-foreground">No upcoming payments.</p>
              )}
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  )
}
