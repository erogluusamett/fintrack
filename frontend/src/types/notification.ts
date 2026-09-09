import type { NotificationType } from "./enums"

export interface Notification {
  id: string
  type: NotificationType
  title: string
  message: string
  read: boolean
  relatedEntityId: string | null
  relatedEntityType: string | null
  createdAt: string
}
