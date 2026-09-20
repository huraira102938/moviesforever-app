import { Link } from 'react-router-dom'
import { User, Bell, Settings, Lock, Calendar, BadgeCheck } from 'lucide-react'
import { useApp } from '../context/AppContext'
import { Button } from '../components/ui/Button'
import { Card } from '../components/ui/Card'
import { Badge } from '../components/ui/Badge'
import GlowOrb from '../components/ui/GlowOrb'
import { formatDate } from '../lib/utils'

export default function ProfileScreen() {
  const { userAccount, unlockInfo, isUnlocked, contactDetails, myNotifications } = useApp()

  const username = userAccount?.username || unlockInfo?.username || null
  const memberSince = unlockInfo?.unlockedAt ? formatDate(new Date(unlockInfo.unlockedAt).toISOString()) : null

  const whatsappHref = contactDetails?.whatsappNumber
    ? `https://wa.me/${contactDetails.whatsappNumber.replace(/[^0-9]/g, '')}`
    : null

  return (
    <div className="min-h-screen relative overflow-hidden">
      <GlowOrb className="absolute top-0 right-1/4 w-80 h-80 rounded-full blur-3xl bg-brand-500/10 pointer-events-none" />

      <div className="relative z-10 max-w-2xl mx-auto px-5 py-10 flex flex-col gap-6">
        <div className="flex items-center gap-3 mb-2">
          <div className="w-1 h-6 rounded-full bg-brand-500" />
          <h1 className="text-2xl font-bold text-white">My Profile</h1>
        </div>

        <div className="flex flex-col items-center gap-3 py-6">
          <div className="w-16 h-16 rounded-full bg-white/10 border-2 border-brand-500/40 flex items-center justify-center">
            <User className="w-8 h-8 text-brand-400" />
          </div>
          <div className="text-center">
            <h2 className="text-xl font-bold text-white">
              {username || 'Free Preview User'}
            </h2>
            {isUnlocked ? (
              <div className="mt-1">
                <Badge>Lifetime Member</Badge>
              </div>
            ) : (
              <div className="mt-1">
                <Badge variant="info">Free Access</Badge>
              </div>
            )}
          </div>
          {memberSince && (
            <p className="text-xs text-gray-500 flex items-center gap-1.5">
              <Calendar className="w-3.5 h-3.5" />
              Member since {memberSince}
            </p>
          )}
        </div>

        {isUnlocked ? (
          <Card>
            <div className="flex items-start gap-3">
              <div className="w-10 h-10 rounded-full bg-emerald-500/15 flex items-center justify-center shrink-0">
                <BadgeCheck className="w-5 h-5 text-emerald-400" />
              </div>
              <div>
                <h3 className="text-white font-semibold text-sm">Account Active</h3>
                <p className="text-gray-400 text-xs mt-1">
                  You have full access to all movies. Enjoy unlimited streaming in HD quality.
                </p>
                {userAccount?.realName && (
                  <p className="text-gray-400 text-xs mt-2">
                    Name: <span className="text-gray-300">{userAccount.realName}</span>
                  </p>
                )}
              </div>
            </div>
          </Card>
        ) : (
          <Card className="text-center border-brand-500/30">
            <div className="w-14 h-14 rounded-full bg-brand-500/15 flex items-center justify-center mx-auto mb-4">
              <Lock className="w-7 h-7 text-brand-400" />
            </div>
            <h3 className="text-white font-bold text-lg mb-2">Unlock to Start Watching</h3>
            <p className="text-gray-400 text-sm mb-5 max-w-sm mx-auto">
              Get lifetime access to every movie in HD quality with a one-time payment.
            </p>
            <Link to="/lock">
              <Button>Unlock Forever</Button>
            </Link>
          </Card>
        )}

        <div className="flex flex-col gap-3">
          <Link
            to="/notifications"
            className="flex items-center justify-between px-5 py-4 rounded-2xl border border-white/10 bg-white/5 hover:bg-white/[0.07] transition"
          >
            <div className="flex items-center gap-3">
              <Bell className="w-5 h-5 text-gray-400" />
              <span className="text-white text-sm font-medium">Notifications</span>
            </div>
            {myNotifications.length > 0 ? (
              <span className="px-2 py-0.5 rounded-full bg-brand-500 text-white text-xs font-semibold min-w-[20px] text-center">
                {myNotifications.length > 9 ? '9+' : myNotifications.length}
              </span>
            ) : (
              <span className="text-gray-600 text-xs">{myNotifications.length}</span>
            )}
          </Link>

          <Link
            to="/settings"
            className="flex items-center justify-between px-5 py-4 rounded-2xl border border-white/10 bg-white/5 hover:bg-white/[0.07] transition"
          >
            <div className="flex items-center gap-3">
              <Settings className="w-5 h-5 text-gray-400" />
              <span className="text-white text-sm font-medium">Settings</span>
            </div>
          </Link>

          {whatsappHref && (
            <a
              href={whatsappHref}
              target="_blank"
              rel="noopener noreferrer"
              className="flex items-center justify-between px-5 py-4 rounded-2xl border border-white/10 bg-white/5 hover:bg-white/[0.07] transition"
            >
              <div className="flex items-center gap-3">
                <span>💬</span>
                <span className="text-white text-sm font-medium">WhatsApp Support</span>
              </div>
            </a>
          )}
        </div>
      </div>
    </div>
  )
}