import { useEffect, useState, useRef } from 'react'
import {
  collection,
  onSnapshot,
  query,
  orderBy,
  where,
  DocumentData,
  QueryConstraint,
} from 'firebase/firestore'
import { db } from '../config/firebase'

export function useFirestoreCollection<T>(
  collectionPath: string,
  mapFn: (id: string, data: DocumentData) => T,
  constraints: QueryConstraint[] = [],
  enabled = true,
): T[] {
  const [items, setItems] = useState<T[]>([])
  const mapFnRef = useRef(mapFn)
  mapFnRef.current = mapFn

  useEffect(() => {
    if (!enabled) {
      setItems([])
      return
    }

    const colRef = collection(db, collectionPath)
    const q = constraints.length > 0 ? query(colRef, ...constraints) : colRef

    const unsubscribe = onSnapshot(q, (snapshot) => {
      const result = snapshot.docs.map((doc) => mapFnRef.current(doc.id, doc.data()))
      setItems(result)
    })

    return () => unsubscribe()
  }, [collectionPath, enabled, constraints.length])

  return items
}

export function useFirestoreDoc<T>(
  docPath: string,
  mapFn: (id: string, data: DocumentData) => T,
  enabled = true,
): T | null {
  const [item, setItem] = useState<T | null>(null)
  const mapFnRef = useRef(mapFn)
  mapFnRef.current = mapFn

  useEffect(() => {
    if (!enabled || !docPath) {
      setItem(null)
      return
    }

    const docRef = import('firebase/firestore').then((mod) => mod.doc(db, docPath)).then((docRef) => {
      return onSnapshot(docRef, (snapshot) => {
        if (snapshot.exists()) {
          setItem(mapFnRef.current(snapshot.id, snapshot.data()!))
        } else {
          setItem(null)
        }
      })
    })

    let cleanup: (() => void) | null = null
    docRef.then((unsub) => {
      cleanup = unsub
    })

    return () => {
      if (cleanup) cleanup()
    }
  }, [docPath, enabled])

  return item
}
