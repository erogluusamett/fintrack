import { useQuery } from "@tanstack/react-query"
import { categoryApi } from "@/api/category.api"
import { queryKeys } from "@/api/query-keys"
import type { CategoryType } from "@/types/enums"

export function useCategories(type?: CategoryType) {
  const query = useQuery({
    queryKey: queryKeys.categories,
    queryFn: () => categoryApi.list(),
    staleTime: 5 * 60_000, // kategoriler nadiren değişir, agresif refetch gerekmiyor
  })

  const categories = type ? (query.data ?? []).filter((c) => c.type === type) : (query.data ?? [])

  return { ...query, categories }
}
