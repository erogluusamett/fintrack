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
import { BILLING_CYCLES, CURRENCIES } from "@/types/enums"
import type { Subscription } from "@/types/subscription"
import { formatEnumLabel } from "@/utils/format"
import { getErrorMessage } from "@/utils/error-message"
import { subscriptionSchema, type SubscriptionFormValues } from "./schemas"
import { useCreateSubscription, useUpdateSubscription } from "./use-subscriptions"

const NO_CATEGORY_VALUE = "NONE"

interface SubscriptionFormDialogProps {
  open: boolean
  onOpenChange: (open: boolean) => void
  subscription?: Subscription
  defaultCurrency: (typeof CURRENCIES)[number]
}

function emptyValues(defaultCurrency: (typeof CURRENCIES)[number]): SubscriptionFormValues {
  return {
    categoryId: null,
    name: "",
    amount: "",
    currency: defaultCurrency,
    billingCycle: "MONTHLY",
    nextBillingDate: new Date().toISOString().slice(0, 10),
  }
}

export function SubscriptionFormDialog({
  open,
  onOpenChange,
  subscription,
  defaultCurrency,
}: SubscriptionFormDialogProps) {
  const isEditing = !!subscription
  const { categories } = useCategories("EXPENSE")
  const createSubscription = useCreateSubscription()
  const updateSubscription = useUpdateSubscription()
  const isPending = createSubscription.isPending || updateSubscription.isPending

  const form = useForm<SubscriptionFormValues>({
    resolver: zodResolver(subscriptionSchema),
    defaultValues: emptyValues(defaultCurrency),
  })

  useEffect(() => {
    if (!open) return
    form.reset(
      subscription
        ? {
            categoryId: subscription.categoryId,
            name: subscription.name,
            amount: String(subscription.amount),
            currency: subscription.currency,
            billingCycle: subscription.billingCycle,
            nextBillingDate: subscription.nextBillingDate,
          }
        : emptyValues(defaultCurrency),
    )
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [open, subscription])

  function onSubmit(values: SubscriptionFormValues) {
    const body = {
      categoryId: values.categoryId,
      name: values.name,
      amount: Number(values.amount),
      currency: values.currency,
      billingCycle: values.billingCycle,
      nextBillingDate: values.nextBillingDate,
    }

    const onSuccess = () => {
      toast.success(isEditing ? "Subscription updated" : "Subscription created")
      onOpenChange(false)
    }
    const onError = (error: unknown) => toast.error(getErrorMessage(error))

    if (isEditing) {
      updateSubscription.mutate({ id: subscription.id, body }, { onSuccess, onError })
    } else {
      createSubscription.mutate(body, { onSuccess, onError })
    }
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-md">
        <DialogHeader>
          <DialogTitle>{isEditing ? "Edit subscription" : "Add subscription"}</DialogTitle>
          <DialogDescription>
            {isEditing
              ? "Update this subscription's details."
              : "Track a recurring subscription like a streaming service or gym membership."}
          </DialogDescription>
        </DialogHeader>

        <Form {...form}>
          <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-4">
            <FormField
              control={form.control}
              name="name"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Name</FormLabel>
                  <FormControl>
                    <Input placeholder="Netflix" {...field} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />

            <FormField
              control={form.control}
              name="categoryId"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Category</FormLabel>
                  <Select
                    value={field.value ?? NO_CATEGORY_VALUE}
                    onValueChange={(v) => field.onChange(v === NO_CATEGORY_VALUE ? null : v)}
                  >
                    <FormControl>
                      <SelectTrigger className="w-full">
                        <SelectValue>
                          {(v: string) =>
                            v === NO_CATEGORY_VALUE
                              ? "No category"
                              : (categories.find((c) => c.id === v)?.name ?? v)
                          }
                        </SelectValue>
                      </SelectTrigger>
                    </FormControl>
                    <SelectContent>
                      <SelectItem value={NO_CATEGORY_VALUE}>No category</SelectItem>
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
              name="billingCycle"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Billing cycle</FormLabel>
                  <Select value={field.value} onValueChange={field.onChange}>
                    <FormControl>
                      <SelectTrigger className="w-full">
                        <SelectValue>{(v: string) => formatEnumLabel(v)}</SelectValue>
                      </SelectTrigger>
                    </FormControl>
                    <SelectContent>
                      {BILLING_CYCLES.map((c) => (
                        <SelectItem key={c} value={c}>
                          {formatEnumLabel(c)}
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
              name="nextBillingDate"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Next billing date</FormLabel>
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
                {isPending ? "Saving..." : isEditing ? "Save changes" : "Add subscription"}
              </Button>
            </DialogFooter>
          </form>
        </Form>
      </DialogContent>
    </Dialog>
  )
}
