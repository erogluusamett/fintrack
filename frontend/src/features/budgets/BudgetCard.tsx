import { MoreVertical, Pencil, Trash2 } from "lucide-react"
import { Badge } from "@/components/ui/badge"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardHeader } from "@/components/ui/card"
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu"
import { BudgetProgressBar } from "@/components/common/BudgetProgressBar"
import type { Budget, BudgetStatusResponse } from "@/types/budget"
import { formatCurrency, formatPercentage } from "@/utils/currency"
import { formatEnumLabel } from "@/utils/format"

const STATUS_BADGE_VARIANT: Record<BudgetStatusResponse["status"], "default" | "secondary" | "destructive"> = {
  OK: "secondary",
  WARNING: "default",
  EXCEEDED: "destructive",
}

interface BudgetCardProps {
  budget: Budget
  status?: BudgetStatusResponse
  isStatusLoading: boolean
  onEdit: () => void
  onDelete: () => void
}

export function BudgetCard({ budget, status, isStatusLoading, onEdit, onDelete }: BudgetCardProps) {
  return (
    <Card>
      <CardHeader className="flex flex-row items-start justify-between gap-2 pb-2">
        <div>
          <p className="text-sm font-medium">{budget.categoryName ?? "General"}</p>
          <p className="text-xs text-muted-foreground">
            {formatEnumLabel(budget.period)} · since {new Date(budget.startDate).toLocaleDateString()}
          </p>
        </div>
        <DropdownMenu>
          <DropdownMenuTrigger
            render={<Button variant="ghost" size="icon" className="size-8 shrink-0" />}
          >
            <MoreVertical className="size-4" />
          </DropdownMenuTrigger>
          <DropdownMenuContent align="end">
            <DropdownMenuItem onClick={onEdit}>
              <Pencil className="size-4" /> Edit
            </DropdownMenuItem>
            <DropdownMenuItem variant="destructive" onClick={onDelete}>
              <Trash2 className="size-4" /> Delete
            </DropdownMenuItem>
          </DropdownMenuContent>
        </DropdownMenu>
      </CardHeader>
      <CardContent className="space-y-3">
        {isStatusLoading || !status ? (
          <div className="h-16 animate-pulse rounded-md bg-muted" />
        ) : (
          <>
            <div className="flex items-baseline justify-between">
              <span className="text-lg font-semibold">{formatCurrency(status.spent, budget.currency)}</span>
              <span className="text-sm text-muted-foreground">
                of {formatCurrency(status.limit, budget.currency)}
              </span>
            </div>
            <BudgetProgressBar usagePercentage={status.usagePercentage} status={status.status} />
            <div className="flex items-center justify-between text-xs">
              <Badge variant={STATUS_BADGE_VARIANT[status.status]}>{formatEnumLabel(status.status)}</Badge>
              <span className="text-muted-foreground">
                {formatPercentage(status.usagePercentage)} used ·{" "}
                {formatCurrency(status.remaining, budget.currency)} left
              </span>
            </div>
          </>
        )}
      </CardContent>
    </Card>
  )
}
