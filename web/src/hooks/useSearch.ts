import { useMemo } from 'react'
import type { Movie } from '../types'

interface UseSearchParams {
  movies: Movie[]
  query: string
  selectedCategory: string
  selectedGenres: Set<string>
  freeOnly: boolean
}

export function useMovieSearch({
  movies,
  query,
  selectedCategory,
  selectedGenres,
  freeOnly,
}: UseSearchParams) {
  return useMemo(() => {
    let filtered = movies.filter((m) => !m.paused)

    if (freeOnly) {
      filtered = filtered.filter((m) => m.isFree)
    }

    if (selectedCategory && selectedCategory !== 'all') {
      filtered = filtered.filter((m) => m.category === selectedCategory)
    }

    if (selectedGenres.size > 0) {
      filtered = filtered.filter((m) =>
        Array.from(selectedGenres).some((g) => m.genres.includes(g)),
      )
    }

    if (query.trim()) {
      const q = query.toLowerCase().trim()
      filtered = filtered.filter(
        (m) =>
          m.title.toLowerCase().includes(q) ||
          m.description.toLowerCase().includes(q) ||
          m.language.toLowerCase().includes(q),
      )
    }

    const sectionPriority: Record<string, number> = {
      'hit-of-this-year': 0,
      'all-time-hit': 1,
      'hot': 2,
    }

    return [...filtered].sort((a, b) => {
      const aSection = a.sections?.[0]
      const bSection = b.sections?.[0]
      const aPriority = aSection != null ? (sectionPriority[aSection] ?? 3) : 3
      const bPriority = bSection != null ? (sectionPriority[bSection] ?? 3) : 3

      if (aPriority !== bPriority) return aPriority - bPriority

      if (aSection === 'all-time-hit' && bSection === 'all-time-hit') {
        return (b.year ?? 0) - (a.year ?? 0)
      }

      return 0
    })
  }, [movies, query, selectedCategory, selectedGenres, freeOnly])
}
