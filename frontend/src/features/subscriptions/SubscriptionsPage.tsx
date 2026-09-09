import { useMemo, useState } from "react"
import { CalendarClock, Plus, Repeat, Wallet } from "lucide-react"
import { toast } from "sonner"
import { ConfirmDialog } from "@/components/common/ConfirmDialog"
import { EmptyState } from "@/components/common/EmptyState"
import { PageHeader } from "@/components/common/PageHeader"
import { StatCard } from "@/components/common/StatCard"
import { Button } from "@/components/ui/button"
import { Skeleton } from "@/components/ui/skeleton"
import { useAuthStore } from "@/store/auth-store"
import type { Currency } from "@/types/enums"
import type { Subscription } from "@/types/subscription"
import { formatCurrency } from "@/utils/currency"
import { getErrorMessage } from "@/utils/error-message"
import { toMonthlyAmount, toYearlyAmount } from "@/utils/subscription-cost"
import { SubscriptionCard } from "./SubscriptionCard"
import { SubscriptionFormDialog } from "./SubscriptionFormDialog"
import {
  useCancelSubscription,
  useDeleteSubscription,
  useSubscriptions,
} from "./use-subscriptions"

/** Backend'de tek bir "summary" endpoint'i yok; billing cycle normalizasyonu client-side yapılıyor.
 *  Farklı para birimlerini tek bir toplamda yanlışlıkla birleştirmemek için para birimine göre gruplanıyor. */
function summarizeByCurrency(subscriptions: Subscription[]) {
  const active = subscriptions.filter((s) => s.active)
  const totals = new Map<Currency, { monthly: number; yearly: number }>()

  for (const sub of active) {
    const current = totals.get(sub.currency) ?? { monthly: 0, yearly: 0 }
    current.monthly += toMonthlyAmount(sub.amount, sub.billingCycle)
    current.yearly += toYearlyAmount(sub.amount, sub.billingCycle)
    totals.set(sub.currency, current)
  }

  return { activeCount: active.length, totals }
}

export function SubscriptionsPage() {
  const user = useAuthStore((state) => state.user)
  const { data: subscriptions, isLoading } = useSubscriptions()
  const cancelSubscription = useCancelSubscription()
  const deleteSubscription = useDeleteSubscription()

  const [formOpen, setFormOpen] = useState(false)
  const [editingSubscription, setEditingSubscription] = useState<Subscription | undefined>(undefined)
  const [cancellingSubscription, setCancellingSubscription] = useState<Subscription | null>(null)
  const [deletingSubscription, setDeletingSubscription] = useState<Subscription | null>(null)

  const summary = useMemo(() => summarizeByCurrency(subscriptions ?? []), [subscriptions])

  function openCreateDialog() {
    setEditingSubscription(undefined)
    setFormOpen(true)
  }

  function openEditDialog(subscription: Subscription) {
    setEditingSubscription(subscription)
    setFormOpen(true)
  }

  function confirmCancel() {
    if (!cancellingSubscription) return
    cancelSubscription.mutate(cancellingSubscription.id, {
      onSuccess: () => {
        toast.success("Subscription cancelled")
        setCancellingSubscription(null)
      },
      onError: (error) => toast.error(getErrorMessage(error)),
    })
  }

  function confirmDelete() {
    if (!deletingSubscription) return
    deleteSubscription.mutate(deletingSubscription.id, {
      onSuccess: () => {
        toast.success("Subscription deleted")
        setDeletingSubscription(null)
      },
      onError: (error) => toast.error(getErrorMessage(error)),
    })
  }

  const currencyEntries = Array.from(summary.totals.entries())

  return (
    <div className="space-y-6">
      <PageHeader
        title="Subscriptions"
        description="Keep track of every recurring subscription and what it costs you."
        action={
          <Button onClick={openCreateDialog}>
            <Plus className="size-4" />
            Add subscription
          </Button>
        }
      />

      {!isLoading && subscriptions && subscriptions.length > 0 && (
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          <StatCard label="Active subscriptions" value={String(summary.activeCount)} icon={Repeat} />
          <StatCard
            label="Monthly cost"
            value={
              currencyEntries.length > 0
                ? currencyEntries.map(([currency, t]) => formatCurrency(t.monthly, currency)).join(" + ")
                : formatCurrency(0, user?.defaultCurrency ?? "TRY")
            }
            icon={Wallet}
          />
          <StatCard
            label="Yearly cost"
            value={
              currencyEntries.length > 0
                ? currencyEntries.map(([currency, t]) => formatCurrency(t.yearly, currency)).join(" + ")
                : formatCurrency(0, user?.defaultCurrency ?? "TRY")
            }
            icon={CalendarClock}
          />
        </div>
      )}

      {isLoading ? (
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {Array.from({ length: 3 }).map((_, i) => (
            <Skeleton key={i} className="h-36 rounded-xl" />
          ))}
        </div>
      ) : subscriptions && subscriptions.length > 0 ? (
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {subscriptions.map((subscription) => (
            <SubscriptionCard
              key={subscription.id}
              subscription={subscription}
              onEdit={() => openEditDialog(subscription)}
              onCancel={() => setCancellingSubscription(subscription)}
              onDelete={() => setDeletingSubscription(subscription)}
            />
          ))}
        </div>
      ) : (
        <EmptyState
          icon={Repeat}
          title="No subscriptions yet"
          description="Add a subscription to track recurring costs like streaming or memberships."
          action={
            <Button onClick={openCreateDialog}>
              <Plus className="size-4" />
              Add subscription
            </Button>
          }
        />
      )}

      <SubscriptionFormDialog
        open={formOpen}
        onOpenChange={setFormOpen}
        subscription={editingSubscription}
        defaultCurrency={user?.defaultCurrency ?? "TRY"}
      />

      <ConfirmDialog
        open={!!cancellingSubscription}
        onOpenChange={(open) => !open && setCancellingSubscription(null)}
        title="Cancel subscription?"
        description="This subscription will be marked as cancelled but kept in your history."
        confirmLabel="Cancel subscription"
        destructive={false}
        onConfirm={confirmCancel}
        isLoading={cancelSubscription.isPending}
      />

      <ConfirmDialog
        open={!!deletingSubscription}
        onOpenChange={(open) => !open && setDeletingSubscription(null)}
        title="Delete subscription?"
        description="This subscription will be permanently removed. This action can't be undone."
        confirmLabel="Delete"
        onConfirm={confirmDelete}
        isLoading={deleteSubscription.isPending}
      />
    </div>
  )
}
