import { MoreVertical, Pause, Pencil, Play, Trash2 } from "lucide-react"
import { Badge } from "@/components/ui/badge"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardHeader } from "@/components/ui/card"
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu"
import type { RecurringTransaction } from "@/types/recurring"
import { formatCurrency, formatDate } from "@/utils/currency"
import { formatEnumLabel } from "@/utils/format"

const TYPE_BADGE_VARIANT: Record<RecurringTransaction["type"], "secondary" | "default" | "outline"> = {
  INCOME: "secondary",
  EXPENSE: "default",
  TRANSFER: "outline",
}

interface RecurringCardProps {
  recurringTransaction: RecurringTransaction
  isTogglingActive: boolean
  onEdit: () => void
  onToggleActive: () => void
  onDelete: () => void
}

export function RecurringCard({
  recurringTransaction,
  isTogglingActive,
  onEdit,
  onToggleActive,
  onDelete,
}: RecurringCardProps) {
  const r = recurringTransaction

  return (
    <Card>
      <CardHeader className="flex flex-row items-start justify-between gap-2 pb-2">
        <div>
          <p className="text-sm font-medium">{r.description || r.categoryName || formatEnumLabel(r.type)}</p>
          <p className="text-xs text-muted-foreground">
            {r.categoryName ?? "No category"} · {formatEnumLabel(r.frequency)}
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
            <DropdownMenuItem onClick={onToggleActive} disabled={isTogglingActive}>
              {r.active ? (
                <>
                  <Pause className="size-4" /> Pause
                </>
              ) : (
                <>
                  <Play className="size-4" /> Resume
                </>
              )}
            </DropdownMenuItem>
            <DropdownMenuItem variant="destructive" onClick={onDelete}>
              <Trash2 className="size-4" /> Delete
            </DropdownMenuItem>
          </DropdownMenuContent>
        </DropdownMenu>
      </CardHeader>
      <CardContent className="space-y-3">
        <p className="text-lg font-semibold">{formatCurrency(r.amount, r.currency)}</p>
        <div className="flex items-center justify-between text-xs">
          <div className="flex items-center gap-1.5">
            <Badge variant={TYPE_BADGE_VARIANT[r.type]}>{formatEnumLabel(r.type)}</Badge>
            <Badge variant={r.active ? "secondary" : "outline"}>{r.active ? "Active" : "Paused"}</Badge>
          </div>
          <span className="text-muted-foreground">Next: {formatDate(r.nextExecutionDate)}</span>
        </div>
      </CardContent>
    </Card>
  )
}
