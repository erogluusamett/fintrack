import {
  LayoutDashboard,
  ArrowLeftRight,
  PiggyBank,
  Repeat,
  RefreshCw,
  LineChart,
  FileBarChart,
  Bell,
  Settings,
  type LucideIcon,
} from "lucide-react"

export interface NavItem {
  label: string
  to: string
  icon: LucideIcon
}

export const NAV_ITEMS: NavItem[] = [
  { label: "Dashboard", to: "/dashboard", icon: LayoutDashboard },
  { label: "Transactions", to: "/transactions", icon: ArrowLeftRight },
  { label: "Budgets", to: "/budgets", icon: PiggyBank },
  { label: "Subscriptions", to: "/subscriptions", icon: Repeat },
  { label: "Recurring", to: "/recurring", icon: RefreshCw },
  { label: "Analytics", to: "/analytics", icon: LineChart },
  { label: "Reports", to: "/reports", icon: FileBarChart },
  { label: "Notifications", to: "/notifications", icon: Bell },
  { label: "Settings", to: "/settings", icon: Settings },
]
