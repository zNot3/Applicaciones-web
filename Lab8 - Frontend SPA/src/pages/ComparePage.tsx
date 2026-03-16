// src/pages/ComparePage.tsx
import type React from 'react';
import { Link } from 'react-router-dom';
import { ArrowLeft, GitCompareArrows, X } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Card, CardContent } from '@/components/ui/card';
import { useCompare } from '@/context/CompareContext';
import { PROPERTY_TYPE_LABELS, OPERATION_TYPE_LABELS, type Property } from '@/types/property';
import { formatPrice, formatArea } from '@/lib/utils';

// ─── Helpers ─────────────────────────────────────────────────────────────────

function getBestIds(
  properties: Property[],
  getValue: (p: Property) => number,
  mode: 'lowest' | 'highest'
): string[] {
  if (properties.length < 2) return [];
  const values = properties.map(getValue);
  const best = mode === 'lowest' ? Math.min(...values) : Math.max(...values);
  return properties.filter((p) => getValue(p) === best).map((p) => p.id);
}

// ─── Definición de métricas ───────────────────────────────────────────────────

interface MetricConfig {
  label: string;
  render: (p: Property) => React.ReactNode;
  bestIds?: (properties: Property[]) => string[];
}

const METRICS: MetricConfig[] = [
  {
    label: 'Tipo',
    render: (p) => PROPERTY_TYPE_LABELS[p.propertyType],
  },
  {
    label: 'Operación',
    render: (p) => OPERATION_TYPE_LABELS[p.operationType],
  },
  {
    label: 'Ciudad',
    render: (p) => p.city,
  },
  {
    label: 'Precio',
    render: (p) => (
      <>
        {formatPrice(p.price)}
        {p.operationType === 'alquiler' && (
          <span className="text-xs font-normal text-muted-foreground"> /mes</span>
        )}
      </>
    ),
    bestIds: (ps) => getBestIds(ps, (p) => p.price, 'lowest'),
  },
  {
    label: 'Área',
    render: (p) => formatArea(p.area),
    bestIds: (ps) => getBestIds(ps, (p) => p.area, 'highest'),
  },
  {
    label: 'Habitaciones',
    render: (p) => (p.bedrooms > 0 ? `${p.bedrooms} hab.` : '—'),
    bestIds: (ps) => getBestIds(ps, (p) => p.bedrooms, 'highest'),
  },
  {
    label: 'Baños',
    render: (p) => (p.bathrooms > 0 ? `${p.bathrooms} baños` : '—'),
    bestIds: (ps) => getBestIds(ps, (p) => p.bathrooms, 'highest'),
  },
  {
    label: 'Precio / m²',
    render: (p) => {
      const perSqm = p.area > 0 ? p.price / p.area : 0;
      return formatPrice(Math.round(perSqm));
    },
    bestIds: (ps) =>
      getBestIds(ps, (p) => (p.area > 0 ? p.price / p.area : Infinity), 'lowest'),
  },
  {
    label: 'Amenidades',
    render: (p) =>
      p.amenities.length > 0
        ? `${p.amenities.length} incluidas`
        : 'Ninguna',
    bestIds: (ps) => getBestIds(ps, (p) => p.amenities.length, 'highest'),
  },
];

// ─── Componente ───────────────────────────────────────────────────────────────

