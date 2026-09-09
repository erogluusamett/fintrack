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
import { CURRENCIES, RECURRING_FREQUENCIES, TRANSACTION_TYPES } from "@/types/enums"
import type { RecurringTransaction } from "@/types/recurring"
import { formatEnumLabel } from "@/utils/format"
import { getErrorMessage } from "@/utils/error-message"
import { recurringSchema, type RecurringFormValues } from "./schemas"
import { useCreateRecurringTransaction, useUpdateRecurringTransaction } from "./use-recurring"

interface RecurringFormDialogProps {
  open: boolean
  onOpenChange: (open: boolean) => void
  recurringTransaction?: RecurringTransaction
  defaultCurrency: (typeof CURRENCIES)[number]
}

function emptyValues(defaultCurrency: (typeof CURRENCIES)[number]): RecurringFormValues {
  return {
    type: "EXPENSE",
    categoryId: null,
    amount: "",
    currency: defaultCurrency,
    frequency: "MONTHLY",
    nextExecutionDate: new Date().toISOString().slice(0, 10),
    description: "",
  }
}

export function RecurringFormDialog({
  open,
  onOpenChange,
  recurringTransaction,
  defaultCurrency,
}: RecurringFormDialogProps) {
  const isEditing = !!recurringTransaction
  const { categories } = useCategories()
  const createRecurring = useCreateRecurringTransaction()
  const updateRecurring = useUpdateRecurringTransaction()
  const isPending = createRecurring.isPending || updateRecurring.isPending

  const form = useForm<RecurringFormValues>({
    resolver: zodResolver(recurringSchema),
    defaultValues: emptyValues(defaultCurrency),
  })

  const type = form.watch("type")

  useEffect(() => {
    if (!open) return
    form.reset(
      recurringTransaction
        ? {
            type: recurringTransaction.type,
            categoryId: recurringTransaction.categoryId,
            amount: String(recurringTransaction.amount),
            currency: recurringTransaction.currency,
            frequency: recurringTransaction.frequency,
            nextExecutionDate: recurringTransaction.nextExecutionDate,
            description: recurringTransaction.description ?? "",
          }
        : emptyValues(defaultCurrency),
    )
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [open, recurringTransaction])

  function onSubmit(values: RecurringFormValues) {
    const body = {
      type: values.type,
      categoryId: values.categoryId,
      amount: Number(values.amount),
      currency: values.currency,
      frequency: values.frequency,
      nextExecutionDate: values.nextExecutionDate,
      description: values.description || null,
    }

    const onSuccess = () => {
      toast.success(isEditing ? "Recurring payment updated" : "Recurring payment created")
      onOpenChange(false)
    }
    const onError = (error: unknown) => toast.error(getErrorMessage(error))

    if (isEditing) {
      updateRecurring.mutate({ id: recurringTransaction.id, body }, { onSuccess, onError })
    } else {
      createRecurring.mutate(body, { onSuccess, onError })
    }
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-md">
        <DialogHeader>
          <DialogTitle>{isEditing ? "Edit recurring payment" : "Add recurring payment"}</DialogTitle>
          <DialogDescription>
            {isEditing
              ? "Update this recurring transaction's details."
              : "Automate a transaction that repeats on a schedule, like rent or a salary."}
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
              name="frequency"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Frequency</FormLabel>
                  <Select value={field.value} onValueChange={field.onChange}>
                    <FormControl>
                      <SelectTrigger className="w-full">
                        <SelectValue>{(v: string) => formatEnumLabel(v)}</SelectValue>
                      </SelectTrigger>
                    </FormControl>
                    <SelectContent>
                      {RECURRING_FREQUENCIES.map((f) => (
                        <SelectItem key={f} value={f}>
                          {formatEnumLabel(f)}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                  <FormMessage />
                </FormItem>
              )}
            />

            <FormField
              control={form.control}
              name="nextExecutionDate"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Next execution date</FormLabel>
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
                    <Textarea rows={2} placeholder="e.g. Monthly rent" {...field} />
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
                {isPending ? "Saving..." : isEditing ? "Save changes" : "Add recurring payment"}
              </Button>
            </DialogFooter>
          </form>
        </Form>
      </DialogContent>
    </Dialog>
  )
}
