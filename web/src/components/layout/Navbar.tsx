import { useState, useEffect } from 'react'
import { Link, useLocation } from 'react-router-dom'
import { Film, Menu, X, Search, User, Lock } from 'lucide-react'
import { useApp } from '../../context/AppContext'
import { cn } from '../../lib/utils'

const NAV_LINKS = [
  { to: '/', label: 'Home' },
  { to: '/search', label: 'Search' },
  { to: '/profile', label: 'Profile' },
]

export default function Navbar() {
  const [scrolled, setScrolled] = useState(false)
  const [mobileOpen, setMobileOpen] = useState(false)
  const location = useLocation()
  const { isUnlocked } = useApp()

  useEffect(() => {
    const onScroll = () => setScrolled(window.scrollY > 24)
    window.addEventListener('scroll', onScroll, { passive: true })
    return () => window.removeEventListener('scroll', onScroll)
  }, [])

  useEffect(() => setMobileOpen(false), [location.pathname])

  return (
    <header
      className={cn(
        'fixed top-0 left-0 right-0 z-50 transition-all duration-300',
        scrolled ? 'bg-[#0b0b18]/85 backdrop-blur-xl border-b border-white/5' : 'bg-transparent',
      )}
    >
      <nav className="mx-auto max-w-7xl flex items-center justify-between px-5 h-16">
        <Link to="/" className="flex items-center gap-2.5 text-white">
          <Film className="w-7 h-7 text-brand-400" />
          <span className="text-lg font-bold tracking-tight">
            Movies<span className="text-brand-400">Forever</span>
          </span>
        </Link>

        <div className="hidden md:flex items-center gap-1">
          {NAV_LINKS.map((link) => (
            <Link
              key={link.to}
              to={link.to}
              className={cn(
                'px-4 py-2 rounded-xl text-sm font-medium transition-all',
                location.pathname === link.to
                  ? 'bg-brand-500/15 text-brand-400'
                  : 'text-gray-400 hover:text-white hover:bg-white/5',
              )}
            >
              {link.label}
            </Link>
          ))}
        </div>

        <div className="hidden md:flex items-center gap-3">
          {isUnlocked ? (
            <Link
              to="/profile"
              className="w-9 h-9 rounded-full bg-brand-500/15 border border-brand-500/30 flex items-center justify-center text-brand-400 transition hover:bg-brand-500/25"
            >
              <User className="w-4 h-4" />
            </Link>
          ) : (
            <Link
              to="/lock"
              className="flex items-center gap-2 px-4 py-2 bg-brand-500 text-white rounded-xl text-sm font-semibold hover:bg-brand-600 transition-all"
            >
              <Lock className="w-3.5 h-3.5" />
              Sign In
            </Link>
          )}
        </div>

        <button
          className="md:hidden p-2 text-gray-400 hover:text-white transition"
          onClick={() => setMobileOpen(!mobileOpen)}
        >
          {mobileOpen ? <X className="w-6 h-6" /> : <Menu className="w-6 h-6" />}
        </button>
      </nav>

      {mobileOpen && (
        <div className="md:hidden bg-[#0b0b18]/95 backdrop-blur-xl border-t border-white/5 px-5 pb-5 pt-2">
          <div className="flex flex-col gap-1">
            {NAV_LINKS.map((link) => (
              <Link
                key={link.to}
                to={link.to}
                className={cn(
                  'px-4 py-3 rounded-xl text-sm font-medium transition-all',
                  location.pathname === link.to
                    ? 'bg-brand-500/15 text-brand-400'
                    : 'text-gray-400 hover:text-white hover:bg-white/5',
                )}
              >
                {link.label}
              </Link>
            ))}
            {!isUnlocked && (
              <Link
                to="/lock"
                className="mt-2 flex items-center justify-center gap-2 px-4 py-3 bg-brand-500 text-white rounded-xl text-sm font-semibold hover:bg-brand-600 transition-all"
              >
                <Lock className="w-3.5 h-3.5" />
                Sign In
              </Link>
            )}
          </div>
        </div>
      )}
    </header>
  )
}
