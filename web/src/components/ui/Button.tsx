import { cn } from '../../lib/utils'
import { Loader2 } from 'lucide-react'

interface ButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: 'primary' | 'outline' | 'ghost'
  loading?: boolean
}

export function Button({
  variant = 'primary',
  loading = false,
  className,
  children,
  disabled,
  ...props
}: ButtonProps) {
  return (
    <button
      className={cn(
        'inline-flex items-center justify-center gap-2 px-6 py-3 rounded-2xl text-sm font-semibold transition-all duration-200',
        variant === 'primary' &&
          'bg-brand-500 text-white hover:bg-brand-600 hover:-translate-y-0.5 active:translate-y-0 shadow-lg shadow-brand-500/20',
        variant === 'outline' &&
          'border border-brand-500/40 text-brand-400 hover:bg-brand-500/10 hover:border-brand-400',
        variant === 'ghost' && 'text-gray-400 hover:text-white hover:bg-white/5',
        (disabled || loading) && 'opacity-50 pointer-events-none',
        className,
      )}
      disabled={disabled || loading}
      {...props}
    >
      {loading && <Loader2 className="w-4 h-4 animate-spin" />}
      {children}
    </button>
  )
}
