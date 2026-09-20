import { useRef } from 'react'
import { ChevronLeft, ChevronRight } from 'lucide-react'
import type { Movie } from '../../types'
import MoviePoster from './MoviePoster'
import { cn } from '../../lib/utils'

interface SectionRowProps {
  title: string
  movies: Movie[]
  className?: string
}

export default function SectionRow({ title, movies, className }: SectionRowProps) {
  const scrollRef = useRef<HTMLDivElement>(null)

  if (movies.length === 0) return null

  const scroll = (dir: 'left' | 'right') => {
    if (!scrollRef.current) return
    const amount = dir === 'left' ? -300 : 300
    scrollRef.current.scrollBy({ left: amount, behavior: 'smooth' })
  }

  return (
    <div className={cn('relative', className)}>
      <div className="flex items-center justify-between mb-4">
        <div className="flex items-center gap-3">
          <div className="w-1 h-5 rounded-full bg-brand-500" />
          <h2 className="text-lg font-bold text-white">{title}</h2>
          <span className="text-xs text-gray-500 font-medium">{movies.length} movies</span>
        </div>
        <div className="flex gap-1.5">
          <button
            onClick={() => scroll('left')}
            className="p-1.5 rounded-lg border border-white/10 bg-white/5 text-gray-400 hover:text-white hover:bg-white/10 transition"
          >
            <ChevronLeft className="w-4 h-4" />
          </button>
          <button
            onClick={() => scroll('right')}
            className="p-1.5 rounded-lg border border-white/10 bg-white/5 text-gray-400 hover:text-white hover:bg-white/10 transition"
          >
            <ChevronRight className="w-4 h-4" />
          </button>
        </div>
      </div>

      <div
        ref={scrollRef}
        className="flex gap-3 overflow-x-auto no-scrollbar scroll-smooth"
      >
        {movies.map((movie) => (
          <MoviePoster key={movie.id} movie={movie} className="w-[160px] shrink-0" />
        ))}
      </div>
    </div>
  )
}
