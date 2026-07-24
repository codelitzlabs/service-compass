import { create } from 'zustand'

type CatalogView = 'grid' | 'list'

interface Preferences {
  catalogView: CatalogView
  setCatalogView: (catalogView: CatalogView) => void
}

export const usePreferences = create<Preferences>((set) => ({
  catalogView: 'grid',
  setCatalogView: (catalogView) => set({ catalogView }),
}))
