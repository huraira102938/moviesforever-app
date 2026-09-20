import React, { createContext, useContext, useMemo } from 'react'
import {
  type DocumentData,
  collection,
  orderBy,
  query,
  where,
  doc,
} from 'firebase/firestore'
import { db } from '../config/firebase'
import { useFirestoreCollection } from '../hooks/useFirestoreCollection'
import { useUnlock } from '../hooks/useUnlock'
import { useInstall } from '../hooks/useInstall'
import type {
  Movie,
  Category,
  Genre,
  Banner,
  TrendingItem,
  TrendingMovie,
  PricingSettings,
  PaymentDetails,
  ContactDetails,
  UserAccount,
  AppNotification,
  AppShareLink,
  UnlockInfo,
} from '../types'

interface AppContextValue {
  movies: Movie[]
  categories: Category[]
  genres: Genre[]
  banners: Banner[]
  trendingItems: TrendingItem[]
  trendingMovies: TrendingMovie[]
  pricing: PricingSettings | null
  paymentDetails: PaymentDetails | null
  contactDetails: ContactDetails | null
  notifications: AppNotification[]
  appShareLink: AppShareLink | null
  userAccount: UserAccount | null
  unlockInfo: UnlockInfo | null
  isUnlocked: boolean
  isInstalled: boolean
  myNotifications: AppNotification[]
  unlockedMovieIds: Set<string>
  saveUnlock: (info: UnlockInfo) => void
  clearUnlock: () => void
  markInstalled: () => void
}

const AppContext = createContext<AppContextValue | null>(null)

