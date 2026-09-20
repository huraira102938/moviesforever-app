import { useApp } from '../../context/AppContext'

export default function AdminNote() {
  const { pricing } = useApp()

  if (!pricing?.note) return null

  return (
    <div className="bg-brand-500/10 border border-brand-500/20 rounded-xl px-4 py-3">
      <p className="text-brand-300 text-sm">{pricing.note}</p>
    </div>
  )
}
