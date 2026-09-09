import { useState } from "react"
import { Plus, RefreshCw } from "lucide-react"
import { toast } from "sonner"
import { ConfirmDialog } from "@/components/common/ConfirmDialog"
import { EmptyState } from "@/components/common/EmptyState"
import { PageHeader } from "@/components/common/PageHeader"
import { Button } from "@/components/ui/button"
import { Skeleton } from "@/components/ui/skeleton"
import { useAuthStore } from "@/store/auth-store"
import type { RecurringTransaction } from "@/types/recurring"
import { getErrorMessage } from "@/utils/error-message"
import { RecurringCard } from "./RecurringCard"
import { RecurringFormDialog } from "./RecurringFormDialog"
import {
  useDeleteRecurringTransaction,
  usePauseRecurringTransaction,
  useRecurringTransactions,
  useResumeRecurringTransaction,
} from "./use-recurring"

export function RecurringPaymentsPage() {
  const user = useAuthStore((state) => state.user)
  const { data: recurringTransactions, isLoading } = useRecurringTransactions()
  const pauseRecurring = usePauseRecurringTransaction()
  const resumeRecurring = useResumeRecurringTransaction()
  const deleteRecurring = useDeleteRecurringTransaction()

  const [formOpen, setFormOpen] = useState(false)
  const [editingRecurring, setEditingRecurring] = useState<RecurringTransaction | undefined>(undefined)
  const [deletingRecurring, setDeletingRecurring] = useState<RecurringTransaction | null>(null)
  const [togglingId, setTogglingId] = useState<string | null>(null)

  function openCreateDialog() {
    setEditingRecurring(undefined)
    setFormOpen(true)
  }

  function openEditDialog(recurring: RecurringTransaction) {
    setEditingRecurring(recurring)
    setFormOpen(true)
  }

  function toggleActive(recurring: RecurringTransaction) {
    setTogglingId(recurring.id)
    const mutation = recurring.active ? pauseRecurring : resumeRecurring
    mutation.mutate(recurring.id, {
      onSuccess: () => {
        toast.success(recurring.active ? "Recurring payment paused" : "Recurring payment resumed")
        setTogglingId(null)
      },
      onError: (error) => {
        toast.error(getErrorMessage(error))
        setTogglingId(null)
      },
    })
  }

  function confirmDelete() {
    if (!deletingRecurring) return
    deleteRecurring.mutate(deletingRecurring.id, {
      onSuccess: () => {
        toast.success("Recurring payment deleted")
        setDeletingRecurring(null)
      },
      onError: (error) => toast.error(getErrorMessage(error)),
    })
  }

  return (
    <div className="space-y-6">
      <PageHeader
        title="Recurring Payments"
        description="Automate transactions that repeat on a schedule, like rent or salary."
        action={
          <Button onClick={openCreateDialog}>
            <Plus className="size-4" />
            Add recurring payment
          </Button>
        }
      />

      {isLoading ? (
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {Array.from({ length: 3 }).map((_, i) => (
            <Skeleton key={i} className="h-32 rounded-xl" />
          ))}
        </div>
      ) : recurringTransactions && recurringTransactions.length > 0 ? (
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {recurringTransactions.map((recurring) => (
            <RecurringCard
              key={recurring.id}
              recurringTransaction={recurring}
              isTogglingActive={togglingId === recurring.id}
              onEdit={() => openEditDialog(recurring)}
              onToggleActive={() => toggleActive(recurring)}
              onDelete={() => setDeletingRecurring(recurring)}
            />
          ))}
        </div>
      ) : (
        <EmptyState
          icon={RefreshCw}
          title="No recurring payments yet"
          description="Automate transactions you expect to repeat, like rent, salary, or a gym membership."
          action={
            <Button onClick={openCreateDialog}>
              <Plus className="size-4" />
              Add recurring payment
            </Button>
          }
        />
      )}

      <RecurringFormDialog
        open={formOpen}
        onOpenChange={setFormOpen}
        recurringTransaction={editingRecurring}
        defaultCurrency={user?.defaultCurrency ?? "TRY"}
      />

      <ConfirmDialog
        open={!!deletingRecurring}
        onOpenChange={(open) => !open && setDeletingRecurring(null)}
        title="Delete recurring payment?"
        description="This recurring payment will be permanently removed. This action can't be undone."
        confirmLabel="Delete"
        onConfirm={confirmDelete}
        isLoading={deleteRecurring.isPending}
      />
    </div>
  )
}
