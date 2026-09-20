import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import {
  ArrowLeft,
  Flame,
  CheckCircle,
  Copy,
  LockOpen,
  ShieldCheck,
  MessageCircle,
} from 'lucide-react'
import { useApp } from '../context/AppContext'
import { Button } from '../components/ui/Button'
import { Card } from '../components/ui/Card'
import { copyToClipboard, openWhatsApp } from '../lib/utils'

const BENEFITS = [
  'Full access to all current & future movies/shows',
  'Fast streaming servers with zero ads',
  'HD & 4K video quality',
  'No monthly renewal or hidden fees, ever',
]

function StepCard({ stepNumber, title, description }: { stepNumber: string; title: string; description: string }) {
  return (
    <div className="flex gap-4 p-3.5 rounded-2xl border border-white/10 bg-white/5">
      <div className="w-7 h-7 shrink-0 rounded-full bg-brand-500 flex items-center justify-center">
        <span className="text-black text-sm font-bold">{stepNumber}</span>
      </div>
      <div>
        <p className="text-white text-sm font-bold">{title}</p>
        <p className="text-gray-400 text-xs leading-relaxed mt-0.5">{description}</p>
      </div>
    </div>
  )
}

export default function PaymentInstructions() {
  const navigate = useNavigate()
  const { pricing, paymentDetails, contactDetails } = useApp()
  const [copiedField, setCopiedField] = useState<string | null>(null)

  const handleCopy = async (label: string, value: string) => {
    const ok = await copyToClipboard(value)
    if (ok) {
      setCopiedField(label)
      setTimeout(() => setCopiedField(null), 1500)
    }
  }

  const handleSendScreenshot = () => {
    if (!contactDetails?.whatsappNumber) return
    const message = 'Hi! I have made the payment for MoviesForever Lifetime Access.'
    openWhatsApp(contactDetails.whatsappNumber, message)
  }

  return (
    <div className="min-h-screen">
      <div className="max-w-2xl mx-auto px-5 py-8">
        <header className="flex items-center gap-3 mb-6">
          <button
            onClick={() => navigate(-1)}
            className="w-10 h-10 rounded-full bg-white/10 border border-white/10 flex items-center justify-center text-white hover:bg-white/15 transition"
          >
            <ArrowLeft className="w-5 h-5" />
          </button>
          <h1 className="text-lg font-bold text-white">Lifetime Pass Checkout</h1>
        </header>

        <div className="flex flex-col gap-5">
          <Card className="border-brand-500/30">
            <div className="flex items-center gap-3">
              <div className="w-11 h-11 shrink-0 rounded-full bg-red-500/15 flex items-center justify-center">
                <Flame className="w-6 h-6 text-red-400" />
              </div>
              <div>
                <p className="text-red-400 text-[11px] font-bold tracking-wide">LIMITED OFFER</p>
                <p className="text-brand-300 text-lg font-bold">
                  {pricing?.standardPrice ? `PKR ${pricing.standardPrice.toLocaleString()} • One-Time Payment` : 'One-Time Payment'}
                </p>
              </div>
            </div>
          </Card>

          <div>
            <h2 className="text-white font-bold text-base mb-3">What You Get</h2>
            <div className="flex flex-col gap-2">
              {BENEFITS.map((benefit) => (
                <div key={benefit} className="flex items-center gap-3">
                  <CheckCircle className="w-4 h-4 text-brand-400 shrink-0" />
                  <span className="text-gray-300 text-sm">{benefit}</span>
                </div>
              ))}
            </div>
          </div>

          <div>
            <h2 className="text-white font-bold text-base mb-3">3 Easy Steps to Unlock</h2>
            <div className="flex flex-col gap-2.5">
              <StepCard
                stepNumber="1"
                title="Send Payment"
                description={`Transfer PKR ${pricing?.standardPrice?.toLocaleString() ?? '—'} to the bank account details provided below.`}
              />
              <StepCard
                stepNumber="2"
                title="Share Screenshot"
                description={
                  contactDetails?.whatsappNumber
                    ? `Click 'Send Screenshot on WhatsApp' below and attach your payment receipt to +${contactDetails.whatsappNumber}.`
                    : "Click 'Send Screenshot on WhatsApp' below and attach your payment receipt."
                }
              />
              <StepCard
                stepNumber="3"
                title="Get Account Credentials"
                description="Once verified, our team will send your unique Code ID & Username to unlock your account forever."
              />
            </div>
          </div>

          <div>
            <h2 className="text-white font-bold text-base mb-3">Payment Transfer Details</h2>
            <Card className="p-4">
              <div className="flex flex-col gap-2.5">
                {[
                  { label: 'Bank Name', value: paymentDetails?.bankName || '—' },
                  { label: 'Account Title', value: paymentDetails?.accountTitle || '—' },
                  { label: 'Account Number', value: paymentDetails?.accountNumber || '—' },
                ].map(({ label, value }) => (
                  <div
                    key={label}
                    className="flex items-center justify-between gap-3 rounded-xl bg-white/5 border border-white/10 px-4 py-2.5"
                  >
                    <div className="min-w-0">
                      <p className="text-gray-500 text-xs">{label}</p>
                      <p className="text-white text-sm font-semibold truncate">{value}</p>
                    </div>
                    <button
                      onClick={() => handleCopy(label, value)}
                      className="p-1.5 rounded-lg text-brand-400 hover:bg-brand-500/10 transition"
                      title={`Copy ${label}`}
                    >
                      {copiedField === label ? (
                        <CheckCircle className="w-4 h-4 text-emerald-400" />
                      ) : (
                        <Copy className="w-4 h-4" />
                      )}
                    </button>
                  </div>
                ))}
              </div>
            </Card>
          </div>

          <Button onClick={handleSendScreenshot} className="w-full">
            <MessageCircle className="w-4 h-4" />
            Send Screenshot on WhatsApp
          </Button>

          <div className="flex items-center justify-center gap-2 text-gray-500">
            <LockOpen className="w-3.5 h-3.5" />
            <span className="text-xs text-center">
              Verification typically takes less than 20 minutes
            </span>
          </div>

          <div className="flex items-center justify-center gap-2 text-gray-500">
            <ShieldCheck className="w-3.5 h-3.5" />
            <span className="text-xs text-center">
              Already have a code? Sign in from the previous page.
            </span>
          </div>
        </div>
      </div>
    </div>
  )
}