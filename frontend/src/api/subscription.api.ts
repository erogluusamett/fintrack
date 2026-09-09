import { api, unwrap } from "./axios"
import type { ApiResponse } from "@/types/api"
import type { Subscription, SubscriptionRequest } from "@/types/subscription"

export const subscriptionApi = {
  list: () => unwrap(api.get<ApiResponse<Subscription[]>>("/subscriptions")),

  create: (body: SubscriptionRequest) => unwrap(api.post<ApiResponse<Subscription>>("/subscriptions", body)),

  update: (id: string, body: SubscriptionRequest) =>
    unwrap(api.put<ApiResponse<Subscription>>(`/subscriptions/${id}`, body)),

  cancel: (id: string) => unwrap(api.post<ApiResponse<Subscription>>(`/subscriptions/${id}/cancel`)),

  remove: (id: string) => api.delete(`/subscriptions/${id}`),
}
