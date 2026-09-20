import { useState, useEffect, useCallback } from 'react'

const INSTALL_KEY = 'moviesforever_installed'

export function useInstall() {
  const [isInstalled, setIsInstalled] = useState<boolean>(() => {
    return localStorage.getItem(INSTALL_KEY) === 'true'
  })

  const markInstalled = useCallback(() => {
    localStorage.setItem(INSTALL_KEY, 'true')
    setIsInstalled(true)
  }, [])

  return { isInstalled, markInstalled }
}
