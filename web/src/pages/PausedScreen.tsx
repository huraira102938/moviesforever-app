import { Film, MessageCircle } from 'lucide-react'
import { useApp } from '../context/AppContext'
import { Button } from '../components/ui/Button'
import { openWhatsApp } from '../lib/utils'
import GlowOrb from '../components/ui/GlowOrb'

export default function PausedScreen() {
  const { userAccount, contactDetails } = useApp()

  return (
    <div className="min-h-screen flex flex-col items-center justify-center relative overflow-hidden px-5">
      <GlowOrb className="absolute top-1/3 left-1/4 w-96 h-96 rounded-full blur-3xl bg-amber-500/10 pointer-events-none" />

      <div className="relative z-10 flex flex-col items-center gap-6 max-w-md text-center">
        <div className="w-20 h-20 rounded-full bg-amber-500/15 border-2 border-amber-500/30 flex items-center justify-center">
          <Film className="w-10 h-10 text-amber-400" />
        </div>

        <div>
          <h1 className="text-2xl font-black text-white tracking-tight mb-3">
            Account Paused
          </h1>
          <p className="text-gray-400 text-sm leading-relaxed">
            Your account has been paused by the administrator.
            Please contact support to resolve this issue.
          </p>
        </div>

        {userAccount?.pauseUserNote && (
          <div className="bg-amber-500/10 border border-amber-500/20 rounded-xl px-4 py-3 w-full">
            <p className="text-amber-300 text-sm">{userAccount.pauseUserNote}</p>
          </div>
        )}

        {contactDetails?.whatsappNumber && (
          <Button
            onClick={() => openWhatsApp(contactDetails.whatsappNumber, 'Hi! My account has been paused. Can you help?')}
            className="w-full"
          >
            <MessageCircle className="w-4 h-4" />
            Contact Support
          </Button>
        )}
      </div>
    </div>
  )
}