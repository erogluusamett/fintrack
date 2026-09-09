import { MoreVertical, Pencil, Trash2, XCircle } from "lucide-react"
import { Badge } from "@/components/ui/badge"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardHeader } from "@/components/ui/card"
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu"
import type { Subscription } from "@/types/subscription"
import { formatCurrency, formatDate } from "@/utils/currency"
import { formatEnumLabel } from "@/utils/format"

interface SubscriptionCardProps {
  subscription: Subscription
  onEdit: () => void
  onCancel: () => void
  onDelete: () => void
}

export function SubscriptionCard({ subscription, onEdit, onCancel, onDelete }: SubscriptionCardProps) {
  return (
    <Card>
      <CardHeader className="flex flex-row items-start justify-between gap-2 pb-2">
        <div>
          <p className="text-sm font-medium">{subscription.name}</p>
          <p className="text-xs text-muted-foreground">
            {subscription.categoryName ?? "No category"} · {formatEnumLabel(subscription.billingCycle)}
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
            {subscription.active && (
              <DropdownMenuItem onClick={onCancel}>
                <XCircle className="size-4" /> Cancel
              </DropdownMenuItem>
            )}
            <DropdownMenuItem variant="destructive" onClick={onDelete}>
              <Trash2 className="size-4" /> Delete
            </DropdownMenuItem>
          </DropdownMenuContent>
        </DropdownMenu>
      </CardHeader>
      <CardContent className="space-y-3">
        <p className="text-lg font-semibold">
          {formatCurrency(subscription.amount, subscription.currency)}
          <span className="text-sm font-normal text-muted-foreground">
            {" "}
            / {formatEnumLabel(subscription.billingCycle).toLowerCase()}
          </span>
        </p>
        <div className="flex items-center justify-between text-xs">
          <Badge variant={subscription.active ? "secondary" : "outline"}>
            {subscription.active ? "Active" : "Cancelled"}
          </Badge>
          <span className="text-muted-foreground">Next: {formatDate(subscription.nextBillingDate)}</span>
        </div>
      </CardContent>
    </Card>
  )
}
