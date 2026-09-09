import { zodResolver } from "@hookform/resolvers/zod"
import { useEffect } from "react"
import { useForm } from "react-hook-form"
import { toast } from "sonner"
import { Button } from "@/components/ui/button"
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog"
import {
  Form,
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from "@/components/ui/form"
import { Input } from "@/components/ui/input"
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select"
import { Textarea } from "@/components/ui/textarea"
import { useCategories } from "@/features/categories/use-categories"
import { CURRENCIES, TRANSACTION_TYPES } from "@/types/enums"
import type { Transaction } from "@/types/transaction"
import { getErrorMessage } from "@/utils/error-message"
import { formatEnumLabel } from "@/utils/format"
import { transactionSchema, type TransactionFormValues } from "./schemas"
import { useCreateTransaction, useUpdateTransaction } from "./use-transactions"

interface TransactionFormDialogProps {
  open: boolean
  onOpenChange: (open: boolean) => void
  /** Verilirse düzenleme modu, verilmezse yeni kayıt. */
  transaction?: Transaction
  defaultCurrency: (typeof CURRENCIES)[number]
}

const EMPTY_VALUES: TransactionFormValues = {
  type: "EXPENSE",
  categoryId: null,
  amount: "",
  currency: "TRY",
  transactionDate: new Date().toISOString().slice(0, 10),
  description: "",
}

export function TransactionFormDialog({
  open,
  onOpenChange,
  transaction,
  defaultCurrency,
}: TransactionFormDialogProps) {
  const isEditing = !!transaction
  const { categories } = useCategories()
  const createTransaction = useCreateTransaction()
  const updateTransaction = useUpdateTransaction()
  const isPending = createTransaction.isPending || updateTransaction.isPending

  const form = useForm<TransactionFormValues>({
    resolver: zodResolver(transactionSchema),
    defaultValues: EMPTY_VALUES,
  })

  const type = form.watch("type")

  // Dialog her açıldığında (yeni transaction'a ya da farklı bir transaction'ı
  // düzenlemeye geçildiğinde) formu doğru değerlerle sıfırla.
  useEffect(() => {
    if (!open) return
    form.reset(
      transaction
        ? {
            type: transaction.type,
            categoryId: transaction.categoryId,
            amount: String(transaction.amount),
            currency: transaction.currency,
            transactionDate: transaction.transactionDate,
            description: transaction.description ?? "",
          }
        : { ...EMPTY_VALUES, currency: defaultCurrency },
    )
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [open, transaction])

  function onSubmit(values: TransactionFormValues) {
    const body = {
      type: values.type,
      categoryId: values.categoryId,
      amount: Number(values.amount),
      currency: values.currency,
      transactionDate: values.transactionDate,
      description: values.description || null,
    }

    const onSuccess = () => {
      toast.success(isEditing ? "Transaction updated" : "Transaction created")
      onOpenChange(false)
    }
    const onError = (error: unknown) => toast.error(getErrorMessage(error))

    if (isEditing) {
      updateTransaction.mutate({ id: transaction.id, body }, { onSuccess, onError })
    } else {
      createTransaction.mutate(body, { onSuccess, onError })
    }
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-md">
        <DialogHeader>
          <DialogTitle>{isEditing ? "Edit transaction" : "Add transaction"}</DialogTitle>
          <DialogDescription>
            {isEditing ? "Update the details of this transaction." : "Record a new income, expense, or transfer."}
          </DialogDescription>
        </DialogHeader>

        <Form {...form}>
          <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-4">
            <FormField
              control={form.control}
              name="type"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Type</FormLabel>
                  <Select
                    value={field.value}
                    onValueChange={(value) => {
                      field.onChange(value)
                      if (value === "TRANSFER") form.setValue("categoryId", null)
                    }}
                  >
                    <FormControl>
                      <SelectTrigger className="w-full">
                        <SelectValue>{(value: string) => formatEnumLabel(value)}</SelectValue>
                      </SelectTrigger>
                    </FormControl>
                    <SelectContent>
                      {TRANSACTION_TYPES.map((t) => (
                        <SelectItem key={t} value={t}>
                          {formatEnumLabel(t)}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                  <FormMessage />
                </FormItem>
              )}
            />

            {type !== "TRANSFER" && (
              <FormField
                control={form.control}
                name="categoryId"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Category</FormLabel>
                    <Select value={field.value ?? undefined} onValueChange={field.onChange}>
                      <FormControl>
                        <SelectTrigger className="w-full">
                          <SelectValue placeholder="Select a category">
                            {(value: string | null) =>
                              value ? categories.find((c) => c.id === value)?.name : "Select a category"
                            }
                          </SelectValue>
                        </SelectTrigger>
                      </FormControl>
                      <SelectContent>
                        {categories
                          .filter((c) => c.type === type)
                          .map((c) => (
                            <SelectItem key={c.id} value={c.id}>
                              {c.name}
                            </SelectItem>
                          ))}
                      </SelectContent>
                    </Select>
                    <FormMessage />
                  </FormItem>
                )}
              />
            )}

            <div className="grid grid-cols-2 gap-3">
              <FormField
                control={form.control}
                name="amount"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Amount</FormLabel>
                    <FormControl>
                      <Input type="number" step="0.01" min="0" {...field} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
              <FormField
                control={form.control}
                name="currency"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Currency</FormLabel>
                    <Select value={field.value} onValueChange={field.onChange}>
                      <FormControl>
                        <SelectTrigger className="w-full">
                          <SelectValue />
                        </SelectTrigger>
                      </FormControl>
                      <SelectContent>
                        {CURRENCIES.map((c) => (
                          <SelectItem key={c} value={c}>
                            {c}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                    <FormMessage />
                  </FormItem>
                )}
              />
            </div>

            <FormField
              control={form.control}
              name="transactionDate"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Date</FormLabel>
                  <FormControl>
                    <Input type="date" {...field} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />

            <FormField
              control={form.control}
              name="description"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Description (optional)</FormLabel>
                  <FormControl>
                    <Textarea rows={2} placeholder="e.g. Grocery shopping" {...field} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />

            <DialogFooter>
              <Button type="button" variant="outline" onClick={() => onOpenChange(false)}>
                Cancel
              </Button>
              <Button type="submit" disabled={isPending}>
                {isPending ? "Saving..." : isEditing ? "Save changes" : "Add transaction"}
              </Button>
            </DialogFooter>
          </form>
        </Form>
      </DialogContent>
    </Dialog>
  )
}
