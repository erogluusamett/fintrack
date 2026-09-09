import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query"
import { subscriptionApi } from "@/api/subscription.api"
import { queryKeys } from "@/api/query-keys"
import type { SubscriptionRequest } from "@/types/subscription"

export function useSubscriptions() {
  return useQuery({
    queryKey: queryKeys.subscriptions,
    queryFn: () => subscriptionApi.list(),
  })
}

function useInvalidateSubscriptionQueries() {
  const queryClient = useQueryClient()
  return () => {
    queryClient.invalidateQueries({ queryKey: queryKeys.subscriptions })
    queryClient.invalidateQueries({ queryKey: ["analytics"] })
  }
}

export function useCreateSubscription() {
  const invalidate = useInvalidateSubscriptionQueries()
  return useMutation({
    mutationFn: (body: SubscriptionRequest) => subscriptionApi.create(body),
    onSuccess: invalidate,
  })
}

export function useUpdateSubscription() {
  const invalidate = useInvalidateSubscriptionQueries()
  return useMutation({
    mutationFn: ({ id, body }: { id: string; body: SubscriptionRequest }) => subscriptionApi.update(id, body),
    onSuccess: invalidate,
  })
}

export function useCancelSubscription() {
  const invalidate = useInvalidateSubscriptionQueries()
  return useMutation({
    mutationFn: (id: string) => subscriptionApi.cancel(id),
    onSuccess: invalidate,
  })
}

export function useDeleteSubscription() {
  const invalidate = useInvalidateSubscriptionQueries()
  return useMutation({
    mutationFn: (id: string) => subscriptionApi.remove(id),
    onSuccess: invalidate,
  })
}
