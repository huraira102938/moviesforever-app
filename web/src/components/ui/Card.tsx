import { cn } from '../../lib/utils'

interface CardProps {
  children: React.ReactNode
  className?: string
  hover?: boolean
}

export function Card({ children, className, hover = false }: CardProps) {
  return (
    <div
      className={cn(
        'border border-white/10 bg-white/5 backdrop-blur-sm rounded-2xl p-5',
        hover && 'transition-all duration-200 hover:bg-white/[0.07] hover:border-white/15',
        className,
      )}
    >
      {children}
    </div>
  )
}
