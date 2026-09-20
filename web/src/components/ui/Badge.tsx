import { cn } from '../../lib/utils'

interface BadgeProps {
  children: React.ReactNode
  variant?: 'default' | 'success' | 'warning' | 'info'
  className?: string
}

export function Badge({ children, variant = 'default', className }: BadgeProps) {
  return (
    <span
      className={cn(
        'inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-semibold',
        variant === 'default' && 'bg-brand-500/90 text-white',
        variant === 'success' && 'bg-emerald-500/90 text-white',
        variant === 'warning' && 'bg-amber-500/90 text-black',
        variant === 'info' && 'bg-sky-500/90 text-white',
        className,
      )}
    >
      {children}
    </span>
  )
}
