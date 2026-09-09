import { Link } from "react-router-dom"
import { Bell, CheckCheck } from "lucide-react"
import { Button } from "@/components/ui/button"
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from "@/components/ui/popover"
import { formatDateTime } from "@/utils/currency"
import { NOTIFICATION_ICON } from "./notification-icon"
import { useMarkAllNotificationsRead, useMarkNotificationRead, useRecentNotifications } from "./use-notifications"

export function NotificationBell() {
  const { data, isLoading } = useRecentNotifications()
  const markRead = useMarkNotificationRead()
  const markAllRead = useMarkAllNotificationsRead()

  const notifications = data?.content ?? []
  const hasUnread = notifications.some((n) => !n.read)

  return (
    <Popover>
      <PopoverTrigger
        render={<Button variant="ghost" size="icon" className="relative" aria-label="Notifications" />}
      >
        <Bell className="size-4.5" />
        {hasUnread && (
          <span className="absolute top-1.5 right-1.5 size-2 rounded-full bg-destructive ring-2 ring-background" />
        )}
      </PopoverTrigger>
      <PopoverContent align="end" className="w-80 p-0">
        <div className="flex items-center justify-between gap-2 border-b border-border px-3 py-2.5">
          <p className="text-sm font-medium">Notifications</p>
          {hasUnread && (
            <Button
              variant="ghost"
              size="sm"
              className="h-7 gap-1 px-2 text-xs text-muted-foreground"
              onClick={() => markAllRead.mutate()}
              disabled={markAllRead.isPending}
            >
              <CheckCheck className="size-3.5" />
              Mark all read
            </Button>
          )}
        </div>

        <div className="max-h-80 overflow-y-auto">
          {isLoading ? (
            <p className="px-3 py-6 text-center text-sm text-muted-foreground">Loading...</p>
          ) : notifications.length === 0 ? (
            <p className="px-3 py-6 text-center text-sm text-muted-foreground">You're all caught up.</p>
          ) : (
            <ul className="divide-y divide-border">
              {notifications.map((notification) => {
                const Icon = NOTIFICATION_ICON[notification.type]
                return (
                  <li
                    key={notification.id}
                    className="flex cursor-default items-start gap-2.5 px-3 py-2.5 hover:bg-muted/50"
                    onClick={() => !notification.read && markRead.mutate(notification.id)}
                  >
                    <div className="relative flex size-8 shrink-0 items-center justify-center rounded-full bg-primary/10 text-primary">
                      <Icon className="size-4" />
                      {!notification.read && (
                        <span className="absolute -top-0.5 -right-0.5 size-2 rounded-full bg-destructive ring-2 ring-popover" />
                      )}
                    </div>
                    <div className="min-w-0">
                      <p className="text-sm font-medium">{notification.title}</p>
                      <p className="mt-0.5 line-clamp-2 text-xs text-muted-foreground">{notification.message}</p>
                      <p className="mt-1 text-[11px] text-muted-foreground">
                        {formatDateTime(notification.createdAt)}
                      </p>
                    </div>
                  </li>
                )
              })}
            </ul>
          )}
        </div>

        <div className="border-t border-border p-1.5">
          <Button
            variant="ghost"
            size="sm"
            className="w-full"
            nativeButton={false}
            render={<Link to="/notifications" />}
          >
            View all notifications
          </Button>
        </div>
      </PopoverContent>
    </Popover>
  )
}
