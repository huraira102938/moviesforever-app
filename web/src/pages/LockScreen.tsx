import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import {
  Film,
  Lock,
  CheckCircle,
  XCircle,
  Zap,
  Monitor,
  Shield,
  BadgeCheck,
} from 'lucide-react'
import { useApp } from '../context/AppContext'
import { redeem } from '../lib/workerApi'
import { Button } from '../components/ui/Button'
import { Card } from '../components/ui/Card'
import { Input } from '../components/ui/Input'
import { Modal } from '../components/ui/Modal'
import AdminNote from '../components/common/AdminNote'
import GlowOrb from '../components/ui/GlowOrb'

export default function LockScreen() {
  const navigate = useNavigate()
  const { pricing, saveUnlock } = useApp()
  const [showSignIn, setShowSignIn] = useState(false)
  const [codeId, setCodeId] = useState('')
  const [username, setUsername] = useState('')
  const [redeeming, setRedeeming] = useState(false)
  const [error, setError] = useState('')

  const handleRedeem = async () => {
    if (!codeId.trim() || !username.trim()) {
      setError('Please enter both your Code ID and Username.')
      return
    }
    setError('')
    setRedeeming(true)
    try {
      const res = await redeem(codeId.trim(), username.trim())
      if (res.success) {
        saveUnlock({
          id: codeId.trim(),
          username: username.trim(),
          unlockedAt: Date.now(),
        })
        navigate('/celebration', { replace: true })
      } else {
        setError(res.message || 'Redemption failed. Please try again.')
      }
    } catch {
      setError('Network error. Please try again.')
    } finally {
      setRedeeming(false)
    }
  }

  return (
    <div className="min-h-screen flex flex-col relative overflow-hidden px-5 py-8">
      <GlowOrb className="absolute -top-20 left-1/3 w-96 h-96 rounded-full blur-3xl bg-brand-500/10 pointer-events-none" />

      <header className="relative z-10 mx-auto w-full max-w-3xl flex items-center justify-between mb-10">
        <div className="flex items-center gap-2.5 text-white">
          <Film className="w-7 h-7 text-brand-400" />
          <span className="text-lg font-bold tracking-tight">
            Movies<span className="text-brand-400">Forever</span>
          </span>
        </div>
        <button
          onClick={() => setShowSignIn(true)}
          className="text-brand-400 text-sm font-semibold hover:text-brand-300 transition"
        >
          Sign In
        </button>
      </header>

      <div className="relative z-10 w-full max-w-3xl mx-auto flex flex-col items-center gap-8">
        <div className="text-center">
          <h1 className="text-2xl sm:text-3xl font-black text-white tracking-tight mb-2">
            Choose how you want to watch
          </h1>
          <p className="text-gray-400 text-sm">Get lifetime access or explore free content</p>
        </div>

        <div className="grid sm:grid-cols-2 gap-5 w-full max-w-xl">
          <Card className="relative border-brand-500/30">
            <span className="absolute -top-3 left-4 px-3 py-0.5 bg-amber-500/90 text-black text-xs font-semibold rounded-full">
              Limited
            </span>
            <div className="mb-4">
              <h3 className="text-lg font-bold text-white mb-1">Lifetime Access</h3>
              <div className="flex items-baseline gap-1">
                <span className="text-2xl font-black text-brand-400">
                  PKR {pricing?.standardPrice?.toLocaleString() ?? '—'}
                </span>
                <span className="text-xs text-gray-500">one-time</span>
              </div>
            </div>

            <ul className="space-y-2 mb-5">
              {[
                'All movies included',
                'HD & 4K quality',
                'Unlimited streaming',
                'New movies weekly',
                'No ads ever',
              ].map((item) => (
                <li key={item} className="flex items-center gap-2 text-sm text-gray-300">
                  <CheckCircle className="w-4 h-4 text-brand-400 shrink-0" />
                  {item}
                </li>
              ))}
            </ul>

            <Button onClick={() => navigate('/lock/payment')} className="w-full">
              <Lock className="w-4 h-4" />
              Unlock Now
            </Button>
          </Card>

          <Card>
            <div className="mb-4">
              <h3 className="text-lg font-bold text-white mb-1">Free Trial</h3>
              <p className="text-xs text-gray-500">No payment needed</p>
            </div>

            <ul className="space-y-2 mb-3">
              {['Browse free movies', 'Watch trailers'].map((item) => (
                <li key={item} className="flex items-center gap-2 text-sm text-gray-300">
                  <CheckCircle className="w-4 h-4 text-brand-400 shrink-0" />
                  {item}
                </li>
              ))}
            </ul>

            <ul className="space-y-2 mb-5">
              {['Limited selection', 'No premium content'].map((item) => (
                <li key={item} className="flex items-center gap-2 text-sm text-gray-500">
                  <XCircle className="w-4 h-4 text-gray-600 shrink-0" />
                  {item}
                </li>
              ))}
            </ul>

            <Button variant="outline" className="w-full" onClick={() => navigate('/', { replace: true })}>
              Browse Free
            </Button>
          </Card>
        </div>

        <AdminNote />

        <div className="grid grid-cols-2 sm:grid-cols-4 gap-3 w-full max-w-xl">
          {[
            { icon: Zap, label: 'Instant Unlock', desc: 'WhatsApp activation' },
            { icon: Monitor, label: 'Ultra HD / 4K', desc: 'High video quality' },
            { icon: Shield, label: 'Safe Transfer', desc: 'Verified account' },
            { icon: BadgeCheck, label: 'No Ads Ever', desc: 'Zero interruptions' },
          ].map(({ icon: Icon, label, desc }) => (
            <div key={label} className="flex flex-col items-center text-center py-4 rounded-xl border border-white/10 bg-white/[0.03]">
              <Icon className="w-6 h-6 text-brand-400 mb-2" />
              <span className="text-sm font-semibold text-white">{label}</span>
              <span className="text-xs text-gray-500 mt-0.5">{desc}</span>
            </div>
          ))}
        </div>

        <div className="w-full max-w-xl flex items-center gap-3 px-4 py-3 rounded-xl border border-white/10 bg-white/[0.03]">
          <div className="w-2 h-2 rounded-full bg-brand-400 animate-pulse-dot shrink-0" />
          <p className="text-xs text-gray-400">
            Enjoy lifetime access with 24/7 WhatsApp verification support.
          </p>
        </div>
      </div>

      <Modal open={showSignIn} onClose={() => { setShowSignIn(false); setError('') }}>
        <div className="flex flex-col gap-5">
          <div>
            <h3 className="text-lg font-bold text-white">Sign In</h3>
            <p className="text-sm text-gray-400 mt-1">Enter your code to unlock MoviesForever</p>
          </div>

          <div className="flex flex-col gap-3">
            <Input
              placeholder="Code ID"
              value={codeId}
              onChange={(e) => setCodeId(e.target.value)}
            />
            <Input
              placeholder="Username"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
            />
          </div>

          {error && (
            <p className="text-sm text-red-400">{error}</p>
          )}

          <div className="flex gap-3">
            <Button
              onClick={handleRedeem}
              loading={redeeming}
              className="flex-1"
            >
              Unlock Forever
            </Button>
            <Button
              variant="ghost"
              onClick={() => { setShowSignIn(false); setError('') }}
            >
              Cancel
            </Button>
          </div>
        </div>
      </Modal>
    </div>
  )
}