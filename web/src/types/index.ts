export interface Movie {
  id: string
  title: string
  category: string
  genres: string[]
  year: number | null
  description: string
  imdbRating: number | null
  badge: string | null
  trailerKey: string | null
  trailerUrl: string | null
  videoKey: string
  videoUrl: string
  thumbnailKey: string
  thumbnailUrl: string
  isFree: boolean
  language: string
  availableDubs: string[]
  sections: string[]
  paused: boolean
  createdAt: string
  updatedAt: string
}

export interface Category {
  id: string
  name: string
  order: number
}

export interface Genre {
  id: string
  name: string
}

export interface Banner {
  id: string
  imageKey: string
  imageUrl: string
  clickable: boolean
  linkedMovieId: string | null
  order: number
}

export interface TrendingItem {
  id: string
  movieId: string
  order: number
}

export interface TrendingMovie extends TrendingItem {
  title: string
  thumbnailUrl: string
  year: number | null
  badge: string | null
  isFree: boolean
  imdbRating: number | null
}

export interface PricingSettings {
  standardPrice: number
  referralPrice: number
  referralPayout: number
  note: string
}

export interface PaymentDetails {
  bankName: string
  accountTitle: string
  accountNumber: string
}

export interface ContactDetails {
  whatsappNumber: string
  groupTitle: string
  groupLink: string
}

export interface UserAccount {
  id: string
  username: string
  realName: string
  phoneNumber: string
  paymentMethod: string
  paymentNumber: string
  accountTitle: string
  jazzCashNumber: string
  jazzCashTitle: string
  referralCount: number
  paused: boolean
  pauseUserNote: string
}

export interface AppNotification {
  id: string
  text: string
  targets: string[]
  createdAt: string
}

export interface AppShareLink {
  id: string
  title: string
  apkUrl: string
  version: string
  createdAt: string
}

export interface UnlockInfo {
  id: string
  username: string
  unlockedAt: number
}

export const SectionLabels: Record<string, string> = {
  'recently-added': 'Recently Added',
  'hot': 'Hot',
  'all-time-hit': 'All-time Hit',
  'hit-of-this-year': 'Hit of This Year',
}

export const SectionOrder: readonly string[] = [
  'recently-added',
  'hot',
  'all-time-hit',
  'hit-of-this-year',
]

export type MovieDownloadStatus = 'not-downloaded' | 'downloading' | 'completed' | 'failed'
