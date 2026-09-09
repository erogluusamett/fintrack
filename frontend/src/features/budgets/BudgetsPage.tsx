import { useState } from "react"
import { PiggyBank, Plus } from "lucide-react"
import { toast } from "sonner"
import { ConfirmDialog } from "@/components/common/ConfirmDialog"
import { EmptyState } from "@/components/common/EmptyState"
import { PageHeader } from "@/components/common/PageHeader"
import { Button } from "@/components/ui/button"
import { Skeleton } from "@/components/ui/skeleton"
import { useAuthStore } from "@/store/auth-store"
import type { Budget } from "@/types/budget"
import { getErrorMessage } from "@/utils/error-message"
import { BudgetCard } from "./BudgetCard"
import { BudgetFormDialog } from "./BudgetFormDialog"
import { useBudgets, useBudgetStatuses, useDeleteBudget } from "./use-budgets"

export function BudgetsPage() {
  const user = useAuthStore((state) => state.user)
  const { data: budgets, isLoading } = useBudgets()
  const statuses = useBudgetStatuses((budgets ?? []).map((b) => b.id))
  const deleteBudget = useDeleteBudget()

  const [formOpen, setFormOpen] = useState(false)
  const [editingBudget, setEditingBudget] = useState<Budget | undefined>(undefined)
  const [deletingBudget, setDeletingBudget] = useState<Budget | null>(null)

  function openCreateDialog() {
    setEditingBudget(undefined)
    setFormOpen(true)
  }

  function openEditDialog(budget: Budget) {
    setEditingBudget(budget)
    setFormOpen(true)
  }

  function confirmDelete() {
    if (!deletingBudget) return
    deleteBudget.mutate(deletingBudget.id, {
      onSuccess: () => {
        toast.success("Budget deleted")
        setDeletingBudget(null)
      },
      onError: (error) => toast.error(getErrorMessage(error)),
    })
  }

  return (
    <div className="space-y-6">
      <PageHeader
        title="Budgets"
        description="Set spending limits and track how you're doing against them."
        action={
          <Button onClick={openCreateDialog}>
            <Plus className="size-4" />
            New budget
          </Button>
        }
      />

      {isLoading ? (
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {Array.from({ length: 3 }).map((_, i) => (
            <Skeleton key={i} className="h-40 rounded-xl" />
          ))}
        </div>
      ) : budgets && budgets.length > 0 ? (
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {budgets.map((budget, i) => (
            <BudgetCard
              key={budget.id}
              budget={budget}
              status={statuses[i]?.data}
              isStatusLoading={statuses[i]?.isLoading ?? true}
              onEdit={() => openEditDialog(budget)}
              onDelete={() => setDeletingBudget(budget)}
            />
          ))}
        </div>
      ) : (
        <EmptyState
          icon={PiggyBank}
          title="No budgets yet"
          description="Create a budget to keep your spending in a category — or overall — under control."
          action={
            <Button onClick={openCreateDialog}>
              <Plus className="size-4" />
              New budget
            </Button>
          }
        />
      )}

      <BudgetFormDialog
        open={formOpen}
        onOpenChange={setFormOpen}
        budget={editingBudget}
        defaultCurrency={user?.defaultCurrency ?? "TRY"}
      />

      <ConfirmDialog
        open={!!deletingBudget}
        onOpenChange={(open) => !open && setDeletingBudget(null)}
        title="Delete budget?"
        description="This budget will be permanently removed. This action can't be undone."
        confirmLabel="Delete"
        onConfirm={confirmDelete}
        isLoading={deleteBudget.isPending}
      />
    </div>
  )
}
