export function formatDate(dateString: string): string {
  if (!dateString) return ''
  try {
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
    })
  } catch {
    return dateString
  }
}

export function formatFileSize(bytes: number): string {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(1)) + ' ' + sizes[i]
}

export function generateId(length = 8): string {
  const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789'
  let result = ''
  for (let i = 0; i < length; i++) {
    result += chars.charAt(Math.floor(Math.random() * chars.length))
  }
  return result
}

export function generateUsername(): string {
  const adjectives = ['Swift', 'Lucky', 'Golden', 'Cool', 'Happy', 'Brave', 'Wild', 'Epic']
  const nouns = ['Tiger', 'Wolf', 'Dragon', 'Hawk', 'Star', 'Phoenix', 'Storm', 'Fox']
  const adj = adjectives[Math.floor(Math.random() * adjectives.length)]
  const noun = nouns[Math.floor(Math.random() * nouns.length)]
  const num = Math.floor(Math.random() * 900 + 100)
  return `${adj}${noun}${num}`
}

export function openWhatsApp(number: string, message?: string): void {
  const url = new URL(`https://wa.me/${number.replace(/[^0-9]/g, '')}`)
  if (message) url.searchParams.set('text', message)
  window.open(url.toString(), '_blank', 'noopener,noreferrer')
}

export function copyToClipboard(text: string): Promise<boolean> {
  return navigator.clipboard.writeText(text).then(
    () => true,
    () => false,
  )
}

export function cn(...classes: (string | boolean | null | undefined)[]): string {
  return classes.filter(Boolean).join(' ')
}
