import type { BillingCycle } from "@/types/enums"

const WEEKS_PER_MONTH = 52 / 12

/** Farklı billing cycle'lardaki tutarları karşılaştırılabilir kılmak için aylığa normalize eder. */
export function toMonthlyAmount(amount: number, cycle: BillingCycle): number {
  switch (cycle) {
    case "WEEKLY":
      return amount * WEEKS_PER_MONTH
    case "MONTHLY":
      return amount
    case "YEARLY":
      return amount / 12
  }
}

export function toYearlyAmount(amount: number, cycle: BillingCycle): number {
  return toMonthlyAmount(amount, cycle) * 12
}
