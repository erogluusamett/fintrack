import {
  AlertTriangle,
  Bell,
  CalendarClock,
  Lightbulb,
  Repeat,
  type LucideIcon,
} from "lucide-react"
import type { NotificationType } from "@/types/enums"

export const NOTIFICATION_ICON: Record<NotificationType, LucideIcon> = {
  BUDGET_WARNING: AlertTriangle,
  SUBSCRIPTION_REMINDER: Repeat,
  PAYMENT_REMINDER: CalendarClock,
  FINANCIAL_INSIGHT: Lightbulb,
  SYSTEM: Bell,
}
