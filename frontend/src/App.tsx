import { Navigate, Route, Routes } from "react-router-dom"
import { AppLayout } from "@/components/layout/AppLayout"
import { AuthLayout } from "@/components/layout/AuthLayout"
import { AnalyticsPage } from "@/features/analytics/AnalyticsPage"
import { ForgotPasswordPage } from "@/features/auth/ForgotPasswordPage"
import { LoginPage } from "@/features/auth/LoginPage"
import { RegisterPage } from "@/features/auth/RegisterPage"
import { ResetPasswordPage } from "@/features/auth/ResetPasswordPage"
import { useBootstrapAuth } from "@/features/auth/use-auth"
import { BudgetsPage } from "@/features/budgets/BudgetsPage"
import { DashboardPage } from "@/features/dashboard/DashboardPage"
import { RecurringPaymentsPage } from "@/features/recurring/RecurringPaymentsPage"
import { SubscriptionsPage } from "@/features/subscriptions/SubscriptionsPage"
import { TransactionsPage } from "@/features/transactions/TransactionsPage"
import { ProtectedRoute } from "@/routes/ProtectedRoute"

export function App() {
  useBootstrapAuth()

  return (
    <Routes>
      <Route element={<AuthLayout />}>
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route path="/forgot-password" element={<ForgotPasswordPage />} />
        <Route path="/reset-password" element={<ResetPasswordPage />} />
      </Route>

      <Route element={<ProtectedRoute />}>
        <Route element={<AppLayout />}>
          <Route path="/dashboard" element={<DashboardPage />} />
          <Route path="/transactions" element={<TransactionsPage />} />
          <Route path="/budgets" element={<BudgetsPage />} />
          <Route path="/subscriptions" element={<SubscriptionsPage />} />
          <Route path="/recurring" element={<RecurringPaymentsPage />} />
          <Route path="/analytics" element={<AnalyticsPage />} />
        </Route>
      </Route>

      <Route path="/" element={<Navigate to="/dashboard" replace />} />
      <Route path="*" element={<Navigate to="/dashboard" replace />} />
    </Routes>
  )
}
