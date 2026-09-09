/** "EXPENSE" -> "Expense". Backend enum değerleri her zaman upper-case sabitler. */
export function formatEnumLabel(value: string): string {
  return value.charAt(0) + value.slice(1).toLowerCase()
}
