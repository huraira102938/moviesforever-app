import { Link } from 'react-router-dom'
import { Play } from 'lucide-react'
import type { Movie } from '../../types'
import { Badge } from '../ui/Badge'
import { cn } from '../../lib/utils'

interface MoviePosterProps {
  movie: Movie
  className?: string
}

export default function MoviePoster({ movie, className }: MoviePosterProps) {
  return (
    <Link
      to={`/movie/${movie.id}`}
      className={cn(
        'group relative block rounded-2xl overflow-hidden border border-white/5 bg-white/5',
        'aspect-[2/3] transition-all duration-300 hover:-translate-y-1 hover:border-brand-500/30 hover:shadow-xl hover:shadow-brand-500/10',
        className,
      )}
    >
      {movie.thumbnailUrl ? (
        <img
          src={movie.thumbnailUrl}
          alt={movie.title}
          loading="lazy"
          className="absolute inset-0 w-full h-full object-cover"
        />
      ) : (
        <div className="absolute inset-0 flex items-center justify-center text-gray-600 text-sm">
          No Image
        </div>
      )}

      <div className="absolute inset-0 bg-gradient-to-t from-black/85 via-transparent to-transparent opacity-0 group-hover:opacity-100 transition-opacity duration-300" />

      <div className="absolute inset-0 flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity duration-300">
        <div className="w-12 h-12 rounded-full bg-brand-500/90 flex items-center justify-center shadow-lg">
          <Play className="w-5 h-5 text-white fill-white ml-0.5" />
        </div>
      </div>

      <div className="absolute bottom-0 left-0 right-0 p-3">
        <div className="flex items-center gap-1.5 mb-1">
          {movie.badge && <Badge>{movie.badge}</Badge>}
          {movie.isFree && <Badge variant="success">FREE</Badge>}
        </div>
        <h3 className="text-white text-sm font-semibold leading-tight line-clamp-2">{movie.title}</h3>
        {movie.year && <p className="text-gray-400 text-xs mt-0.5">{movie.year}</p>}
      </div>
    </Link>
  )
}
