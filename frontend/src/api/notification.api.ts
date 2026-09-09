import { api, unwrap } from "./axios"
import type { ApiResponse, PageResponse } from "@/types/api"
import type { Notification } from "@/types/notification"

export const notificationApi = {
  list: (params: { page?: number; size?: number } = {}) =>
    unwrap(api.get<ApiResponse<PageResponse<Notification>>>("/notifications", { params })),

  markRead: (id: string) => api.put(`/notifications/${id}/read`),

  markAllRead: () => api.put("/notifications/read-all"),
}
