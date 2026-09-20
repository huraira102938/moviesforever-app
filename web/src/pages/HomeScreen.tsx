import { Link } from 'react-router-dom'
import { Lock, Flame, MessageCircle } from 'lucide-react'
import { useApp } from '../context/AppContext'
import BannerCarousel from '../components/movie/BannerCarousel'
import SectionRow from '../components/movie/SectionRow'
import MovieGrid from '../components/movie/MovieGrid'
import { Button } from '../components/ui/Button'
import { Card } from '../components/ui/Card'
import { SectionLabels, SectionOrder } from '../types'
import { useMemo } from 'react'

export default function HomeScreen() {
  const {
    movies,
    banners,
    trendingMovies,
    pricing,
    isUnlocked,
  } = useApp()

  const activeMovies = useMemo(() => movies.filter((m) => !m.paused), [movies])

  const trendingMoviesList = useMemo(() => {
    const movieMap = new Map(activeMovies.map((m) => [m.id, m]))
    return trendingMovies
      .map((t) => movieMap.get(t.movieId))
      .filter((m): m is (typeof activeMovies)[number] => m !== undefined)
  }, [trendingMovies, activeMovies])

  const freeMovies = useMemo(
    () => activeMovies.filter((m) => m.isFree),
    [activeMovies],
  )

  const sectionsByLabel = useMemo(() => {
    return SectionOrder.map((section) => {
      let sectionMovies = activeMovies.filter((m) => m.sections?.includes(section))
      if (section === 'all-time-hit') {
        sectionMovies = [...sectionMovies].sort((a, b) => (b.year ?? 0) - (a.year ?? 0))
      }
      return {
        key: section,
        label: SectionLabels[section],
        movies: sectionMovies,
      }
    }).filter((s) => s.movies.length > 0)
  }, [activeMovies])

  return (
    <div className="min-h-screen">
      <div className="max-w-7xl mx-auto px-5 py-8 flex flex-col gap-10">
        <BannerCarousel banners={banners} />

        {!isUnlocked && pricing?.note && (
          <Link to="/lock">
            <Card className="border-brand-500/30 hover:bg-brand-500/[0.08] cursor-pointer transition-all">
              <div className="flex items-center justify-between gap-4">
                <div className="flex items-center gap-3">
                  <div className="w-10 h-10 rounded-full bg-brand-500/15 flex items-center justify-center shrink-0">
                    <Lock className="w-5 h-5 text-brand-400" />
                  </div>
                  <div>
                    <p className="text-white font-semibold text-sm">Limited Time Offer</p>
                    <p className="text-brand-300 text-xs mt-0.5">{pricing.note}</p>
                  </div>
                </div>
                <Button className="shrink-0">
                  Unlock for PKR {pricing.standardPrice?.toLocaleString() ?? '—'}
                </Button>
              </div>
            </Card>
          </Link>
        )}

        {sectionsByLabel.map((section) => (
          <SectionRow
            key={section.key}
            title={section.label}
            movies={section.movies}
          />
        ))}

        {freeMovies.length > 0 && (
          <SectionRow title="Free to Watch" movies={freeMovies} />
        )}

        {trendingMoviesList.length > 0 && (
          <div>
            <div className="flex items-center gap-3 mb-5">
              <Flame className="w-5 h-5 text-brand-400" />
              <h2 className="text-lg font-bold text-white">Trending Now</h2>
            </div>
            <MovieGrid
              movies={trendingMoviesList}
              className="grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 gap-4"
            />
          </div>
        )}

        {!isUnlocked && (
          <Card className="text-center">
            <h3 className="text-white font-bold text-lg mb-2">Want the full experience?</h3>
            <p className="text-gray-400 text-sm mb-5 max-w-md mx-auto">
              Unlock lifetime access to every movie in HD quality.
            </p>
            <Link to={`/lock`}>
              <Button>
                <MessageCircle className="w-4 h-4" />
                Get Unlocked Now
              </Button>
            </Link>
          </Card>
        )}
      </div>
    </div>
  )
}