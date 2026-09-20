import { Film, Play, Zap, Star } from 'lucide-react'
import { useApp } from '../context/AppContext'
import { Button } from '../components/ui/Button'
import GlowOrb from '../components/ui/GlowOrb'
import { useNavigate } from 'react-router-dom'

export default function WelcomeScreen() {
  const { markInstalled, pricing } = useApp()
  const navigate = useNavigate()

  const handleStart = () => {
    markInstalled()
    navigate('/lock', { replace: true })
  }

  return (
    <div className="min-h-screen flex flex-col items-center justify-center relative overflow-hidden px-5">
      <GlowOrb className="absolute top-1/4 left-1/4 w-80 h-80 rounded-full blur-3xl bg-brand-500/15 pointer-events-none" />
      <GlowOrb className="absolute bottom-1/4 right-1/4 w-80 h-80 rounded-full blur-3xl bg-fuchsia-500/10 pointer-events-none" />

      <div className="relative z-10 flex flex-col items-center gap-8 max-w-md text-center">
        <div className="w-20 h-20 rounded-2xl bg-brand-500/15 border border-brand-500/30 flex items-center justify-center">
          <Film className="w-10 h-10 text-brand-400" />
        </div>

        <div>
          <h1 className="text-3xl sm:text-4xl font-black text-white tracking-tight mb-3">
            Welcome to <span className="text-gradient">MoviesForever</span>
          </h1>
          <p className="text-gray-400 text-sm leading-relaxed">
            Stream unlimited movies in HD quality. New content added every week.
          </p>
        </div>

        <div className="grid grid-cols-3 gap-4 w-full">
          {[
            { icon: Play, label: 'Stream Free' },
            { icon: Star, label: 'Top Movies' },
            { icon: Zap, label: 'HD Quality' },
          ].map(({ icon: Icon, label }) => (
            <div
              key={label}
              className="flex flex-col items-center gap-2 py-3 rounded-xl border border-white/10 bg-white/5"
            >
              <Icon className="w-5 h-5 text-brand-400" />
              <span className="text-xs text-gray-400 font-medium">{label}</span>
            </div>
          ))}
        </div>

        {pricing?.note && (
          <div className="bg-brand-500/10 border border-brand-500/20 rounded-xl px-4 py-3 w-full">
            <p className="text-brand-300 text-sm">{pricing.note}</p>
          </div>
        )}

        <Button onClick={handleStart} className="w-full animate-pulse-glow">
          Start Browsing
        </Button>
      </div>
    </div>
  )
}
