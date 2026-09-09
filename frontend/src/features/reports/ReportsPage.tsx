import { useMemo, useState } from "react"
import { Download, FileBarChart } from "lucide-react"
import { EmptyState } from "@/components/common/EmptyState"
import { PageHeader } from "@/components/common/PageHeader"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Input } from "@/components/ui/input"
import { Skeleton } from "@/components/ui/skeleton"
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table"
import type { Currency } from "@/types/enums"
import type { Transaction } from "@/types/transaction"
import { downloadCsv, toCsv } from "@/utils/csv"
import { formatCurrency, formatDate, formatPercentage } from "@/utils/currency"
import { useReportTransactions } from "./use-report-transactions"

function startOfMonth(): string {
  const now = new Date()
  return new Date(now.getFullYear(), now.getMonth(), 1).toISOString().slice(0, 10)
}

function today(): string {
  return new Date().toISOString().slice(0, 10)
}

interface CurrencySummary {
  income: number
  expense: number
  net: number
  expenseByCategory: Map<string, number>
}

/** Farklı para birimlerini tek toplamda birleştirmemek için currency'e göre gruplanıyor
 *  (bkz. Subscriptions sayfasındaki aynı yaklaşım — özel bir report/summary endpoint'i yok). */
function summarizeByCurrency(transactions: Transaction[]): Map<Currency, CurrencySummary> {
  const result = new Map<Currency, CurrencySummary>()

  for (const tx of transactions) {
    if (tx.type === "TRANSFER") continue
    const summary = result.get(tx.currency) ?? {
      income: 0,
      expense: 0,
      net: 0,
      expenseByCategory: new Map<string, number>(),
    }
    if (tx.type === "INCOME") {
      summary.income += tx.amount
      summary.net += tx.amount
    } else {
      summary.expense += tx.amount
      summary.net -= tx.amount
      const categoryName = tx.categoryName ?? "Uncategorized"
      summary.expenseByCategory.set(categoryName, (summary.expenseByCategory.get(categoryName) ?? 0) + tx.amount)
    }
    result.set(tx.currency, summary)
  }

  return result
}

