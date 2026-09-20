import { useState } from 'react'
import { Search } from 'lucide-react'
import { useApp } from '../context/AppContext'
import { Input } from '../components/ui/Input'
import { useMovieSearch } from '../hooks/useSearch'
import MovieGrid from '../components/movie/MovieGrid'
import { cn } from '../lib/utils'

export default function SearchScreen() {
  const { movies, categories, genres } = useApp()

  const [query, setQuery] = useState('')
  const [selectedCategory, setSelectedCategory] = useState('all')
  const [selectedGenres, setSelectedGenres] = useState<Set<string>>(new Set())
  const [freeOnly, setFreeOnly] = useState(false)

  const results = useMovieSearch({
    movies,
    query,
    selectedCategory,
    selectedGenres,
    freeOnly,
  })

  const toggleGenre = (genreId: string) => {
    setSelectedGenres((prev) => {
      const next = new Set(prev)
      if (next.has(genreId)) next.delete(genreId)
      else next.add(genreId)
      return next
    })
  }

  return (
    <div className="min-h-screen">
      <div className="max-w-7xl mx-auto px-5 py-8 flex flex-col gap-5">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-3">
            <div className="w-1 h-6 rounded-full bg-brand-500" />
            <h1 className="text-2xl font-bold text-white">Search</h1>
          </div>

          <label className="flex items-center gap-2 text-sm text-gray-400 cursor-pointer">
            <span className="font-medium">Free only</span>
            <button
              role="switch"
              aria-checked={freeOnly}
              onClick={() => setFreeOnly(!freeOnly)}
              className={cn(
                'w-10 h-6 rounded-full relative transition-colors',
                freeOnly ? 'bg-brand-500' : 'bg-white/10',
              )}
            >
              <span
                className={cn(
                  'absolute top-0.5 w-5 h-5 rounded-full bg-white transition-all',
                  freeOnly ? 'left-[18px]' : 'left-0.5',
                )}
              />
            </button>
          </label>
        </div>

        <Input
          icon={<Search className="w-4 h-4" />}
          placeholder="Search movies, genres, languages..."
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          onClear={() => setQuery('')}
        />

        <div className="flex flex-col gap-3">
          <div className="flex gap-2 overflow-x-auto no-scrollbar">
            <button
              onClick={() => setSelectedCategory('all')}
              className={cn(
                'px-4 py-2 rounded-full text-sm font-medium whitespace-nowrap transition-all',
                selectedCategory === 'all'
                  ? 'bg-brand-500 text-white'
                  : 'bg-white/5 border border-white/10 text-gray-400 hover:text-white',
              )}
            >
              All
            </button>
            {categories.map((cat) => (
              <button
                key={cat.id}
                onClick={() => setSelectedCategory(cat.id)}
                className={cn(
                  'px-4 py-2 rounded-full text-sm font-medium whitespace-nowrap transition-all',
                  selectedCategory === cat.id
                    ? 'bg-brand-500 text-white'
                    : 'bg-white/5 border border-white/10 text-gray-400 hover:text-white',
                )}
              >
                {cat.name}
              </button>
            ))}
          </div>

          <div className="flex gap-2 overflow-x-auto no-scrollbar">
            {genres.map((genre) => (
              <button
                key={genre.id}
                onClick={() => toggleGenre(genre.id)}
                className={cn(
                  'px-4 py-2 rounded-full text-sm font-medium whitespace-nowrap transition-all border',
                  selectedGenres.has(genre.id)
                    ? 'bg-brand-500/20 text-brand-400 border-brand-500/40'
                    : 'bg-white/5 border-white/10 text-gray-400 hover:text-white',
                )}
              >
                {genre.name}
              </button>
            ))}
          </div>
        </div>

        <div>
          <p className="text-xs text-gray-500 mb-4">{results.length} movies found</p>
          <MovieGrid movies={results} />
        </div>
      </div>
    </div>
  )
}