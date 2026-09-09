/**
 * Yalnızca refresh token localStorage'da tutulur — access token kasıtlı
 * olarak sadece bellekte (bkz. store/auth-store.ts) yaşar ve sayfa
 * yenilendiğinde kaybolur (bootstrap sırasında refresh token ile yeniden
 * alınır). Backend refresh token'ı httpOnly cookie olarak değil JSON
 * body'de döndüğü için localStorage tek pratik seçenek; bu, XSS
 * senaryosunda access token'a göre daha uzun ömürlü bir riski kabul etmek
 * anlamına gelir — "gerçek" çözüm backend'in refresh token'ı httpOnly
 * cookie olarak set etmesi olurdu (mevcut API sözleşmesinin dışında).
 */
const STORAGE_KEY = "fintrack_refresh_token"

export function getRefreshToken(): string | null {
  try {
    return localStorage.getItem(STORAGE_KEY)
  } catch {
    return null
  }
}

export function setRefreshToken(token: string): void {
  try {
    localStorage.setItem(STORAGE_KEY, token)
  } catch {
    // localStorage kullanılamıyor (gizli sekme vb.) — sessizce yok say, oturum sayfa yenilenince düşer.
  }
}

export function clearRefreshToken(): void {
  try {
    localStorage.removeItem(STORAGE_KEY)
  } catch {
    // no-op
  }
}
