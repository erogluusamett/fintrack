import { useQuery } from "@tanstack/react-query"
import { transactionApi } from "@/api/transaction.api"

/** Reports sayfası için: tarih aralığındaki tüm transaction'lar tek seferde,
 *  client-side toplama/CSV export için (özel bir summary endpoint'i yok). */
export function useReportTransactions(from: string, to: string) {
  return useQuery({
    queryKey: ["reports", "transactions", from, to],
    queryFn: () =>
      transactionApi.list({
        from,
        to,
        page: 0,
        size: 1000,
        sort: "transactionDate,asc",
      }),
    enabled: Boolean(from && to),
  })
}
