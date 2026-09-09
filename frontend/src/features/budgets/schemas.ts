import { z } from "zod"
import { BUDGET_PERIODS, CURRENCIES } from "@/types/enums"

export const budgetSchema = z.object({
  categoryId: z.string().nullable(),
  period: z.enum(BUDGET_PERIODS),
  amountLimit: z
    .string()
    .min(1, "Amount is required")
    .refine((v) => Number(v) > 0, "Amount must be greater than 0"),
  currency: z.enum(CURRENCIES),
  startDate: z.string().min(1, "Start date is required"),
})

export type BudgetFormValues = z.infer<typeof budgetSchema>
