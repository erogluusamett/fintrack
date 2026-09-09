import { useState } from "react"
import { Bell, CheckCheck } from "lucide-react"
import { EmptyState } from "@/components/common/EmptyState"
import { PageHeader } from "@/components/common/PageHeader"
import { Pagination } from "@/components/common/Pagination"
import { Badge } from "@/components/ui/badge"
import { Button } from "@/components/ui/button"
import { Skeleton } from "@/components/ui/skeleton"
import { cn } from "@/lib/utils"
import { formatDateTime } from "@/utils/currency"
import { NOTIFICATION_ICON } from "./notification-icon"
import { useMarkAllNotificationsRead, useMarkNotificationRead, useNotifications } from "./use-notifications"

const PAGE_SIZE = 20

export function NotificationsPage() {
  const [page, setPage] = useState(0)
  const { data, isLoading } = useNotifications(page, PAGE_SIZE)
  const markRead = useMarkNotificationRead()
  const markAllRead = useMarkAllNotificationsRead()

  const notifications = data?.content ?? []
  const hasUnread = notifications.some((n) => !n.read)

  return (
    <div className="space-y-6">
      <PageHeader
        title="Notifications"
        description="Budget alerts, subscription reminders, and financial insights."
        action={
          hasUnread && (
            <Button variant="outline" onClick={() => markAllRead.mutate()} disabled={markAllRead.isPending}>
              <CheckCheck className="size-4" />
              Mark all as read
            </Button>
          )
        }
      />

      {isLoading ? (
        <div className="space-y-2">
          {Array.from({ length: 6 }).map((_, i) => (
            <Skeleton key={i} className="h-16 rounded-lg" />
          ))}
        </div>
      ) : notifications.length > 0 ? (
        <div className="overflow-hidden rounded-xl border border-border">
          <ul className="divide-y divide-border">
            {notifications.map((notification) => {
              const Icon = NOTIFICATION_ICON[notification.type]
              return (
                <li
                  key={notification.id}
                  className={cn(
                    "flex cursor-default items-start gap-3 px-4 py-3.5 transition-colors hover:bg-muted/50",
                    !notification.read && "bg-primary/5",
                  )}
                  onClick={() => !notification.read && markRead.mutate(notification.id)}
                >
                  <div className="relative flex size-9 shrink-0 items-center justify-center rounded-full bg-primary/10 text-primary">
                    <Icon className="size-4.5" />
                  </div>
                  <div className="min-w-0 flex-1">
                    <div className="flex items-center gap-2">
                      <p className="text-sm font-medium">{notification.title}</p>
                      {!notification.read && (
                        <Badge variant="default" className="h-4 px-1.5 text-[10px]">
                          New
                        </Badge>
                      )}
                    </div>
                    <p className="mt-0.5 text-sm text-muted-foreground">{notification.message}</p>
                    <p className="mt-1.5 text-xs text-muted-foreground">{formatDateTime(notification.createdAt)}</p>
                  </div>
                </li>
              )
            })}
          </ul>
        </div>
      ) : (
        <EmptyState
          icon={Bell}
          title="No notifications"
          description="Budget alerts, subscription reminders, and insights will show up here."
        />
      )}

      {data && data.totalElements > 0 && (
        <Pagination
          page={data.page}
          totalPages={data.totalPages}
          totalElements={data.totalElements}
          onPageChange={setPage}
        />
      )}
    </div>
  )
}
