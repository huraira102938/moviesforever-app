const WORKER_URL = import.meta.env.VITE_WORKER_URL || 'https://moviesforever.huraira102938.workers.dev'

export interface RedeemResponse {
  success: boolean
  message: string
  username?: string
}

export interface SignedUrlResponse {
  url: string | null
  allowed: boolean
  message?: string
}

async function post<T>(path: string, body: Record<string, unknown>): Promise<T> {
  const res = await fetch(`${WORKER_URL}${path}`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body),
  })
  return res.json() as Promise<T>
}

export function redeem(id: string, username: string): Promise<RedeemResponse> {
  return post<RedeemResponse>('/redeem', { id, username })
}

export function getSignedUrl(movieId: string): Promise<SignedUrlResponse> {
  return post<SignedUrlResponse>('/signed-url', { movieId })
}
