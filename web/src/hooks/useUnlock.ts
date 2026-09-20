import { useState, useEffect, useCallback } from 'react'
import type { UnlockInfo } from '../types'

const UNLOCK_KEY = 'moviesforever_unlock'

export function useUnlock() {
  const [unlockInfo, setUnlockInfo] = useState<UnlockInfo | null>(() => {
    try {
      const raw = localStorage.getItem(UNLOCK_KEY)
      return raw ? JSON.parse(raw) : null
    } catch {
      return null
    }
  })

  const isUnlocked = unlockInfo !== null
  const loading = false

  const saveUnlock = useCallback((info: UnlockInfo) => {
    localStorage.setItem(UNLOCK_KEY, JSON.stringify(info))
    setUnlockInfo(info)
  }, [])

  const clearUnlock = useCallback(() => {
    localStorage.removeItem(UNLOCK_KEY)
    setUnlockInfo(null)
  }, [])

  return { unlockInfo, isUnlocked, loading, saveUnlock, clearUnlock }
}
