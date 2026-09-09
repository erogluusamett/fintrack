import { ArrowDownLeft, ArrowLeftRight, ArrowUpRight, Pencil, Trash2 } from "lucide-react"
import { Badge } from "@/components/ui/badge"
import { Button } from "@/components/ui/button"
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table"
import type { Transaction } from "@/types/transaction"
import { formatCurrency, formatDate } from "@/utils/currency"

const TYPE_META = {
  INCOME: { label: "Income", icon: ArrowDownLeft, className: "bg-success/10 text-success" },
  EXPENSE: { label: "Expense", icon: ArrowUpRight, className: "bg-destructive/10 text-destructive" },
  TRANSFER: { label: "Transfer", icon: ArrowLeftRight, className: "bg-primary/10 text-primary" },
} as const

interface TransactionsTableProps {
  transactions: Transaction[]
  onEdit: (transaction: Transaction) => void
  onDelete: (transaction: Transaction) => void
}

export function TransactionsTable({ transactions, onEdit, onDelete }: TransactionsTableProps) {
  return (
    <div className="overflow-x-auto rounded-xl border border-border">
      <Table>
        <TableHeader>
          <TableRow>
            <TableHead>Date</TableHead>
            <TableHead>Description</TableHead>
            <TableHead>Category</TableHead>
            <TableHead>Type</TableHead>
            <TableHead className="text-right">Amount</TableHead>
            <TableHead className="w-20 text-right">Actions</TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          {transactions.map((tx) => {
            const meta = TYPE_META[tx.type]
            return (
              <TableRow key={tx.id}>
                <TableCell className="whitespace-nowrap text-muted-foreground">
                  {formatDate(tx.transactionDate)}
                </TableCell>
                <TableCell className="max-w-[220px] truncate font-medium">
                  {tx.description || "—"}
                </TableCell>
                <TableCell className="text-muted-foreground">{tx.categoryName ?? "Uncategorized"}</TableCell>
                <TableCell>
                  <Badge variant="secondary" className={meta.className}>
                    <meta.icon data-icon="inline-start" className="size-3" />
                    {meta.label}
                  </Badge>
                </TableCell>
                <TableCell
                  className={
                    tx.type === "EXPENSE"
                      ? "text-right font-medium text-destructive whitespace-nowrap"
                      : "text-right font-medium text-success whitespace-nowrap"
                  }
                >
                  {tx.type === "EXPENSE" ? "-" : "+"}
                  {formatCurrency(tx.amount, tx.currency)}
                </TableCell>
                <TableCell>
                  <div className="flex justify-end gap-1">
                    <Button variant="ghost" size="icon-sm" aria-label="Edit" onClick={() => onEdit(tx)}>
                      <Pencil className="size-3.5" />
                    </Button>
                    <Button
                      variant="ghost"
                      size="icon-sm"
                      aria-label="Delete"
                      className="text-destructive hover:text-destructive"
                      onClick={() => onDelete(tx)}
                    >
                      <Trash2 className="size-3.5" />
                    </Button>
                  </div>
                </TableCell>
              </TableRow>
            )
          })}
        </TableBody>
      </Table>
    </div>
  )
}
