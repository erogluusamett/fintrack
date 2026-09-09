import type { BillingCycle, Currency } from "./enums"

export interface Subscription {
  id: string
  categoryId: string | null
  categoryName: string | null
  name: string
  amount: number
  currency: Currency
  billingCycle: BillingCycle
  nextBillingDate: string
  active: boolean
}

export interface SubscriptionRequest {
  categoryId: string | null
  name: string
  amount: number
  currency: Currency
  billingCycle: BillingCycle
  nextBillingDate: string
}
