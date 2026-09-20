import { doc, onSnapshot } from 'firebase/firestore'
import { useEffect, useState } from 'react'
import { db } from '../config/firebase'

export function useFirestoreDoc<T>(
  collectionPath: string,
  docId: string,
  mapFn: (id: string, data: Record<string, unknown>) => T,
  enabled = true,
): T | null {
  const [item, setItem] = useState<T | null>(null)

  useEffect(() => {
    if (!enabled || !docId) {
      setItem(null)
      return
    }

    const docRef = doc(db, collectionPath, docId)
    const unsubscribe = onSnapshot(docRef, (snapshot) => {
      if (snapshot.exists()) {
        setItem(mapFn(snapshot.id, snapshot.data() as Record<string, unknown>))
      } else {
        setItem(null)
      }
    })

    return () => unsubscribe()
  }, [collectionPath, docId, enabled])

  return item
}
