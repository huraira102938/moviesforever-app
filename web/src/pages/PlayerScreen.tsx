import { useEffect, useRef, useState, useMemo } from 'react'
import { useParams, useNavigate, useSearchParams } from 'react-router-dom'
import {
  ArrowLeft,
  Maximize,
  Minimize,
  X,
  AlertTriangle,
  RefreshCw,
  Monitor,
  ZoomIn,
  Maximize2,
  ArrowLeftRight,
} from 'lucide-react'
import { useApp } from '../context/AppContext'
import { getSignedUrl } from '../lib/workerApi'
import { Spinner } from '../components/ui/Spinner'
import { cn } from '../lib/utils'

type ResizeMode = 'fit' | 'zoom' | 'fill' | 'fixed-width' | 'fixed-height'

const RESIZE_OPTIONS: { mode: ResizeMode; label: string; icon: React.ReactNode }[] = [
  { mode: 'fit', label: 'Fit', icon: <Monitor className="w-4 h-4" /> },
  { mode: 'zoom', label: 'Zoom', icon: <ZoomIn className="w-4 h-4" /> },
  { mode: 'fill', label: 'Fill', icon: <Maximize2 className="w-4 h-4" /> },
  { mode: 'fixed-width', label: 'Fixed Width', icon: <ArrowLeftRight className="w-4 h-4" /> },
  { mode: 'fixed-height', label: 'Fixed Height', icon: <ArrowLeftRight className="w-4 h-4 rotate-90" /> },
]

function getVideoObjectFit(mode: ResizeMode): React.CSSProperties['objectFit'] {
  switch (mode) {
    case 'fit': return 'contain'
    case 'zoom': return 'cover'
    case 'fill': return 'fill'
    case 'fixed-width': return 'contain'
    case 'fixed-height': return 'contain'
  }
}

function getVideoClass(mode: ResizeMode): string {
  switch (mode) {
    case 'fixed-width': return 'w-full'
    case 'fixed-height': return 'h-full'
    default: return ''
  }
}

