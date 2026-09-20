import { useParams, useNavigate } from 'react-router-dom'
import { useState, useMemo } from 'react'
import {
  ArrowLeft,
  Play,
  Download,
  Star,
  Calendar,
  Globe,
  Lock,
} from 'lucide-react'
import { useApp } from '../context/AppContext'
import { Button } from '../components/ui/Button'
import { Badge } from '../components/ui/Badge'
import { Card } from '../components/ui/Card'
import BackButton from '../components/common/BackButton'
import GlowOrb from '../components/ui/GlowOrb'
import { Spinner } from '../components/ui/Spinner'

export default function MovieDetailScreen() {
  const { movieId } = useParams<{ movieId: string }>()
  const navigate = useNavigate()
  const { movies, genres, isUnlocked, pricing } = useApp()

  const movie = movies.find((m) => m.id === movieId)

  const genreNames = useMemo(() => {
    if (!movie) return []
    const genreMap = new Map(genres.map((g) => [g.id, g.name]))
    return movie.genres.map((gId) => genreMap.get(gId) ?? gId)
  }, [movie, genres])

  if (!movie) {
    return (
      <div className="min-h-screen flex flex-col items-center justify-center gap-4">
        <Spinner />
        <p className="text-gray-400 text-sm">Loading movie...</p>
      </div>
    )
  }

  const canPlay = isUnlocked || movie.isFree

  return (
    <div className="min-h-screen">
      <div className="relative w-full aspect-[2/1] sm:aspect-[3/1] overflow-hidden">
        <img
          src={movie.thumbnailUrl || ''}
          alt={movie.title}
          className="w-full h-full object-cover"
        />
        <div className="absolute inset-0 bg-gradient-to-t from-[#0b0b18] via-transparent to-[#0b0b18]/40" />

        <div className="absolute top-5 left-5 z-20">
          <BackButton />
        </div>

        {movie.badge && (
          <div className="absolute top-5 right-5 z-20">
            <Badge>{movie.badge}</Badge>
          </div>
        )}
      </div>

      <div className="max-w-3xl mx-auto px-5 -mt-16 relative z-10 flex flex-col gap-6 pb-12">
        <div>
          <h1 className="text-2xl sm:text-3xl font-black text-white tracking-tight mb-3">
            {movie.title}
          </h1>

          <div className="flex flex-wrap items-center gap-3 text-sm">
            {movie.imdbRating && (
              <div className="flex items-center gap-1 px-2.5 py-1 rounded-full border border-amber-500/30 bg-amber-500/10">
                <Star className="w-3.5 h-3.5 text-amber-400 fill-amber-400" />
                <span className="text-amber-400 font-semibold">{movie.imdbRating}</span>
              </div>
            )}
            {movie.year && (
              <div className="flex items-center gap-1.5 text-gray-400">
                <Calendar className="w-3.5 h-3.5" />
                {movie.year}
              </div>
            )}
            {movie.language && (
              <div className="flex items-center gap-1.5 text-gray-400">
                <Globe className="w-3.5 h-3.5" />
                {movie.language}
              </div>
            )}
            {movie.isFree && <Badge variant="success">FREE</Badge>}
          </div>
        </div>

        {genreNames.length > 0 && (
          <div className="flex flex-wrap gap-2">
            {genreNames.map((name) => (
              <span
                key={name}
                className="px-3 py-1 rounded-full text-xs font-medium bg-white/5 border border-white/10 text-gray-300"
              >
                {name}
              </span>
            ))}
          </div>
        )}

        <div className="flex flex-wrap gap-3">
          {canPlay ? (
            <>
              <Button onClick={() => navigate(`/player/${movie.id}`)}>
                <Play className="w-4 h-4" />
                Watch Now
              </Button>
              {movie.trailerUrl && (
                <Button
                  variant="outline"
                  onClick={() => navigate(`/player/${movie.id}?trailer=true`)}
                >
                  <Play className="w-4 h-4" />
                  Watch Trailer
                </Button>
              )}
            </>
          ) : (
            <>
              {movie.trailerUrl && (
                <Button
                  variant="outline"
                  onClick={() => navigate(`/player/${movie.id}?trailer=true`)}
                >
                  <Play className="w-4 h-4" />
                  Watch Trailer
                </Button>
              )}
            </>
          )}
        </div>

        {!canPlay && (
          <Card className="border-brand-500/30">
            <div className="flex items-center gap-2 mb-3">
              <Lock className="w-5 h-5 text-brand-400" />
              <h3 className="text-white font-semibold">Lifetime Access</h3>
            </div>
            <div className="flex items-baseline gap-1 mb-2">
              <span className="text-2xl font-black text-brand-400">
                PKR {pricing?.standardPrice?.toLocaleString() ?? '—'}
              </span>
              <span className="text-xs text-gray-500">one-time</span>
            </div>
            <p className="text-gray-400 text-sm mb-4">
              Unlock full access to stream and download this movie.
            </p>
            <Button onClick={() => navigate('/lock')} className="w-full">
              <Lock className="w-4 h-4" />
              Unlock Lifetime Access
            </Button>
          </Card>
        )}

        {movie.description && (
          <div>
            <h3 className="text-white font-semibold text-lg mb-2">Overview</h3>
            <p className="text-gray-400 text-sm leading-relaxed">{movie.description}</p>
          </div>
        )}

        {movie.availableDubs?.length > 0 && (
          <div>
            <h3 className="text-white font-semibold text-sm mb-1.5">Available in</h3>
            <p className="text-gray-400 text-sm">{movie.availableDubs.join(', ')}</p>
          </div>
        )}
      </div>
    </div>
  )
}