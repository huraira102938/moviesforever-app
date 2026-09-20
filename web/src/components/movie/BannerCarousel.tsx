import { useState, useEffect, useCallback } from 'react'
import { ChevronLeft, ChevronRight } from 'lucide-react'
import type { Banner } from '../../types'
import { cn } from '../../lib/utils'
import { useApp } from '../../context/AppContext'

interface BannerCarouselProps {
  banners: Banner[]
  className?: string
}

export default function BannerCarousel({ banners, className }: BannerCarouselProps) {
  const [current, setCurrent] = useState(0)
  const [paused, setPaused] = useState(false)
  const { movies } = useApp()

  const sorted = [...banners].sort((a, b) => a.order - b.order)

  const next = useCallback(() => {
    if (sorted.length === 0) return
    setCurrent((c) => (c + 1) % sorted.length)
  }, [sorted.length])

  const prev = useCallback(() => {
    if (sorted.length === 0) return
    setCurrent((c) => (c - 1 + sorted.length) % sorted.length)
  }, [sorted.length])

  useEffect(() => {
    if (paused || sorted.length <= 1) return
    const id = setInterval(next, 5000)
    return () => clearInterval(id)
  }, [paused, next, sorted.length])

  if (sorted.length === 0) return null

  const getBannerUrl = (banner: Banner): string => {
    return banner.imageUrl
  }

  return (
    <div
      className={cn('relative rounded-2xl overflow-hidden', className)}
      onMouseEnter={() => setPaused(true)}
      onMouseLeave={() => setPaused(false)}
    >
      <div className="relative aspect-[16/7] sm:aspect-[16/6]">
        {sorted.map((banner, i) => {
          const linkedMovie = banner.clickable && banner.linkedMovieId
            ? movies.find((m) => m.id === banner.linkedMovieId)
            : null

          const Wrapper = linkedMovie ? 'a' : 'div'
          const wrapperProps = linkedMovie
            ? { href: `/movie/${linkedMovie.id}` }
            : {}

          return (
            <Wrapper
              key={banner.id}
              {...wrapperProps}
              className={cn(
                'absolute inset-0 transition-opacity duration-700',
                i === current ? 'opacity-100 z-10' : 'opacity-0 z-0',
              )}
            >
              <img
                src={getBannerUrl(banner)}
                alt=""
                className="w-full h-full object-cover"
              />
              <div className="absolute inset-0 bg-gradient-to-t from-black/70 via-transparent to-transparent" />
              <div className="absolute bottom-4 left-5">
                <span className="inline-block px-3 py-1 bg-brand-500/80 backdrop-blur-sm text-white text-xs font-semibold rounded-full">
                  Featured
                </span>
              </div>
            </Wrapper>
          )
        })}
      </div>

      {sorted.length > 1 && (
        <>
          <button
            onClick={prev}
            className="absolute left-3 top-1/2 -translate-y-1/2 z-20 w-9 h-9 rounded-full bg-black/40 backdrop-blur-sm border border-white/10 text-white flex items-center justify-center opacity-0 hover:opacity-100 group-hover:opacity-100 transition-opacity"
          >
            <ChevronLeft className="w-4 h-4" />
          </button>
          <button
            onClick={next}
            className="absolute right-3 top-1/2 -translate-y-1/2 z-20 w-9 h-9 rounded-full bg-black/40 backdrop-blur-sm border border-white/10 text-white flex items-center justify-center opacity-0 hover:opacity-100 group-hover:opacity-100 transition-opacity"
          >
            <ChevronRight className="w-4 h-4" />
          </button>

          <div className="absolute bottom-3 left-1/2 -translate-x-1/2 z-20 flex gap-1.5">
            {sorted.map((_, i) => (
              <button
                key={i}
                onClick={() => setCurrent(i)}
                className={cn(
                  'w-2 h-2 rounded-full transition-all duration-300',
                  i === current ? 'bg-brand-400 w-5' : 'bg-white/40 hover:bg-white/60',
                )}
              />
            ))}
          </div>
        </>
      )}
    </div>
  )
}
