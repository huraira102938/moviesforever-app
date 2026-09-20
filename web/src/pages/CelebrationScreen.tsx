import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { PartyPopper, Film } from 'lucide-react'
import { Button } from '../components/ui/Button'
import GlowOrb from '../components/ui/GlowOrb'

export default function CelebrationScreen() {
  const navigate = useNavigate()
  const [dismissed, setDismissed] = useState(false)

  useEffect(() => {
    const timer = setTimeout(() => {
      if (!dismissed) navigate('/', { replace: true })
    }, 8000)
    return () => clearTimeout(timer)
  }, [navigate, dismissed])

  return (
    <div className="min-h-screen flex flex-col items-center justify-center relative overflow-hidden px-5">
      <GlowOrb className="absolute top-1/3 left-1/3 w-96 h-96 rounded-full blur-3xl bg-brand-500/15 pointer-events-none" />
      <GlowOrb className="absolute bottom-1/3 right-1/3 w-80 h-80 rounded-full blur-3xl bg-emerald-500/10 pointer-events-none" />

      {[...Array(12)].map((_, i) => (
        <div
          key={i}
          className="absolute w-2 h-2 rounded-full animate-float"
          style={{
            left: `${Math.random() * 100}%`,
            top: `${Math.random() * 100}%`,
            animationDelay: `${Math.random() * 3}s`,
            backgroundColor: ['#818cf8', '#a78bfa', '#f0abfc', '#34d399', '#fbbf24'][i % 5],
          }}
        />
      ))}

      <div className="relative z-10 flex flex-col items-center gap-6 max-w-md text-center">
        <div className="w-20 h-20 rounded-full bg-brand-500/15 border-2 border-brand-500/30 flex items-center justify-center animate-pulse-glow">
          <PartyPopper className="w-10 h-10 text-brand-400" />
        </div>

        <div>
          <h1 className="text-3xl font-black text-white tracking-tight mb-3">
            You're all set!
          </h1>
          <p className="text-gray-400 text-sm leading-relaxed">
            Welcome to MoviesForever. Enjoy unlimited access to every movie in HD quality.
          </p>
        </div>

        <div className="flex items-center gap-2.5 text-white">
          <Film className="w-5 h-5 text-brand-400" />
          <span className="font-bold">
            Movies<span className="text-brand-400">Forever</span>
          </span>
        </div>

        <Button onClick={() => navigate('/', { replace: true })} className="w-full">
          Start Exploring
        </Button>

        <p className="text-xs text-gray-500">Redirecting automatically in a few seconds...</p>
      </div>
    </div>
  )
}