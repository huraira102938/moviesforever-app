import { useParams, useNavigate } from 'react-router-dom'
import { ArrowLeft } from 'lucide-react'
import { useApp } from '../context/AppContext'
import MovieGrid from '../components/movie/MovieGrid'
import { useMemo } from 'react'
import { Spinner } from '../components/ui/Spinner'

export default function CategoryBrowseScreen() {
  const { categoryId } = useParams<{ categoryId: string }>()
  const navigate = useNavigate()
  const { categories, movies } = useApp()

  const category = categories.find((c) => c.id === categoryId)

  const categoryMovies = useMemo(
    () => movies.filter((m) => !m.paused && m.category === categoryId),
    [movies, categoryId],
  )

  if (!category) {
    return (
      <div className="min-h-screen flex flex-col items-center justify-center gap-4">
        <Spinner />
        <p className="text-gray-400 text-sm">Loading category...</p>
      </div>
    )
  }

  return (
    <div className="min-h-screen">
      <div className="max-w-7xl mx-auto px-5 py-8 flex flex-col gap-6">
        <div className="flex items-center gap-3">
          <button
            onClick={() => navigate(-1)}
            className="w-10 h-10 rounded-full bg-white/10 border border-white/10 flex items-center justify-center text-white hover:bg-white/15 transition"
          >
            <ArrowLeft className="w-5 h-5" />
          </button>
          <div>
            <h1 className="text-2xl font-bold text-white">{category.name}</h1>
            <p className="text-xs text-gray-500">{categoryMovies.length} movies</p>
          </div>
        </div>

        <MovieGrid movies={categoryMovies} />
      </div>
    </div>
  )
}