export default function PlayerScreen() {
  const { movieId } = useParams<{ movieId: string }>()
  const [searchParams] = useSearchParams()
  const navigate = useNavigate()
  const { movies } = useApp()

  const movie = movies.find((m) => m.id === movieId)
  const isTrailer = searchParams.get('trailer') === 'true'

  const videoRef = useRef<HTMLVideoElement>(null)
  const containerRef = useRef<HTMLDivElement>(null)

  const [videoUrl, setVideoUrl] = useState<string | null>(null)
  const [loadingUrl, setLoadingUrl] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [controlsVisible, setControlsVisible] = useState(true)
  const [resizeMode, setResizeMode] = useState<ResizeMode>('fit')
  const [showResizeMenu, setShowResizeMenu] = useState(false)
  const [isFullscreen, setIsFullscreen] = useState(false)

  const title = movie?.title ?? (isTrailer ? 'Trailer' : 'Playing')

  useEffect(() => {
    let cancelled = false
    const fetchUrl = async () => {
      if (!movie) return
      setLoadingUrl(true)
      try {
        const url = isTrailer ? movie.trailerUrl : movie.videoUrl
        if (url) {
          if (!cancelled) setVideoUrl(url)
        } else {
          const res = await getSignedUrl(movie.id)
          if (!cancelled) {
            if (res.allowed && res.url) setVideoUrl(res.url)
            else setError(res.message || 'Unable to play this video.')
          }
        }
      } catch {
        if (!cancelled) setError('Failed to load video. Please try again.')
      } finally {
        if (!cancelled) setLoadingUrl(false)
      }
    }
    fetchUrl()
    return () => { cancelled = true }
  }, [movie, isTrailer])

  useEffect(() => {
    let timer: ReturnType<typeof setTimeout>
    const show = () => {
      setControlsVisible(true)
      clearTimeout(timer)
      timer = setTimeout(() => setControlsVisible(false), 4000)
    }
    show()
    const container = containerRef.current
    container?.addEventListener('mousemove', show)
    container?.addEventListener('click', show)
    return () => {
      clearTimeout(timer)
      container?.removeEventListener('mousemove', show)
      container?.removeEventListener('click', show)
    }
  }, [])

  const toggleFullscreen = async () => {
    if (!containerRef.current) return
    if (!document.fullscreenElement) {
      await containerRef.current.requestFullscreen()
      setIsFullscreen(true)
    } else {
      await document.exitFullscreen()
      setIsFullscreen(false)
    }
  }

  const videoStyle: React.CSSProperties = useMemo(() => ({
    objectFit: getVideoObjectFit(resizeMode),
    ...(resizeMode === 'fixed-width' ? { width: '100%' } : {}),
    ...(resizeMode === 'fixed-height' ? { height: '100%' } : {}),
  }), [resizeMode])

  if (error) {
    return (
      <div className="min-h-screen flex flex-col items-center justify-center gap-4 px-5 text-center">
        <div className="w-16 h-16 rounded-full bg-red-500/15 flex items-center justify-center">
          <AlertTriangle className="w-8 h-8 text-red-400" />
        </div>
        <p className="text-white font-semibold text-lg">Playback Error</p>
        <p className="text-gray-400 text-sm max-w-sm">{error}</p>
        <div className="flex gap-3">
          <button
            onClick={() => { setError(null); setLoadingUrl(true); window.location.reload() }}
            className="flex items-center gap-2 px-5 py-2.5 bg-brand-500 text-white rounded-xl text-sm font-semibold hover:bg-brand-600 transition"
          >
            <RefreshCw className="w-4 h-4" />
            Retry
          </button>
          <button
            onClick={() => navigate(-1)}
            className="flex items-center gap-2 px-5 py-2.5 border border-white/10 bg-white/5 text-white rounded-xl text-sm font-medium hover:bg-white/10 transition"
          >
            <ArrowLeft className="w-4 h-4" />
            Go Back
          </button>
        </div>
      </div>
    )
  }

  return (
    <div
      ref={containerRef}
      className="relative bg-black w-screen h-screen overflow-hidden"
    >
      {loadingUrl ? (
        <div className="absolute inset-0 flex flex-col items-center justify-center gap-4">
          <Spinner size="lg" />
          <p className="text-gray-400 text-sm">Loading video...</p>
        </div>
      ) : videoUrl ? (
        <video
          ref={videoRef}
          src={videoUrl}
          autoPlay
          controls
          className={cn('w-full h-full', getVideoClass(resizeMode))}
          style={videoStyle}
        />
      ) : null}

      <div
        className={cn(
          'absolute inset-x-0 top-0 p-4 bg-gradient-to-b from-black/80 to-transparent transition-opacity duration-300 z-20',
          controlsVisible ? 'opacity-100' : 'opacity-0 pointer-events-none',
        )}
      >
        <div className="flex items-center justify-between">
          <button
            onClick={() => navigate(-1)}
            className="w-10 h-10 rounded-full bg-white/10 backdrop-blur-sm flex items-center justify-center text-white hover:bg-white/20 transition"
          >
            <ArrowLeft className="w-5 h-5" />
          </button>

          <p className="text-white text-sm font-medium truncate max-w-[50%] text-center">{title}</p>

          <div className="flex items-center gap-2 relative">
            <button
              onClick={() => setShowResizeMenu(!showResizeMenu)}
              className="w-10 h-10 rounded-full bg-white/10 backdrop-blur-sm flex items-center justify-center text-white hover:bg-white/20 transition"
            >
              <Monitor className="w-4 h-4" />
            </button>

            {showResizeMenu && (
              <div className="absolute right-0 top-12 bg-[#141428] border border-white/10 rounded-xl shadow-2xl overflow-hidden w-44 z-30">
                {RESIZE_OPTIONS.map((opt) => (
                  <button
                    key={opt.mode}
                    onClick={() => { setResizeMode(opt.mode); setShowResizeMenu(false) }}
                    className={cn(
                      'w-full flex items-center gap-2.5 px-4 py-2.5 text-sm transition',
                      resizeMode === opt.mode
                        ? 'bg-brand-500/15 text-brand-400'
                        : 'text-gray-300 hover:bg-white/5',
                    )}
                  >
                    {opt.icon}
                    {opt.label}
                  </button>
                ))}
              </div>
            )}

            <button
              onClick={toggleFullscreen}
              className="w-10 h-10 rounded-full bg-white/10 backdrop-blur-sm flex items-center justify-center text-white hover:bg-white/20 transition"
            >
              {isFullscreen ? <Minimize className="w-4 h-4" /> : <Maximize className="w-4 h-4" />}
            </button>
          </div>
        </div>
      </div>
    </div>
  )
}