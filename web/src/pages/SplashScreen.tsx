import { useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { Film } from 'lucide-react'
import { useApp } from '../context/AppContext'
import { Spinner } from '../components/ui/Spinner'

export default function SplashScreen() {
  const navigate = useNavigate()
  const { isInstalled, isUnlocked } = useApp()

  useEffect(() => {
    const timer = setTimeout(() => {
      if (!isInstalled) {
        navigate('/welcome', { replace: true })
      } else if (!isUnlocked) {
        navigate('/lock', { replace: true })
      } else {
        navigate('/', { replace: true })
      }
    }, 1500)

    return () => clearTimeout(timer)
  }, [isInstalled, isUnlocked, navigate])

  return (
    <div className="min-h-screen flex flex-col items-center justify-center gap-6 bg-[#0b0b18]">
      <div className="w-20 h-20 rounded-2xl bg-brand-500/15 border border-brand-500/30 flex items-center justify-center animate-pulse-glow">
        <Film className="w-10 h-10 text-brand-400" />
      </div>
      <h1 className="text-2xl font-bold text-white tracking-tight">
        Movies<span className="text-brand-400">Forever</span>
      </h1>
      <Spinner size="md" />
    </div>
  )
}