export function AppProvider({ children }: { children: React.ReactNode }) {
  const { unlockInfo, isUnlocked, saveUnlock, clearUnlock } = useUnlock()
  const { isInstalled, markInstalled } = useInstall()

  const movies = useFirestoreCollection<Movie>(
    'movies',
    (id, data): Movie => ({
      id,
      title: (data.title as string) ?? '',
      category: (data.category as string) ?? '',
      genres: (data.genres as string[]) ?? [],
      year: (data.year as number) ?? null,
      description: (data.description as string) ?? '',
      imdbRating: (data.imdbRating as number) ?? null,
      badge: (data.badge as string) ?? null,
      trailerKey: (data.trailerKey as string) ?? null,
      trailerUrl: (data.trailerUrl as string) ?? null,
      videoKey: (data.videoKey as string) ?? '',
      videoUrl: (data.videoUrl as string) ?? '',
      thumbnailKey: (data.thumbnailKey as string) ?? '',
      thumbnailUrl: (data.thumbnailUrl as string) ?? '',
      isFree: (data.isFree as boolean) ?? false,
      language: (data.language as string) ?? '',
      availableDubs: (data.availableDubs as string[]) ?? [],
      sections: (data.sections as string[]) ?? [],
      paused: (data.paused as boolean) ?? false,
      createdAt: (data.createdAt as string) ?? '',
      updatedAt: (data.updatedAt as string) ?? '',
    }),
    [orderBy('createdAt', 'desc')],
  )

  const categories = useFirestoreCollection<Category>(
    'categories',
    (id, data): Category => ({
      id,
      name: (data.name as string) ?? '',
      order: (data.order as number) ?? 0,
    }),
    [orderBy('order', 'asc')],
  )

  const genres = useFirestoreCollection<Genre>(
    'genres',
    (id, data): Genre => ({
      id,
      name: (data.name as string) ?? '',
    }),
  )

  const banners = useFirestoreCollection<Banner>(
    'banners',
    (id, data): Banner => ({
      id,
      imageKey: (data.imageKey as string) ?? '',
      imageUrl: (data.imageUrl as string) ?? '',
      clickable: (data.clickable as boolean) ?? false,
      linkedMovieId: (data.linkedMovieId as string) ?? null,
      order: (data.order as number) ?? 0,
    }),
    [orderBy('order', 'asc')],
  )

  const trendingItems = useFirestoreCollection<TrendingItem>(
    'trending',
    (id, data): TrendingItem => ({
      id,
      movieId: (data.movieId as string) ?? '',
      order: (data.order as number) ?? 0,
    }),
    [orderBy('order', 'asc')],
  )

  const pricing = useFirestoreCollection<PricingSettings>(
    'settings',
    (_id, data): PricingSettings => ({
      standardPrice: (data.standardPrice as number) ?? 0,
      referralPrice: (data.referralPrice as number) ?? 0,
      referralPayout: (data.referralPayout as number) ?? 0,
      note: (data.note as string) ?? '',
    }),
    [where('__name__', '==', 'pricing')],
  )[0] ?? null

  const paymentDetails = useFirestoreCollection<PaymentDetails>(
    'settings',
    (_id, data): PaymentDetails => ({
      bankName: (data.bankName as string) ?? '',
      accountTitle: (data.accountTitle as string) ?? '',
      accountNumber: (data.accountNumber as string) ?? '',
    }),
    [where('__name__', '==', 'payment-details')],
  )[0] ?? null

  const contactDetails = useFirestoreCollection<ContactDetails>(
    'settings',
    (_id, data): ContactDetails => ({
      whatsappNumber: (data.whatsappNumber as string) ?? '',
      groupTitle: (data.groupTitle as string) ?? '',
      groupLink: (data.groupLink as string) ?? '',
    }),
    [where('__name__', '==', 'contact')],
  )[0] ?? null

  const notifications = useFirestoreCollection<AppNotification>(
    'notifications',
    (id, data): AppNotification => ({
      id,
      text: (data.text as string) ?? '',
      targets: (data.targets as string[]) ?? [],
      createdAt: (data.createdAt as string) ?? '',
    }),
    [orderBy('createdAt', 'desc')],
  )

  const appShareLinks = useFirestoreCollection<AppShareLink>(
    'app-sharing',
    (id, data): AppShareLink => ({
      id,
      title: (data.title as string) ?? '',
      apkUrl: (data.apkUrl as string) ?? '',
      version: (data.version as string) ?? '',
      createdAt: (data.createdAt as string) ?? '',
    }),
    [orderBy('createdAt', 'desc')],
  )

  const appShareLink = useMemo(() => appShareLinks[0] ?? null, [appShareLinks])

  const userAccount = useFirestoreCollection<UserAccount>(
    'users',
    (id, data): UserAccount => ({
      id,
      username: (data.username as string) ?? '',
      realName: (data.realName as string) ?? '',
      phoneNumber: (data.phoneNumber as string) ?? '',
      paymentMethod: (data.paymentMethod as string) ?? '',
      paymentNumber: (data.paymentNumber as string) ?? '',
      accountTitle: (data.accountTitle as string) ?? '',
      jazzCashNumber: (data.jazzCashNumber as string) ?? '',
      jazzCashTitle: (data.jazzCashTitle as string) ?? '',
      referralCount: (data.referralCount as number) ?? 0,
      paused: (data.paused as boolean) ?? false,
      pauseUserNote: (data.pauseUserNote as string) ?? '',
    }),
    [where('__name__', '==', unlockInfo?.id ?? '__none__')],
    isUnlocked && !!unlockInfo?.id,
  )[0] ?? null

  const myNotifications = useMemo(() => {
    if (!userAccount && !isUnlocked) return []
    const group = isUnlocked ? 'paid' : 'free'
    return notifications.filter((n) => n.targets.includes(group))
  }, [notifications, userAccount, isUnlocked])

  const trendingMovies = useMemo(() => {
    const movieMap = new Map(movies.map((m) => [m.id, m]))
    return trendingItems
      .map((t) => {
        const movie = movieMap.get(t.movieId)
        if (!movie || movie.paused || !movie.thumbnailUrl) return null
        return {
          ...t,
          title: movie.title,
          thumbnailUrl: movie.thumbnailUrl,
          year: movie.year,
          badge: movie.badge,
          isFree: movie.isFree,
          imdbRating: movie.imdbRating,
        }
      })
      .filter((t): t is TrendingMovie => t !== null)
  }, [trendingItems, movies])

  const value = useMemo<AppContextValue>(
    () => ({
      movies,
      categories,
      genres,
      banners,
      trendingItems,
      trendingMovies,
      pricing,
      paymentDetails,
      contactDetails,
      notifications,
      appShareLink,
      userAccount,
      unlockInfo,
      isUnlocked,
      isInstalled,
      myNotifications,
      unlockedMovieIds: new Set<string>(),
      saveUnlock,
      clearUnlock,
      markInstalled,
    }),
    [
      movies,
      categories,
      genres,
      banners,
      trendingItems,
      trendingMovies,
      pricing,
      paymentDetails,
      contactDetails,
      notifications,
      appShareLink,
      userAccount,
      unlockInfo,
      isUnlocked,
      isInstalled,
      myNotifications,
      saveUnlock,
      clearUnlock,
      markInstalled,
    ],
  )

  return <AppContext.Provider value={value}>{children}</AppContext.Provider>
}

export function useApp(): AppContextValue {
  const ctx = useContext(AppContext)
  if (!ctx) throw new Error('useApp must be used within AppProvider')
  return ctx
}
