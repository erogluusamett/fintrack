import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query"
import { notificationApi } from "@/api/notification.api"
import { queryKeys } from "@/api/query-keys"

export function useNotifications(page = 0, size = 20) {
  return useQuery({
    queryKey: queryKeys.notifications(page),
    queryFn: () => notificationApi.list({ page, size }),
  })
}

/** Bell dropdown'ı için: en son bildirimlerin ilk sayfası, kısa aralıklarla yenilenir. */
export function useRecentNotifications() {
  return useQuery({
    queryKey: queryKeys.notifications(0),
    queryFn: () => notificationApi.list({ page: 0, size: 8 }),
    refetchInterval: 60_000,
  })
}

function useInvalidateNotifications() {
  const queryClient = useQueryClient()
  return () => queryClient.invalidateQueries({ queryKey: ["notifications"] })
}

export function useMarkNotificationRead() {
  const invalidate = useInvalidateNotifications()
  return useMutation({
    mutationFn: (id: string) => notificationApi.markRead(id),
    onSuccess: invalidate,
  })
}

export function useMarkAllNotificationsRead() {
  const invalidate = useInvalidateNotifications()
  return useMutation({
    mutationFn: () => notificationApi.markAllRead(),
    onSuccess: invalidate,
  })
}
