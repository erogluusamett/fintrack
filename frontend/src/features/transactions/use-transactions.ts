import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query"
import { transactionApi } from "@/api/transaction.api"
import { queryKeys } from "@/api/query-keys"
import type { TransactionFilters, TransactionRequest } from "@/types/transaction"

export function useTransactions(filters: TransactionFilters) {
  return useQuery({
    queryKey: queryKeys.transactions(filters),
    queryFn: () => transactionApi.list(filters),
    placeholderData: (previousData) => previousData, // sayfa değişirken tablo flash-boş olmasın
  })
}

/** Bir transaction create/update/delete edildiğinde etkilenen her şeyi tazeler: listeler + dashboard + analytics. */
function useInvalidateTransactionRelatedQueries() {
  const queryClient = useQueryClient()
  return () => {
    queryClient.invalidateQueries({ queryKey: ["transactions"] })
    queryClient.invalidateQueries({ queryKey: ["analytics"] })
    queryClient.invalidateQueries({ queryKey: queryKeys.budgets })
  }
}

export function useCreateTransaction() {
  const invalidate = useInvalidateTransactionRelatedQueries()
  return useMutation({
    mutationFn: (body: TransactionRequest) => transactionApi.create(body),
    onSuccess: invalidate,
  })
}

export function useUpdateTransaction() {
  const invalidate = useInvalidateTransactionRelatedQueries()
  return useMutation({
    mutationFn: ({ id, body }: { id: string; body: TransactionRequest }) => transactionApi.update(id, body),
    onSuccess: invalidate,
  })
}

export function useDeleteTransaction() {
  const invalidate = useInvalidateTransactionRelatedQueries()
  return useMutation({
    mutationFn: (id: string) => transactionApi.remove(id),
    onSuccess: invalidate,
  })
}
