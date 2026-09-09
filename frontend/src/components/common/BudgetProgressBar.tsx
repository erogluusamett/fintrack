import { cn } from "@/lib/utils"
import type { BudgetStatus } from "@/types/enums"

const STATUS_COLOR: Record<BudgetStatus, string> = {
  OK: "bg-success",
  WARNING: "bg-warning",
  EXCEEDED: "bg-destructive",
}

interface BudgetProgressBarProps {
  usagePercentage: number
  status: BudgetStatus
}

export function BudgetProgressBar({ usagePercentage, status }: BudgetProgressBarProps) {
  const width = Math.min(usagePercentage, 100)

  return (
    <div className="h-1.5 w-full overflow-hidden rounded-full bg-muted">
      <div
        className={cn("h-full rounded-full transition-all", STATUS_COLOR[status])}
        style={{ width: `${width}%` }}
      />
    </div>
  )
}
