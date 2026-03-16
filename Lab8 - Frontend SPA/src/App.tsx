import type React from 'react';
import { Routes, Route, Link } from 'react-router-dom';
import { Toaster } from '@/components/ui/sonner';
import { Home, Building2, GitCompareArrows } from 'lucide-react';
import { HomePage } from '@/pages/HomePage';
import { NewPropertyPage } from '@/pages/NewPropertyPage';
import { PropertyDetailPage } from '@/pages/PropertyDetailPage';
import { ComparePage } from '@/pages/ComparePage';
import { CompareProvider, useCompare } from '@/context/CompareContext';

function CompareBanner(): React.ReactElement | null {
  const { compareList } = useCompare();
  if (compareList.length === 0) return null;

  return (
    <div className="fixed bottom-5 left-1/2 -translate-x-1/2 z-50 flex items-center gap-4 bg-primary text-primary-foreground px-6 py-3 rounded-full shadow-lg text-sm font-medium">
      <GitCompareArrows className="h-4 w-4" />
      <span>
        {compareList.length} / 3 propiedades seleccionadas
      </span>
      <Link
        to="/compare"
        className="bg-primary-foreground text-primary px-3 py-1 rounded-full hover:opacity-90 transition-opacity font-semibold"
      >
        Comparar →
      </Link>
    </div>
  );
}

function App(): React.ReactElement {
  return (
    <CompareProvider>
      <Toaster position="top-right" richColors closeButton />

      <div className="min-h-screen flex flex-col bg-background">
        {/* Header */}
        <header className="sticky top-0 z-50 w-full border-b bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/60">
          <div className="container mx-auto flex h-16 items-center px-4">
            <Link to="/" className="flex items-center gap-2 font-bold text-xl">
              <Building2 className="h-6 w-6 text-primary" />
              <span>RealEstate</span>
            </Link>

            <nav className="ml-auto flex items-center gap-4">
              <Link
                to="/"
                className="flex items-center gap-1 text-sm font-medium text-muted-foreground hover:text-foreground transition-colors"
              >
                <Home className="h-4 w-4" />
                Inicio
              </Link>

              <Link
                to="/compare"
                className="flex items-center gap-1 text-sm font-medium text-muted-foreground hover:text-foreground transition-colors"
              >
                <GitCompareArrows className="h-4 w-4" />
                Comparar
              </Link>
            </nav>
          </div>
        </header>

        <main className="flex-1">
          <Routes>
            <Route path="/" element={<HomePage />} />
            <Route path="/new" element={<NewPropertyPage />} />
            <Route path="/property/:id" element={<PropertyDetailPage />} />
            <Route path="/compare" element={<ComparePage />} />
            <Route
              path="*"
              element={
                <div className="container mx-auto px-4 py-16 text-center">
                  <h1 className="text-4xl font-bold mb-4">404</h1>
                  <p className="text-muted-foreground mb-6">Página no encontrada</p>
                  <Link to="/" className="text-primary hover:underline">
                    Volver al inicio
                  </Link>
                </div>
              }
            />
          </Routes>
        </main>

        {/* Banner flotante de comparación */}
        <CompareBanner />

        <footer className="border-t py-6 mt-auto">
          <div className="container mx-auto px-4 text-center text-sm text-muted-foreground">
            <p>Portal Inmobiliario - Módulo 2 del Curso de Desarrollo Web</p>
            <p className="mt-1">Desarrollado con React 19, Tailwind CSS y Shadcn UI</p>
          </div>
        </footer>
      </div>
    </CompareProvider>
  );
}

export default App;
