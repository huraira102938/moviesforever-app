import { useNavigate } from 'react-router-dom'
import { ArrowLeft, RotateCcw, ExternalLink, Film } from 'lucide-react'
import { useApp } from '../context/AppContext'
import { Card } from '../components/ui/Card'
import { openWhatsApp } from '../lib/utils'

export default function SettingsScreen() {
  const navigate = useNavigate()
  const { contactDetails, clearUnlock, isUnlocked } = useApp()

  return (
    <div className="min-h-screen">
      <div className="max-w-2xl mx-auto px-5 py-10 flex flex-col gap-6">
        <div className="flex items-center gap-3">
          <button
            onClick={() => navigate(-1)}
            className="w-10 h-10 rounded-full bg-white/10 border border-white/10 flex items-center justify-center text-white hover:bg-white/15 transition"
          >
            <ArrowLeft className="w-5 h-5" />
          </button>
          <h1 className="text-xl font-bold text-white">Settings</h1>
        </div>

        <Card>
          <h3 className="text-white font-semibold text-sm mb-3">Support</h3>
          <div className="flex flex-col gap-3">
            {contactDetails?.groupTitle && (
              <button
                onClick={() => {
                  if (contactDetails.groupLink) window.open(contactDetails.groupLink, '_blank', 'noopener,noreferrer')
                }}
                className="flex items-center justify-between px-4 py-3 rounded-xl border border-white/10 bg-white/5 hover:bg-white/[0.07] transition"
              >
                <div className="flex items-center gap-3">
                  <ExternalLink className="w-4 h-4 text-gray-400" />
                  <span className="text-white text-sm">{contactDetails.groupTitle}</span>
                </div>
              </button>
            )}
            {contactDetails?.whatsappNumber && (
              <button
                onClick={() => openWhatsApp(contactDetails.whatsappNumber, 'Hi! I need help with MoviesForever.')}
                className="flex items-center justify-between px-4 py-3 rounded-xl border border-white/10 bg-white/5 hover:bg-white/[0.07] transition"
              >
                <div className="flex items-center gap-3">
                  <span className="text-lg">💬</span>
                  <span className="text-white text-sm">WhatsApp Support</span>
                </div>
              </button>
            )}
          </div>
        </Card>

        <Card>
          <h3 className="text-white font-semibold text-sm mb-3">About</h3>
          <div className="flex items-center gap-3 px-4 py-3 rounded-xl border border-white/10 bg-white/5">
            <Film className="w-5 h-5 text-brand-400" />
            <div>
              <p className="text-white text-sm font-medium">MoviesForever</p>
              <p className="text-gray-500 text-xs">Stream movies in HD quality</p>
            </div>
          </div>
        </Card>

        {isUnlocked && (
          <Card className="border-red-500/20">
            <h3 className="text-white font-semibold text-sm mb-3">Account</h3>
            <button
              onClick={() => {
                if (window.confirm('Are you sure you want to reset your unlock? You will need to enter your code again.')) {
                  clearUnlock()
                  navigate('/lock', { replace: true })
                }
              }}
              className="flex items-center gap-3 w-full px-4 py-3 rounded-xl border border-red-500/20 bg-red-500/5 text-red-400 hover:bg-red-500/10 transition"
            >
              <RotateCcw className="w-4 h-4" />
              <span className="text-sm font-medium">Reset Unlock</span>
            </button>
          </Card>
        )}
      </div>
    </div>
  )
}