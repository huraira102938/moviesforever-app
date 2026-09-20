import { lazy, Suspense, useEffect } from 'react'
import { BrowserRouter, Routes, Route, Navigate, useLocation } from 'react-router-dom'
import { AppProvider, useApp } from './context/AppContext'
import AppLayout from './components/layout/AppLayout'
import { Spinner } from './components/ui/Spinner'

const SplashScreen = lazy(() => import('./pages/SplashScreen'))
const WelcomeScreen = lazy(() => import('./pages/WelcomeScreen'))
const LockScreen = lazy(() => import('./pages/LockScreen'))
const PaymentInstructions = lazy(() => import('./pages/PaymentInstructions'))
const HomeScreen = lazy(() => import('./pages/HomeScreen'))
const SearchScreen = lazy(() => import('./pages/SearchScreen'))
const ProfileScreen = lazy(() => import('./pages/ProfileScreen'))
const MovieDetailScreen = lazy(() => import('./pages/MovieDetailScreen'))
const PlayerScreen = lazy(() => import('./pages/PlayerScreen'))
const CategoryBrowseScreen = lazy(() => import('./pages/CategoryBrowseScreen'))
const CelebrationScreen = lazy(() => import('./pages/CelebrationScreen'))
const SettingsScreen = lazy(() => import('./pages/SettingsScreen'))
const PausedScreen = lazy(() => import('./pages/PausedScreen'))
const NotificationsScreen = lazy(() => import('./pages/NotificationsScreen'))

function PageLoader() {
  return (
    <div className="min-h-screen flex items-center justify-center">
      <Spinner size="lg" />
    </div>
  )
}

function PausedGuard({ children }: { children: React.ReactNode }) {
  const { userAccount, isUnlocked } = useApp()
  const location = useLocation()

  const isPaused = isUnlocked && userAccount?.paused === true
  const isPausedPage = location.pathname === '/paused'

  if (isPaused && !isPausedPage) {
    return <Navigate to="/paused" replace />
  }

  if (!isPaused && isPausedPage) {
    return <Navigate to="/" replace />
  }

  return <>{children}</>
}

function ScrollToTop() {
  const { pathname } = useLocation()
  useEffect(() => {
    window.scrollTo(0, 0)
  }, [pathname])
  return null
}

export default function App() {
  return (
    <BrowserRouter>
      <AppProvider>
        <PausedGuard>
          <ScrollToTop />
          <Suspense fallback={<PageLoader />}>
            <Routes>
              <Route element={<AppLayout />}>
                <Route path="/" element={<HomeScreen />} />
                <Route path="/search" element={<SearchScreen />} />
                <Route path="/profile" element={<ProfileScreen />} />
                <Route path="/movie/:movieId" element={<MovieDetailScreen />} />
                <Route path="/category/:categoryId" element={<CategoryBrowseScreen />} />
                <Route path="/notifications" element={<NotificationsScreen />} />
                <Route path="/settings" element={<SettingsScreen />} />
                <Route path="/lock" element={<LockScreen />} />
                <Route path="/lock/payment" element={<PaymentInstructions />} />
                <Route path="/paused" element={<PausedScreen />} />
              </Route>

              <Route path="/welcome" element={<WelcomeScreen />} />
              <Route path="/player/:movieId" element={<PlayerScreen />} />
              <Route path="/celebration" element={<CelebrationScreen />} />
              <Route path="/splash" element={<SplashScreen />} />
              <Route path="*" element={<Navigate to="/" replace />} />
            </Routes>
          </Suspense>
        </PausedGuard>
      </AppProvider>
    </BrowserRouter>
  )
}
