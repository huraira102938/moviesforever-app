export default function GlowOrb({ className }: { className?: string }) {
  return (
    <div
      className={
        className ||
        'absolute w-96 h-96 rounded-full blur-3xl bg-brand-500/10 pointer-events-none'
      }
    />
  )
}
