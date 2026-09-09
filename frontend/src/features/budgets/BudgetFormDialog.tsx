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
import { useCategories } from "@/features/categories/use-categories"
import { BUDGET_PERIODS, CURRENCIES } from "@/types/enums"
import type { Budget } from "@/types/budget"
import { formatEnumLabel } from "@/utils/format"
import { getErrorMessage } from "@/utils/error-message"
import { budgetSchema, type BudgetFormValues } from "./schemas"
import { useCreateBudget, useUpdateBudget } from "./use-budgets"

const GENERAL_CATEGORY_VALUE = "GENERAL"

interface BudgetFormDialogProps {
  open: boolean
  onOpenChange: (open: boolean) => void
  budget?: Budget
  defaultCurrency: (typeof CURRENCIES)[number]
}

function emptyValues(defaultCurrency: (typeof CURRENCIES)[number]): BudgetFormValues {
  return {
    categoryId: null,
    period: "MONTHLY",
    amountLimit: "",
    currency: defaultCurrency,
    startDate: new Date().toISOString().slice(0, 10),
  }
}

export function BudgetFormDialog({ open, onOpenChange, budget, defaultCurrency }: BudgetFormDialogProps) {
  const isEditing = !!budget
  const { categories } = useCategories("EXPENSE")
  const createBudget = useCreateBudget()
  const updateBudget = useUpdateBudget()
  const isPending = createBudget.isPending || updateBudget.isPending

  const form = useForm<BudgetFormValues>({
    resolver: zodResolver(budgetSchema),
    defaultValues: emptyValues(defaultCurrency),
  })

  useEffect(() => {
    if (!open) return
    form.reset(
      budget
        ? {
            categoryId: budget.categoryId,
            period: budget.period,
            amountLimit: String(budget.amountLimit),
            currency: budget.currency,
            startDate: budget.startDate,
          }
        : emptyValues(defaultCurrency),
    )
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [open, budget])

  function onSubmit(values: BudgetFormValues) {
    const body = {
      categoryId: values.categoryId,
      period: values.period,
      amountLimit: Number(values.amountLimit),
      currency: values.currency,
      startDate: values.startDate,
    }

    const onSuccess = () => {
      toast.success(isEditing ? "Budget updated" : "Budget created")
      onOpenChange(false)
    }
    const onError = (error: unknown) => toast.error(getErrorMessage(error))

    if (isEditing) {
      updateBudget.mutate({ id: budget.id, body }, { onSuccess, onError })
    } else {
      createBudget.mutate(body, { onSuccess, onError })
    }
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-md">
        <DialogHeader>
          <DialogTitle>{isEditing ? "Edit budget" : "Create budget"}</DialogTitle>
          <DialogDescription>
            {isEditing
              ? "Update this budget's limit or period."
              : "Set a spending limit for a category, or an overall budget."}
          </DialogDescription>
        </DialogHeader>

        <Form {...form}>
          <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-4">
            <FormField
              control={form.control}
              name="categoryId"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Category</FormLabel>
                  <Select
                    value={field.value ?? GENERAL_CATEGORY_VALUE}
                    onValueChange={(v) => field.onChange(v === GENERAL_CATEGORY_VALUE ? null : v)}
                  >
                    <FormControl>
                      <SelectTrigger className="w-full">
                        <SelectValue>
                          {(v: string) =>
                            v === GENERAL_CATEGORY_VALUE
                              ? "General (all categories)"
                              : (categories.find((c) => c.id === v)?.name ?? v)
                          }
                        </SelectValue>
                      </SelectTrigger>
                    </FormControl>
                    <SelectContent>
                      <SelectItem value={GENERAL_CATEGORY_VALUE}>General (all categories)</SelectItem>
                      {categories.map((c) => (
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

            <FormField
              control={form.control}
              name="period"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Period</FormLabel>
                  <Select value={field.value} onValueChange={field.onChange}>
                    <FormControl>
                      <SelectTrigger className="w-full">
                        <SelectValue>{(v: string) => formatEnumLabel(v)}</SelectValue>
                      </SelectTrigger>
                    </FormControl>
                    <SelectContent>
                      {BUDGET_PERIODS.map((p) => (
                        <SelectItem key={p} value={p}>
                          {formatEnumLabel(p)}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                  <FormMessage />
                </FormItem>
              )}
            />

            <div className="grid grid-cols-2 gap-3">
              <FormField
                control={form.control}
                name="amountLimit"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Limit</FormLabel>
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
              name="startDate"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Start date</FormLabel>
                  <FormControl>
                    <Input type="date" {...field} />
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
                {isPending ? "Saving..." : isEditing ? "Save changes" : "Create budget"}
              </Button>
            </DialogFooter>
          </form>
        </Form>
      </DialogContent>
    </Dialog>
  )
}
