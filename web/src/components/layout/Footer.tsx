import { Film } from 'lucide-react'
import { useApp } from '../../context/AppContext'
import { openWhatsApp } from '../../lib/utils'

export default function Footer() {
  const { contactDetails } = useApp()

  return (
    <footer className="border-t border-white/5 bg-[#0b0b18]">
      <div className="mx-auto max-w-7xl px-5 py-10">
        <div className="flex flex-col md:flex-row justify-between gap-8">
          <div className="flex flex-col gap-3">
            <div className="flex items-center gap-2.5 text-white">
              <Film className="w-6 h-6 text-brand-400" />
              <span className="text-lg font-bold tracking-tight">
                Movies<span className="text-brand-400">Forever</span>
              </span>
            </div>
            <p className="text-gray-500 text-sm max-w-xs">
              Watch movies anytime, anywhere. Stream in HD quality with new content added weekly.
            </p>
          </div>

          <div className="flex flex-col gap-3">
            <h4 className="text-white font-semibold text-sm">Get Help</h4>
            {contactDetails?.whatsappNumber && (
              <button
                onClick={() => openWhatsApp(contactDetails.whatsappNumber, 'Hi! I need help with MoviesForever.')}
                className="text-gray-400 text-sm hover:text-brand-400 transition text-left"
              >
                WhatsApp Support
              </button>
            )}
            {contactDetails?.groupLink && (
              <a
                href={contactDetails.groupLink}
                target="_blank"
                rel="noopener noreferrer"
                className="text-gray-400 text-sm hover:text-brand-400 transition"
              >
                Community Group
              </a>
            )}
          </div>
        </div>

        <div className="mt-8 pt-6 border-t border-white/5 text-center">
          <p className="text-gray-600 text-xs">
            &copy; {new Date().getFullYear()} MoviesForever. All rights reserved.
          </p>
        </div>
      </div>
    </footer>
  )
}
