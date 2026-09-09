import { Search, X } from "lucide-react"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select"
import { useCategories } from "@/features/categories/use-categories"
import { TRANSACTION_TYPES, type TransactionType } from "@/types/enums"
import { formatEnumLabel } from "@/utils/format"

export interface TransactionFilterState {
  search: string
  type: TransactionType | "ALL"
  categoryId: string | "ALL"
  from: string
  to: string
  minAmount: string
  maxAmount: string
}

export const EMPTY_FILTER_STATE: TransactionFilterState = {
  search: "",
  type: "ALL",
  categoryId: "ALL",
  from: "",
  to: "",
  minAmount: "",
  maxAmount: "",
}

interface TransactionFilterBarProps {
  value: TransactionFilterState
  onChange: (value: TransactionFilterState) => void
}

export function TransactionFilterBar({ value, onChange }: TransactionFilterBarProps) {
  const { categories } = useCategories()

  function set<K extends keyof TransactionFilterState>(key: K, val: TransactionFilterState[K]) {
    onChange({ ...value, [key]: val })
  }

  const hasActiveFilters = JSON.stringify(value) !== JSON.stringify(EMPTY_FILTER_STATE)

  return (
    <div className="space-y-3 rounded-xl border border-border bg-card p-4">
      <div className="relative">
        <Search className="pointer-events-none absolute top-1/2 left-2.5 size-4 -translate-y-1/2 text-muted-foreground" />
        <Input
          placeholder="Search transactions..."
          className="pl-8"
          value={value.search}
          onChange={(e) => set("search", e.target.value)}
        />
      </div>

      <div className="grid grid-cols-2 gap-3 sm:grid-cols-3 lg:grid-cols-6">
        <Select value={value.type} onValueChange={(v) => set("type", v as TransactionFilterState["type"])}>
          <SelectTrigger className="w-full">
            <SelectValue placeholder="Type">
              {(v: string) => (v === "ALL" ? "All types" : formatEnumLabel(v))}
            </SelectValue>
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="ALL">All types</SelectItem>
            {TRANSACTION_TYPES.map((t) => (
              <SelectItem key={t} value={t}>
                {formatEnumLabel(t)}
              </SelectItem>
            ))}
          </SelectContent>
        </Select>

        <Select value={value.categoryId} onValueChange={(v) => set("categoryId", v ?? "ALL")}>
          <SelectTrigger className="w-full">
            <SelectValue placeholder="Category">
              {(v: string) => (v === "ALL" ? "All categories" : (categories.find((c) => c.id === v)?.name ?? v))}
            </SelectValue>
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="ALL">All categories</SelectItem>
            {categories.map((c) => (
              <SelectItem key={c.id} value={c.id}>
                {c.name}
              </SelectItem>
            ))}
          </SelectContent>
        </Select>

        <Input type="date" value={value.from} onChange={(e) => set("from", e.target.value)} title="From date" />
        <Input type="date" value={value.to} onChange={(e) => set("to", e.target.value)} title="To date" />
        <Input
          type="number"
          placeholder="Min amount"
          value={value.minAmount}
          onChange={(e) => set("minAmount", e.target.value)}
        />
        <Input
          type="number"
          placeholder="Max amount"
          value={value.maxAmount}
          onChange={(e) => set("maxAmount", e.target.value)}
        />
      </div>

      {hasActiveFilters && (
        <Button variant="ghost" size="sm" className="text-muted-foreground" onClick={() => onChange(EMPTY_FILTER_STATE)}>
          <X className="size-3.5" />
          Clear filters
        </Button>
      )}
    </div>
  )
}
