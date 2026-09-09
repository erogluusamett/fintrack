import { z } from "zod"
import { CURRENCIES, RECURRING_FREQUENCIES, TRANSACTION_TYPES } from "@/types/enums"

export const recurringSchema = z.object({
  type: z.enum(TRANSACTION_TYPES),
  categoryId: z.string().nullable(),
  amount: z
    .string()
    .min(1, "Amount is required")
    .refine((v) => Number(v) > 0, "Amount must be greater than 0"),
  currency: z.enum(CURRENCIES),
  frequency: z.enum(RECURRING_FREQUENCIES),
  nextExecutionDate: z.string().min(1, "Next execution date is required"),
  description: z.string().max(255, "Description is too long").optional(),
})

export type RecurringFormValues = z.infer<typeof recurringSchema>
