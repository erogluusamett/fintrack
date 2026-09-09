import { zodResolver } from "@hookform/resolvers/zod"
import { useEffect } from "react"
import { useForm } from "react-hook-form"
import { toast } from "sonner"
import { PageHeader } from "@/components/common/PageHeader"
import { Avatar, AvatarFallback } from "@/components/ui/avatar"
import { Badge } from "@/components/ui/badge"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
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
import { useAuthStore } from "@/store/auth-store"
import { CURRENCIES } from "@/types/enums"
import { formatDate } from "@/utils/currency"
import { getErrorMessage } from "@/utils/error-message"
import { changePasswordSchema, profileSchema, type ChangePasswordFormValues, type ProfileFormValues } from "./schemas"
import { useChangePassword, useUpdateProfile } from "./use-settings"

function ProfileCard() {
  const user = useAuthStore((state) => state.user)
  const updateProfile = useUpdateProfile()

  const form = useForm<ProfileFormValues>({
    resolver: zodResolver(profileSchema),
    defaultValues: {
      firstName: user?.firstName ?? "",
      lastName: user?.lastName ?? "",
      defaultCurrency: user?.defaultCurrency ?? "TRY",
    },
  })

  useEffect(() => {
    if (!user) return
    form.reset({ firstName: user.firstName, lastName: user.lastName, defaultCurrency: user.defaultCurrency })
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [user?.id])

  function onSubmit(values: ProfileFormValues) {
    updateProfile.mutate(values, {
      onSuccess: () => toast.success("Profile updated"),
      onError: (error) => toast.error(getErrorMessage(error)),
    })
  }

  const initials = user ? `${user.firstName[0] ?? ""}${user.lastName[0] ?? ""}`.toUpperCase() : ""

  return (
    <Card>
      <CardHeader>
        <CardTitle className="text-base">Profile</CardTitle>
        <CardDescription>Your personal details and default currency.</CardDescription>
      </CardHeader>
      <CardContent className="space-y-5">
        <div className="flex items-center gap-3">
          <Avatar className="size-12">
            <AvatarFallback className="bg-secondary text-sm font-medium">{initials}</AvatarFallback>
          </Avatar>
          <div className="min-w-0">
            <p className="truncate text-sm font-medium">{user?.email}</p>
            <div className="mt-1 flex items-center gap-1.5">
              <Badge variant={user?.emailVerified ? "secondary" : "outline"} className="h-4 px-1.5 text-[10px]">
                {user?.emailVerified ? "Verified" : "Unverified"}
              </Badge>
              {user?.createdAt && (
                <span className="text-xs text-muted-foreground">Member since {formatDate(user.createdAt)}</span>
              )}
            </div>
          </div>
        </div>

        <Form {...form}>
          <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-4">
            <div className="grid grid-cols-1 gap-3 sm:grid-cols-2">
              <FormField
                control={form.control}
                name="firstName"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>First name</FormLabel>
                    <FormControl>
                      <Input {...field} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
              <FormField
                control={form.control}
                name="lastName"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Last name</FormLabel>
                    <FormControl>
                      <Input {...field} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
            </div>

            <FormField
              control={form.control}
              name="defaultCurrency"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Default currency</FormLabel>
                  <Select value={field.value} onValueChange={field.onChange}>
                    <FormControl>
                      <SelectTrigger className="w-full sm:w-48">
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

            <Button type="submit" disabled={updateProfile.isPending}>
              {updateProfile.isPending ? "Saving..." : "Save changes"}
            </Button>
          </form>
        </Form>
      </CardContent>
    </Card>
  )
}

function ChangePasswordCard() {
  const changePassword = useChangePassword()

  const form = useForm<ChangePasswordFormValues>({
    resolver: zodResolver(changePasswordSchema),
    defaultValues: { currentPassword: "", newPassword: "", confirmPassword: "" },
  })

  function onSubmit(values: ChangePasswordFormValues) {
    changePassword.mutate(
      { currentPassword: values.currentPassword, newPassword: values.newPassword },
      {
        onSuccess: () => {
          toast.success("Password updated")
          form.reset()
        },
        onError: (error) => toast.error(getErrorMessage(error)),
      },
    )
  }

  return (
    <Card>
      <CardHeader>
        <CardTitle className="text-base">Change Password</CardTitle>
        <CardDescription>Choose a strong password you haven&apos;t used before.</CardDescription>
      </CardHeader>
      <CardContent>
        <Form {...form}>
          <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-4">
            <FormField
              control={form.control}
              name="currentPassword"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Current password</FormLabel>
                  <FormControl>
                    <Input type="password" autoComplete="current-password" {...field} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
            <FormField
              control={form.control}
              name="newPassword"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>New password</FormLabel>
                  <FormControl>
                    <Input type="password" autoComplete="new-password" {...field} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
            <FormField
              control={form.control}
              name="confirmPassword"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Confirm new password</FormLabel>
                  <FormControl>
                    <Input type="password" autoComplete="new-password" {...field} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
            <Button type="submit" disabled={changePassword.isPending}>
              {changePassword.isPending ? "Updating..." : "Update password"}
            </Button>
          </form>
        </Form>
      </CardContent>
    </Card>
  )
}

export function SettingsPage() {
  return (
    <div className="space-y-6">
      <PageHeader title="Settings" description="Manage your profile and account security." />

      <div className="grid grid-cols-1 gap-4 lg:grid-cols-2">
        <ProfileCard />
        <ChangePasswordCard />
      </div>
    </div>
  )
}
