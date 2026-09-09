import { Bell } from "lucide-react"
import { Button } from "@/components/ui/button"

// TODO(notifications phase): gerçek /notifications verisiyle badge sayısı + dropdown panel.
export function NotificationBell() {
  return (
    <Button variant="ghost" size="icon" aria-label="Notifications">
      <Bell className="size-4.5" />
    </Button>
  )
}