export function ComparePage(): React.ReactElement {
  const { compareList, removeFromCompare, clearCompare } = useCompare();

  const resolvedBestIds = METRICS.map((m) =>
    m.bestIds ? m.bestIds(compareList) : []
  );

  return (
    <div className="container mx-auto px-4 py-8">
      {/* Header */}
      <div className="mb-6">
        <Button asChild variant="ghost" className="mb-4">
          <Link to="/">
            <ArrowLeft className="h-4 w-4 mr-2" />
            Volver al listado
          </Link>
        </Button>

        <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-3">
          <div>
            <h1 className="text-3xl font-bold">Comparar Propiedades</h1>
            <p className="text-muted-foreground">
              {compareList.length === 0
                ? 'Ninguna propiedad seleccionada'
                : `${compareList.length} de 3 propiedades seleccionadas`}
            </p>
          </div>

          {compareList.length > 0 && (
            <Button variant="outline" size="sm" onClick={clearCompare}>
              <X className="h-4 w-4 mr-1" />
              Limpiar comparación
            </Button>
          )}
        </div>
      </div>

      {/* Empty state */}
      {compareList.length === 0 && (
        <Card>
          <CardContent className="py-20 text-center">
            <GitCompareArrows className="h-16 w-16 mx-auto mb-4 text-muted-foreground opacity-40" />
            <p className="text-lg font-semibold mb-2">
              No hay propiedades para comparar
            </p>
            <p className="text-muted-foreground mb-6">
              Vuelve al listado y presiona{' '}
              <strong>"Comparar"</strong> en hasta 3 propiedades.
            </p>
            <Button asChild>
              <Link to="/">Ver propiedades</Link>
            </Button>
          </CardContent>
        </Card>
      )}

      {/* Tabla de comparación */}
      {compareList.length > 0 && (
        <div className="overflow-x-auto rounded-lg border shadow-sm">
          <table className="w-full text-sm">
            <thead>
              <tr className="border-b bg-muted/50">
                {/* Columna de etiquetas */}
                <th className="px-4 py-3 text-left text-muted-foreground font-medium w-36 border-r">
                  Propiedad
                </th>

                {/* Una columna por propiedad */}
                {compareList.map((property) => {
                  const imageUrl =
                    property.images?.[0] ??
                    `https://placehold.co/800x600/e2e8f0/64748b?text=${encodeURIComponent(property.propertyType)}`;

                  return (
                    <th
                      key={property.id}
                      className="px-4 py-3 text-center min-w-[200px]"
                    >
                      <div className="flex flex-col items-center gap-2">
                        <img
                          src={imageUrl}
                          alt={property.title}
                          className="w-full h-28 object-cover rounded-md"
                        />
                        <span className="font-semibold text-foreground leading-tight line-clamp-2">
                          {property.title}
                        </span>
                        <p className="text-xs text-muted-foreground">
                          {property.address}, {property.city}
                        </p>
                        <Button
                          variant="ghost"
                          size="sm"
                          className="text-destructive hover:text-destructive hover:bg-destructive/10 h-7 text-xs"
                          onClick={() => removeFromCompare(property.id)}
                        >
                          <X className="h-3 w-3 mr-1" />
                          Quitar
                        </Button>
                      </div>
                    </th>
                  );
                })}
              </tr>
            </thead>

            <tbody>
              {METRICS.map((metric, metricIndex) => {
                const bestIds = resolvedBestIds[metricIndex] ?? [];

                return (
                  <tr
                    key={metric.label}
                    className={metricIndex % 2 === 0 ? 'bg-background' : 'bg-muted/20'}
                  >
                    {/* Etiqueta de la métrica */}
                    <td className="px-4 py-3 font-medium text-muted-foreground border-r">
                      {metric.label}
                    </td>

                    {/* Valor por propiedad */}
                    {compareList.map((property) => {
                      const isBest =
                        compareList.length > 1 && bestIds.includes(property.id);

                      return (
                        <td
                          key={property.id}
                          className={`px-4 py-3 text-center font-medium transition-colors ${
                            isBest
                              ? 'text-green-700 bg-green-50'
                              : 'text-foreground'
                          }`}
                        >
                          {metric.render(property)}
                          {isBest && (
                            <span
                              className="ml-1 text-xs text-green-600"
                              title="Mejor valor"
                            >
                              ★
                            </span>
                          )}
                        </td>
                      );
                    })}
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      )}

      {/* Leyenda */}
      {compareList.length > 1 && (
        <p className="text-xs text-muted-foreground mt-3">
          ★ indica el mejor valor en cada categoría
        </p>
      )}
    </div>
  );
}
