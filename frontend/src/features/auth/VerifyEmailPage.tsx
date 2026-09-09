import { useMutation } from "@tanstack/react-query"
import { useEffect, useRef, useState } from "react"
import { Link, useSearchParams } from "react-router-dom"
import { CheckCircle2, XCircle } from "lucide-react"
import { authApi } from "@/api/auth.api"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { getErrorMessage } from "@/utils/error-message"

export function VerifyEmailPage() {
  const [searchParams] = useSearchParams()
  const token = searchParams.get("token")
  const [error, setError] = useState<string | null>(null)
  const attempted = useRef(false)

  const verifyEmail = useMutation({
    mutationFn: (t: string) => authApi.verifyEmail({ token: t }),
  })

  useEffect(() => {
    if (!token || attempted.current) return
    attempted.current = true
    verifyEmail.mutate(token, { onError: (err) => setError(getErrorMessage(err)) })
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [token])

  if (!token) {
    return (
      <Card className="border-none text-center shadow-lg">
        <CardContent className="flex flex-col items-center gap-3 pt-6">
          <XCircle className="size-10 text-destructive" />
          <CardTitle className="text-lg">Invalid verification link</CardTitle>
          <CardDescription>This email verification link is missing or malformed.</CardDescription>
          <Link to="/login" className="text-sm font-medium text-primary hover:underline">
            Back to sign in
          </Link>
        </CardContent>
      </Card>
    )
  }

  return (
    <Card className="border-none text-center shadow-lg">
      <CardHeader className="space-y-1">
        <CardTitle className="text-xl">Verify your email</CardTitle>
      </CardHeader>
      <CardContent className="flex flex-col items-center gap-3">
        {verifyEmail.isPending || verifyEmail.isIdle ? (
          <CardDescription>Verifying your email address...</CardDescription>
        ) : verifyEmail.isSuccess ? (
          <>
            <CheckCircle2 className="size-10 text-success" />
            <CardDescription>Your email has been verified.</CardDescription>
            <Button render={<Link to="/login" />} nativeButton={false} className="mt-2 w-full">
              Continue to sign in
            </Button>
          </>
        ) : (
          <>
            <XCircle className="size-10 text-destructive" />
            <CardDescription>{error ?? "We couldn't verify this link."}</CardDescription>
            <Link to="/login" className="text-sm font-medium text-primary hover:underline">
              Back to sign in
            </Link>
          </>
        )}
      </CardContent>
    </Card>
  )
}
