import { z } from "zod"
import { BILLING_CYCLES, CURRENCIES } from "@/types/enums"

export const subscriptionSchema = z.object({
  categoryId: z.string().nullable(),
  name: z.string().min(1, "Name is required").max(100, "Name is too long"),
  amount: z
    .string()
    .min(1, "Amount is required")
    .refine((v) => Number(v) > 0, "Amount must be greater than 0"),
  currency: z.enum(CURRENCIES),
  billingCycle: z.enum(BILLING_CYCLES),
  nextBillingDate: z.string().min(1, "Next billing date is required"),
})

export type SubscriptionFormValues = z.infer<typeof subscriptionSchema>
