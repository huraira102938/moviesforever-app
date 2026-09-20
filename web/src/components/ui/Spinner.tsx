import { Loader2 } from 'lucide-react'
import { cn } from '../../lib/utils'

interface SpinnerProps {
  size?: 'sm' | 'md' | 'lg'
  className?: string
}

export function Spinner({ size = 'md', className }: SpinnerProps) {
  return (
    <Loader2
      className={cn(
        'animate-spin text-brand-400',
        size === 'sm' && 'w-4 h-4',
        size === 'md' && 'w-6 h-6',
        size === 'lg' && 'w-10 h-10',
        className,
      )}
    />
  )
}
