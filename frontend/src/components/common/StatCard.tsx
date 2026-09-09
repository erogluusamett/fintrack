import type { LucideIcon } from "lucide-react"
import { TrendingDown, TrendingUp } from "lucide-react"
import { Card, CardContent } from "@/components/ui/card"
import { cn } from "@/lib/utils"

interface StatCardProps {
  label: string
  value: string
  icon: LucideIcon
  /** Pozitif = geçen aya göre arttı. `changeIsGood` yönü belirler (gelir artışı iyi, gider artışı kötü). */
  changePercentage?: number
  changeIsGood?: "increase" | "decrease"
  iconClassName?: string
}

export function StatCard({
  label,
  value,
  icon: Icon,
  changePercentage,
  changeIsGood = "increase",
  iconClassName,
}: StatCardProps) {
  const hasChange = changePercentage !== undefined && Number.isFinite(changePercentage)
  const isFlat = hasChange && changePercentage === 0
  const increased = hasChange && changePercentage > 0
  const isPositiveSignal = hasChange && (increased ? changeIsGood === "increase" : changeIsGood === "decrease")

  return (
    <Card>
      <CardContent className="flex items-start justify-between gap-3">
        <div className="space-y-1.5">
          <p className="text-sm text-muted-foreground">{label}</p>
          <p className="text-2xl font-semibold tracking-tight">{value}</p>
          {hasChange && (
            <p
              className={cn(
                "flex items-center gap-1 text-xs font-medium",
                isFlat ? "text-muted-foreground" : isPositiveSignal ? "text-success" : "text-destructive",
              )}
            >
              {!isFlat &&
                (increased ? <TrendingUp className="size-3.5" /> : <TrendingDown className="size-3.5" />)}
              {Math.abs(changePercentage).toFixed(1)}% vs last month
            </p>
          )}
        </div>
        <div
          className={cn(
            "flex size-10 shrink-0 items-center justify-center rounded-lg bg-primary/10 text-primary",
            iconClassName,
          )}
        >
          <Icon className="size-5" />
        </div>
      </CardContent>
    </Card>
  )
}