export function ReportsPage() {
  const [from, setFrom] = useState(startOfMonth())
  const [to, setTo] = useState(today())

  const { data, isLoading } = useReportTransactions(from, to)
  const transactions = useMemo(() => data?.content ?? [], [data])
  const summaries = useMemo(() => summarizeByCurrency(transactions), [transactions])

  function exportCsv() {
    const csv = toCsv(
      ["Date", "Description", "Category", "Type", "Amount", "Currency"],
      transactions.map((tx) => [
        tx.transactionDate,
        tx.description ?? "",
        tx.categoryName ?? "Uncategorized",
        tx.type,
        tx.amount,
        tx.currency,
      ]),
    )
    downloadCsv(`fintrack-transactions_${from}_to_${to}.csv`, csv)
  }

  return (
    <div className="space-y-6">
      <PageHeader
        title="Reports"
        description="Summarize and export your transactions for a date range."
        action={
          <Button onClick={exportCsv} disabled={transactions.length === 0}>
            <Download className="size-4" />
            Export CSV
          </Button>
        }
      />

      <div className="flex flex-wrap items-end gap-3 rounded-xl border border-border bg-card p-4">
        <div className="space-y-1.5">
          <label className="text-xs font-medium text-muted-foreground" htmlFor="report-from">
            From
          </label>
          <Input id="report-from" type="date" value={from} onChange={(e) => setFrom(e.target.value)} />
        </div>
        <div className="space-y-1.5">
          <label className="text-xs font-medium text-muted-foreground" htmlFor="report-to">
            To
          </label>
          <Input id="report-to" type="date" value={to} onChange={(e) => setTo(e.target.value)} />
        </div>
      </div>

      {isLoading ? (
        <div className="space-y-4">
          <Skeleton className="h-32 rounded-xl" />
          <Skeleton className="h-64 rounded-xl" />
        </div>
      ) : transactions.length === 0 ? (
        <EmptyState
          icon={FileBarChart}
          title="No transactions in this range"
          description="Pick a different date range, or add some transactions first."
        />
      ) : (
        Array.from(summaries.entries()).map(([currency, summary]) => {
          const categoryRows = Array.from(summary.expenseByCategory.entries()).sort((a, b) => b[1] - a[1])

          return (
            <div key={currency} className="space-y-4">
              <div className="grid grid-cols-1 gap-4 sm:grid-cols-3">
                <Card>
                  <CardContent>
                    <p className="text-sm text-muted-foreground">Income ({currency})</p>
                    <p className="mt-1.5 text-2xl font-semibold text-success">
                      {formatCurrency(summary.income, currency)}
                    </p>
                  </CardContent>
                </Card>
                <Card>
                  <CardContent>
                    <p className="text-sm text-muted-foreground">Expense ({currency})</p>
                    <p className="mt-1.5 text-2xl font-semibold text-destructive">
                      {formatCurrency(summary.expense, currency)}
                    </p>
                  </CardContent>
                </Card>
                <Card>
                  <CardContent>
                    <p className="text-sm text-muted-foreground">Net ({currency})</p>
                    <p className="mt-1.5 text-2xl font-semibold">{formatCurrency(summary.net, currency)}</p>
                  </CardContent>
                </Card>
              </div>

              {categoryRows.length > 0 && (
                <Card>
                  <CardHeader>
                    <CardTitle className="text-base">Expenses by Category ({currency})</CardTitle>
                  </CardHeader>
                  <CardContent className="px-0 pt-0">
                    <Table>
                      <TableHeader>
                        <TableRow>
                          <TableHead>Category</TableHead>
                          <TableHead className="text-right">Amount</TableHead>
                          <TableHead className="text-right">% of Expenses</TableHead>
                        </TableRow>
                      </TableHeader>
                      <TableBody>
                        {categoryRows.map(([categoryName, amount]) => (
                          <TableRow key={categoryName}>
                            <TableCell className="font-medium">{categoryName}</TableCell>
                            <TableCell className="text-right">{formatCurrency(amount, currency)}</TableCell>
                            <TableCell className="text-right text-muted-foreground">
                              {formatPercentage(summary.expense > 0 ? (amount / summary.expense) * 100 : 0)}
                            </TableCell>
                          </TableRow>
                        ))}
                      </TableBody>
                    </Table>
                  </CardContent>
                </Card>
              )}
            </div>
          )
        })
      )}

      {!isLoading && transactions.length > 0 && (
        <Card>
          <CardHeader>
            <CardTitle className="text-base">All Transactions ({transactions.length})</CardTitle>
          </CardHeader>
          <CardContent className="px-0 pt-0">
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Date</TableHead>
                  <TableHead>Description</TableHead>
                  <TableHead>Category</TableHead>
                  <TableHead>Type</TableHead>
                  <TableHead className="text-right">Amount</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {transactions.map((tx) => (
                  <TableRow key={tx.id}>
                    <TableCell className="whitespace-nowrap text-muted-foreground">
                      {formatDate(tx.transactionDate)}
                    </TableCell>
                    <TableCell className="max-w-[220px] truncate">{tx.description || "—"}</TableCell>
                    <TableCell className="text-muted-foreground">{tx.categoryName ?? "Uncategorized"}</TableCell>
                    <TableCell className="text-muted-foreground">{tx.type}</TableCell>
                    <TableCell
                      className={
                        tx.type === "EXPENSE"
                          ? "text-right font-medium text-destructive whitespace-nowrap"
                          : "text-right font-medium text-success whitespace-nowrap"
                      }
                    >
                      {tx.type === "EXPENSE" ? "-" : "+"}
                      {formatCurrency(tx.amount, tx.currency)}
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </CardContent>
        </Card>
      )}
    </div>
  )
}
