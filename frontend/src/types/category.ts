import type { CategoryType } from "./enums"

export interface Category {
  id: string
  name: string
  type: CategoryType
  isDefault: boolean
}

export interface CreateCategoryRequest {
  name: string
  type: CategoryType
}

export interface UpdateCategoryRequest {
  name: string
}
