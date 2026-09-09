import { api, unwrap } from "./axios"
import type { ApiResponse } from "@/types/api"
import type { Category, CreateCategoryRequest, UpdateCategoryRequest } from "@/types/category"

export const categoryApi = {
  list: () => unwrap(api.get<ApiResponse<Category[]>>("/categories")),

  create: (body: CreateCategoryRequest) => unwrap(api.post<ApiResponse<Category>>("/categories", body)),

  update: (id: string, body: UpdateCategoryRequest) =>
    unwrap(api.put<ApiResponse<Category>>(`/categories/${id}`, body)),

  remove: (id: string) => api.delete(`/categories/${id}`),
}
