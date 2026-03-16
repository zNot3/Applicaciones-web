import { createContext, useContext, useState } from 'react';
import type { Property } from '@/types/property';

interface CompareContextValue {
  compareList: Property[];
  addToCompare: (property: Property) => void;
  removeFromCompare: (id: string) => void;
  clearCompare: () => void;
}

const CompareContext = createContext<CompareContextValue | null>(null);

export function CompareProvider({ children }: { children: React.ReactNode }) {
  const [compareList, setCompareList] = useState<Property[]>([]);

  const addToCompare = (property: Property) => {
    setCompareList((prev) => {
      if (prev.length >= 3) return prev;
      if (prev.some((p) => p.id === property.id)) return prev;
      return [...prev, property];
    });
  };

  const removeFromCompare = (id: string) => {
    setCompareList((prev) => prev.filter((p) => p.id !== id));
  };

  const clearCompare = () => setCompareList([]);

  return (
    <CompareContext.Provider value={{ compareList, addToCompare, removeFromCompare, clearCompare }}>
      {children}
    </CompareContext.Provider>
  );
}

export function useCompare() {
  const ctx = useContext(CompareContext);
  if (!ctx) throw new Error('useCompare must be used inside <CompareProvider>');
  return ctx;
}
