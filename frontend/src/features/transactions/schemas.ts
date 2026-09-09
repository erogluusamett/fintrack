import { z } from "zod"
import { CURRENCIES, TRANSACTION_TYPES } from "@/types/enums"

/**
 * `amount` formda ham string olarak tutulur (native number input zaten
 * string döner) — z.coerce.number() kullanmak react-hook-form'un
 * input/output tiplerini ayırmasını gerektirir ki bu, zodResolver ile
 * gereksiz bir jenerik karmaşasına yol açıyor. Sayıya çevirme onSubmit'te
 * tek bir yerde yapılıyor.
 */
export const transactionSchema = z
  .object({
    type: z.enum(TRANSACTION_TYPES),
    categoryId: z.string().nullable(),
    amount: z
      .string()
      .min(1, "Amount is required")
      .refine((v) => Number(v) > 0, "Amount must be greater than 0"),
    currency: z.enum(CURRENCIES),
    transactionDate: z.string().min(1, "Date is required"),
    description: z.string().max(500).optional(),
  })
  .refine((data) => data.type === "TRANSFER" || !!data.categoryId, {
    message: "Category is required for income and expense",
    path: ["categoryId"],
  })

export type TransactionFormValues = z.infer<typeof transactionSchema>
