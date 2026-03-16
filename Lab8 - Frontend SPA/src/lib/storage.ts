import type { Property, PropertyFilters, CreatePropertyInput } from '@/types/property';
import { generateId } from './utils';

const STORAGE_KEY = 'real_estate_properties';

export function getAllProperties(): Property[] {
  try {
    const data = localStorage.getItem(STORAGE_KEY);
    if (!data) return [];

    // Parseamos y validamos que sea un array
    const parsed: unknown = JSON.parse(data);
    if (!Array.isArray(parsed)) {
      console.error('Datos corruptos en localStorage, reiniciando...');
      return [];
    }

    return parsed as Property[];
  } catch (error) {
    console.error('Error al leer propiedades:', error);
    return [];
  }
}

export function getPropertyById(id: string): Property | undefined {
  const properties = getAllProperties();
  return properties.find((p) => p.id === id);
}

export function createProperty(input: CreatePropertyInput): Property {
  const properties = getAllProperties();

  const newProperty: Property = {
    ...input,
    id: generateId(),
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString(),
  };

  properties.push(newProperty);
  saveProperties(properties);

  return newProperty;
}

export function updateProperty(id: string, input: Partial<CreatePropertyInput>): Property | null {
  const properties = getAllProperties();
  const index = properties.findIndex((p) => p.id === id);

  if (index === -1) return null;

  const existingProperty = properties[index];
  if (!existingProperty) return null;

  const updatedProperty: Property = {
    ...existingProperty,
    ...input,
    updatedAt: new Date().toISOString(),
  };

  properties[index] = updatedProperty;
  saveProperties(properties);

  return updatedProperty;
}

export function deleteProperty(id: string): boolean {
  const properties = getAllProperties();
  const filtered = properties.filter((p) => p.id !== id);

  if (filtered.length === properties.length) {
    return false; // No se encontró la propiedad
  }

  saveProperties(filtered);
  return true;
}

export function filterProperties(filters: PropertyFilters): Property[] {
  let properties = getAllProperties();

  // Filtro de búsqueda por texto (título, descripción, dirección)
  if (filters.search) {
    const searchLower = filters.search.toLowerCase();
    properties = properties.filter(
      (p) =>
        p.title.toLowerCase().includes(searchLower) ||
        p.description.toLowerCase().includes(searchLower) ||
        p.address.toLowerCase().includes(searchLower) ||
        p.city.toLowerCase().includes(searchLower)
    );
  }

  // Filtro por tipo de propiedad
  if (filters.propertyType) {
    properties = properties.filter((p) => p.propertyType === filters.propertyType);
  }

  // Filtro por tipo de operación
  if (filters.operationType) {
    properties = properties.filter((p) => p.operationType === filters.operationType);
  }

  // Filtro por precio mínimo
  if (filters.minPrice !== undefined && filters.minPrice > 0) {
    properties = properties.filter((p) => p.price >= filters.minPrice!);
  }

  // Filtro por precio máximo
  if (filters.maxPrice !== undefined && filters.maxPrice > 0) {
    properties = properties.filter((p) => p.price <= filters.maxPrice!);
  }

  // Filtro por habitaciones mínimas
  if (filters.minBedrooms !== undefined && filters.minBedrooms > 0) {
    properties = properties.filter((p) => p.bedrooms >= filters.minBedrooms!);
  }

  // Filtro por ciudad
  if (filters.city) {
    properties = properties.filter((p) => p.city.toLowerCase().includes(filters.city!.toLowerCase()));
  }

  // Ordenar por fecha de creación (más recientes primero)
  return properties.sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime());
}

function saveProperties(properties: Property[]): void {
  try {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(properties));
  } catch (error) {
    // localStorage puede fallar si está lleno o deshabilitado
    console.error('Error al guardar propiedades:', error);
    throw new Error('No se pudieron guardar los datos. El almacenamiento podría estar lleno.');
  }
}

export function initializeWithSampleData(): void {
  const existing = getAllProperties();
  if (existing.length > 0) return; // Ya hay datos

  // Importamos y guardamos datos de ejemplo
  void import('@/data/sampleProperties').then((module) => {
    const sampleData = module.sampleProperties;
    saveProperties(sampleData as Property[]);
    console.log('Datos de ejemplo cargados');
  });
}
