import { useState } from "react"
import { Plus, Receipt } from "lucide-react"
import { EmptyState } from "@/components/common/EmptyState"
import { PageHeader } from "@/components/common/PageHeader"
import { Pagination } from "@/components/common/Pagination"
import { ConfirmDialog } from "@/components/common/ConfirmDialog"
import { Button } from "@/components/ui/button"
import { Skeleton } from "@/components/ui/skeleton"
import { useAuthStore } from "@/store/auth-store"
import { useDebounce } from "@/hooks/use-debounce"
import type { Transaction, TransactionFilters } from "@/types/transaction"
import { toast } from "sonner"
import { getErrorMessage } from "@/utils/error-message"
import { EMPTY_FILTER_STATE, TransactionFilterBar, type TransactionFilterState } from "./TransactionFilterBar"
import { TransactionFormDialog } from "./TransactionFormDialog"
import { TransactionsTable } from "./TransactionsTable"
import { useDeleteTransaction, useTransactions } from "./use-transactions"

const PAGE_SIZE = 10

function toApiFilters(state: TransactionFilterState, page: number): TransactionFilters {
  return {
    search: state.search || undefined,
    type: state.type === "ALL" ? undefined : state.type,
    categoryId: state.categoryId === "ALL" ? undefined : state.categoryId,
    from: state.from || undefined,
    to: state.to || undefined,
    minAmount: state.minAmount ? Number(state.minAmount) : undefined,
    maxAmount: state.maxAmount ? Number(state.maxAmount) : undefined,
    page,
    size: PAGE_SIZE,
    sort: "transactionDate,desc",
  }
}

export function TransactionsPage() {
  const user = useAuthStore((state) => state.user)
  const [filterState, setFilterState] = useState<TransactionFilterState>(EMPTY_FILTER_STATE)
  const [page, setPage] = useState(0)
  const debouncedFilterState = useDebounce(filterState)

  const [formOpen, setFormOpen] = useState(false)
  const [editingTransaction, setEditingTransaction] = useState<Transaction | undefined>(undefined)
  const [deletingTransaction, setDeletingTransaction] = useState<Transaction | null>(null)

  const filters = toApiFilters(debouncedFilterState, page)
  const { data, isLoading, isPlaceholderData } = useTransactions(filters)
  const deleteTransaction = useDeleteTransaction()

  function handleFilterChange(next: TransactionFilterState) {
    setFilterState(next)
    setPage(0)
  }

  function openCreateDialog() {
    setEditingTransaction(undefined)
    setFormOpen(true)
  }

  function openEditDialog(transaction: Transaction) {
    setEditingTransaction(transaction)
    setFormOpen(true)
  }

  function confirmDelete() {
    if (!deletingTransaction) return
    deleteTransaction.mutate(deletingTransaction.id, {
      onSuccess: () => {
        toast.success("Transaction deleted")
        setDeletingTransaction(null)
      },
      onError: (error) => toast.error(getErrorMessage(error)),
    })
  }

  const hasFilters = JSON.stringify(filterState) !== JSON.stringify(EMPTY_FILTER_STATE)

  return (
    <div className="space-y-6">
      <PageHeader
        title="Transactions"
        description="Track every income, expense, and transfer."
        action={
          <Button onClick={openCreateDialog}>
            <Plus className="size-4" />
            Add transaction
          </Button>
        }
      />

      <TransactionFilterBar value={filterState} onChange={handleFilterChange} />

      {isLoading ? (
        <div className="space-y-2">
          {Array.from({ length: 5 }).map((_, i) => (
            <Skeleton key={i} className="h-12 rounded-lg" />
          ))}
        </div>
      ) : data && data.content.length > 0 ? (
        <div className={isPlaceholderData ? "opacity-60 transition-opacity" : undefined}>
          <TransactionsTable transactions={data.content} onEdit={openEditDialog} onDelete={setDeletingTransaction} />
          <div className="mt-3">
            <Pagination
              page={data.page}
              totalPages={data.totalPages}
              totalElements={data.totalElements}
              onPageChange={setPage}
            />
          </div>
        </div>
      ) : (
        <EmptyState
          icon={Receipt}
          title={hasFilters ? "No matching transactions" : "No transactions yet"}
          description={
            hasFilters
              ? "Try adjusting your filters to see more results."
              : "Start tracking your finances by adding your first transaction."
          }
          action={
            !hasFilters && (
              <Button onClick={openCreateDialog}>
                <Plus className="size-4" />
                Add transaction
              </Button>
            )
          }
        />
      )}

      <TransactionFormDialog
        open={formOpen}
        onOpenChange={setFormOpen}
        transaction={editingTransaction}
        defaultCurrency={user?.defaultCurrency ?? "TRY"}
      />

      <ConfirmDialog
        open={!!deletingTransaction}
        onOpenChange={(open) => !open && setDeletingTransaction(null)}
        title="Delete transaction?"
        description="This transaction will be removed from your active records. This action can't be undone from the app."
        confirmLabel="Delete"
        onConfirm={confirmDelete}
        isLoading={deleteTransaction.isPending}
      />
    </div>
  )
}
