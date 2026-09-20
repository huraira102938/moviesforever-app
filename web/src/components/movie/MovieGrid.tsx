import type { Movie } from '../../types'
import MoviePoster from './MoviePoster'
import { cn } from '../../lib/utils'

interface MovieGridProps {
  movies: Movie[]
  className?: string
}

export default function MovieGrid({ movies, className }: MovieGridProps) {
  if (movies.length === 0) {
    return (
      <div className="flex flex-col items-center justify-center py-16 text-gray-500">
        <p className="text-sm">No movies found</p>
      </div>
    )
  }

  return (
    <div
      className={cn(
        'grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 gap-4',
        className,
      )}
    >
      {movies.map((movie) => (
        <MoviePoster key={movie.id} movie={movie} />
      ))}
    </div>
  )
}
