import { cn } from '../../lib/utils'

interface InputProps extends React.InputHTMLAttributes<HTMLInputElement> {
  icon?: React.ReactNode
  onClear?: () => void
}

export function Input({ className, icon, onClear, ...props }: InputProps) {
  return (
    <div className="relative">
      {icon && (
        <div className="absolute left-4 top-1/2 -translate-y-1/2 text-gray-500 pointer-events-none">
          {icon}
        </div>
      )}
      <input
        className={cn(
          'w-full bg-white/5 border border-white/10 rounded-xl px-4 py-3 text-sm text-white placeholder-gray-500 outline-none transition-all',
          'focus:border-brand-500/50 focus:ring-1 focus:ring-brand-500/25',
          icon && 'pl-11',
          props.type !== 'file' && onClear && 'pr-10',
          className,
        )}
        {...props}
      />
      {onClear && props.value && (
        <button
          type="button"
          onClick={onClear}
          className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-500 hover:text-white transition text-xs"
        >
          Clear
        </button>
      )}
    </div>
  )
}
