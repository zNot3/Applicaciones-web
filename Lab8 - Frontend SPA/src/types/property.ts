import { z } from 'zod';

/**
 * Tipos de operación inmobiliaria disponibles.
 */
export const OPERATION_TYPES = ['venta', 'alquiler'] as const;
export type OperationType = (typeof OPERATION_TYPES)[number];

/**
 * Tipos de propiedad disponibles.
 */
export const PROPERTY_TYPES = ['casa', 'apartamento', 'terreno', 'local', 'oficina'] as const;
export type PropertyType = (typeof PROPERTY_TYPES)[number];

/**
 * Amenidades disponibles para las propiedades.
 */
export const AMENITIES = [
  'piscina',
  'jardin',
  'garage',
  'seguridad',
  'gimnasio',
  'terraza',
  'ascensor',
  'aire_acondicionado',
  'calefaccion',
  'amueblado',
] as const;
export type Amenity = (typeof AMENITIES)[number];

export const createPropertySchema = z.object({
  // Información básica
  title: z
    .string()
    .min(10, 'El título debe tener al menos 10 caracteres')
    .max(100, 'El título no puede exceder 100 caracteres'),

  description: z
    .string()
    .min(50, 'La descripción debe tener al menos 50 caracteres')
    .max(2000, 'La descripción no puede exceder 2000 caracteres'),

  // Tipo y operación
  propertyType: z.enum(PROPERTY_TYPES, {
    error: 'Selecciona un tipo de propiedad válido',
  }),

  operationType: z.enum(OPERATION_TYPES, {
    error: 'Selecciona un tipo de operación válido',
  }),

  // Precio
  price: z
    .number({
      error: 'El precio es requerido y debe ser un número',
    })
    .positive('El precio debe ser mayor a 0')
    .max(100_000_000, 'El precio parece demasiado alto'),

  // Ubicación
  address: z.string().min(10, 'La dirección debe tener al menos 10 caracteres'),

  city: z.string().min(2, 'La ciudad es requerida'),

  // Características
  bedrooms: z
    .number()
    .int('El número de habitaciones debe ser entero')
    .min(0, 'El número de habitaciones no puede ser negativo')
    .max(20, 'Máximo 20 habitaciones'),

  bathrooms: z
    .number()
    .int('El número de baños debe ser entero')
    .min(0, 'El número de baños no puede ser negativo')
    .max(20, 'Máximo 20 baños'),

  area: z
    .number()
    .positive('El área debe ser mayor a 0')
    .max(100_000, 'El área parece demasiado grande'),

  // Amenidades (opcional)
  amenities: z.array(z.enum(AMENITIES)).default([]),

  // Imágenes (opcional)
  images: z
    .array(
      z.string().url('URL de imagen inválida')
    )
    .default([]),
});

export type CreatePropertyInput = z.infer<typeof createPropertySchema>;

export interface Property extends CreatePropertyInput {
  /** Identificador único de la propiedad */
  id: string;

  /** Fecha de creación (ISO string) */
  createdAt: string;

  /** Fecha de última actualización (ISO string) */
  updatedAt: string;
}

/**
 * Filtros disponibles para la búsqueda de propiedades.
 */
export interface PropertyFilters {
  search?: string;
  propertyType?: PropertyType | '';
  operationType?: OperationType | '';
  minPrice?: number;
  maxPrice?: number;
  minBedrooms?: number;
  city?: string;
}

/**
 * Etiquetas legibles para los tipos de propiedad.
 */
export const PROPERTY_TYPE_LABELS: Record<PropertyType, string> = {
  casa: 'Casa',
  apartamento: 'Apartamento',
  terreno: 'Terreno',
  local: 'Local Comercial',
  oficina: 'Oficina',
};

/**
 * Etiquetas legibles para los tipos de operación.
 */
export const OPERATION_TYPE_LABELS: Record<OperationType, string> = {
  venta: 'Venta',
  alquiler: 'Alquiler',
};

/**
 * Etiquetas legibles para las amenidades.
 */
export const AMENITY_LABELS: Record<Amenity, string> = {
  piscina: 'Piscina',
  jardin: 'Jardín',
  garage: 'Garaje',
  seguridad: 'Seguridad 24h',
  gimnasio: 'Gimnasio',
  terraza: 'Terraza',
  ascensor: 'Ascensor',
  aire_acondicionado: 'Aire Acondicionado',
  calefaccion: 'Calefacción',
  amueblado: 'Amueblado',
};
