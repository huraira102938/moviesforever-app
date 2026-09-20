import { useNavigate } from 'react-router-dom'
import { ArrowLeft, Bell } from 'lucide-react'
import { useApp } from '../context/AppContext'
import { formatDate } from '../lib/utils'
import { Card } from '../components/ui/Card'

export default function NotificationsScreen() {
  const navigate = useNavigate()
  const { myNotifications } = useApp()

  return (
    <div className="min-h-screen">
      <div className="max-w-2xl mx-auto px-5 py-10 flex flex-col gap-6">
        <div className="flex items-center gap-3">
          <button
            onClick={() => navigate(-1)}
            className="w-10 h-10 rounded-full bg-white/10 border border-white/10 flex items-center justify-center text-white hover:bg-white/15 transition"
          >
            <ArrowLeft className="w-5 h-5" />
          </button>
          <div className="flex items-center gap-2">
            <Bell className="w-5 h-5 text-brand-400" />
            <h1 className="text-xl font-bold text-white">Notifications</h1>
          </div>
        </div>

        {myNotifications.length === 0 ? (
          <div className="flex flex-col items-center justify-center py-16 text-gray-500 gap-3">
            <Bell className="w-10 h-10 text-gray-600" />
            <p className="text-sm">No notifications yet</p>
          </div>
        ) : (
          <div className="flex flex-col gap-3">
            {myNotifications.map((notification) => (
              <Card key={notification.id}>
                <p className="text-white text-sm leading-relaxed">{notification.text}</p>
                {notification.createdAt && (
                  <p className="text-gray-500 text-xs mt-2">
                    {formatDate(notification.createdAt)}
                  </p>
                )}
              </Card>
            ))}
          </div>
        )}
      </div>
    </div>
  )
}