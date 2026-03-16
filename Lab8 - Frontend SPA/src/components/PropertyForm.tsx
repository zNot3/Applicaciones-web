import type React from 'react';
import { useForm } from 'react-hook-form';

import { toast } from 'sonner';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Textarea } from '@/components/ui/textarea';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import {
  createPropertySchema,
  type CreatePropertyInput,
  PROPERTY_TYPES,
  OPERATION_TYPES,
  PROPERTY_TYPE_LABELS,
  OPERATION_TYPE_LABELS,
} from '@/types/property';

/**
 * Props del formulario de propiedad.
 */
interface PropertyFormProps {
  /** Valores iniciales (para edición) */
  defaultValues?: Partial<CreatePropertyInput>;
  /** Callback al enviar el formulario */
  onSubmit: (data: CreatePropertyInput) => void;
  /** Indica si está guardando */
  isSubmitting?: boolean;
}

export function PropertyForm({
  defaultValues,
  onSubmit,
  isSubmitting = false,
}: PropertyFormProps): React.ReactElement {
  const {
    register,
    handleSubmit,
    setValue,
    watch,
    formState: { errors },
  } = useForm<CreatePropertyInput>({
    resolver: async (values) => {
      try {
        const result = createPropertySchema.safeParse(values);

        if (result.success) {
          return {
            values: result.data,
            errors: {},
          };
        }

        const errors = result.error.issues.reduce(
          (allErrors, currentError) => ({
            ...allErrors,
            [currentError.path[0]]: {
              type: currentError.code,
              message: currentError.message,
            },
          }),
          {} as Record<string, { type: string; message: string }>
        );

        return {
          values: {},
          errors,
        };
      } catch (error) {
        console.error('Error crítico de validación:', error);
        return {
          values: {},
          errors: {
            root: {
              type: 'server',
              message: 'Error inesperado al validar el formulario',
            },
          },
        };
      }
    },
    defaultValues: {
      title: '',
      description: '',
      propertyType: 'apartamento',
      operationType: 'venta',
      price: 0,
      address: '',
      city: '',
      bedrooms: 1,
      bathrooms: 1,
      area: 50,
      amenities: [],
      images: [],
      ...defaultValues,
    },
    mode: 'onTouched',
    reValidateMode: 'onChange',
  });

  const propertyType = watch('propertyType');
  const operationType = watch('operationType');
  const descriptionValue = watch('description');

  const onValidationError = (): void => {
    toast.error('Por favor corrige los errores señalados en el formulario');
  };

  return (
    <form
      onSubmit={handleSubmit(onSubmit, onValidationError)}
      className="space-y-6"
    >
      {/* Sección: Información Básica */}
      <Card>
        <CardHeader>
          <CardTitle>Información Básica</CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          {/* Título */}
          <div className="space-y-2">
            <Label htmlFor="title">Título de la propiedad *</Label>
            <Input
              id="title"
              placeholder="Ej: Elegante apartamento con vista al mar"
              {...register('title')}
            />
            {errors.title && (
              <p className="text-sm text-destructive">{errors.title.message}</p>
            )}
          </div>

          {/* Descripcion */}
          <div className="space-y-2">
            <Label htmlFor="description">Descripcion *</Label>
            <Textarea
              id="description"
              placeholder="Describe la propiedad en detalle (minimo 50 caracteres)..."
              rows={5}
              {...register('description')}
            />
            <div className="flex justify-between items-center">
              <span className={`text-sm ${(descriptionValue?.length ?? 0) < 50 ? 'text-destructive' : 'text-muted-foreground'}`}>
                {descriptionValue?.length ?? 0}/50 caracteres minimos
              </span>
              {errors.description && (
                <p className="text-sm text-destructive">{errors.description.message}</p>
              )}
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Sección: Tipo y Operación */}
      <Card>
        <CardHeader>
          <CardTitle>Tipo y Operación</CardTitle>
        </CardHeader>
        <CardContent className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {/* Tipo de propiedad */}
          <div className="space-y-2">
            <Label>Tipo de propiedad *</Label>
            <Select
              value={propertyType}
              onValueChange={(value) =>
                setValue('propertyType', value as CreatePropertyInput['propertyType'])
              }
            >
              <SelectTrigger>
                <SelectValue placeholder="Selecciona un tipo" />
              </SelectTrigger>
              <SelectContent>
                {PROPERTY_TYPES.map((type) => (
                  <SelectItem key={type} value={type}>
                    {PROPERTY_TYPE_LABELS[type]}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
            {errors.propertyType && (
              <p className="text-sm text-destructive">{errors.propertyType.message}</p>
            )}
          </div>

          {/* Tipo de operación */}
          <div className="space-y-2">
            <Label>Tipo de operación *</Label>
            <Select
              value={operationType}
              onValueChange={(value) =>
                setValue('operationType', value as CreatePropertyInput['operationType'])
              }
            >
              <SelectTrigger>
                <SelectValue placeholder="Selecciona operación" />
              </SelectTrigger>
              <SelectContent>
                {OPERATION_TYPES.map((type) => (
                  <SelectItem key={type} value={type}>
                    {OPERATION_TYPE_LABELS[type]}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
            {errors.operationType && (
              <p className="text-sm text-destructive">{errors.operationType.message}</p>
            )}
          </div>
        </CardContent>
      </Card>

      {/* Sección: Precio */}
      <Card>
        <CardHeader>
          <CardTitle>Precio</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="space-y-2">
            <Label htmlFor="price">
              Precio {operationType === 'alquiler' ? '(mensual)' : ''} *
            </Label>
            <div className="relative">
              <span className="absolute left-3 top-1/2 -translate-y-1/2 text-muted-foreground">
                $
              </span>
              <Input
                id="price"
                type="number"
                className="pl-7"
                placeholder="0"
                {...register('price', { valueAsNumber: true })}
              />
            </div>
            {errors.price && (
              <p className="text-sm text-destructive">{errors.price.message}</p>
            )}
          </div>
        </CardContent>
      </Card>

      {/* Sección: Ubicación */}
      <Card>
        <CardHeader>
          <CardTitle>Ubicación</CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="space-y-2">
            <Label htmlFor="address">Dirección *</Label>
            <Input
              id="address"
              placeholder="Calle, número, piso..."
              {...register('address')}
            />
            {errors.address && (
              <p className="text-sm text-destructive">{errors.address.message}</p>
            )}
          </div>

          <div className="space-y-2">
            <Label htmlFor="city">Ciudad *</Label>
            <Input id="city" placeholder="Ej: Madrid" {...register('city')} />
            {errors.city && (
              <p className="text-sm text-destructive">{errors.city.message}</p>
            )}
          </div>
        </CardContent>
      </Card>

      {/* Sección: Características */}
      <Card>
        <CardHeader>
          <CardTitle>Características</CardTitle>
        </CardHeader>
        <CardContent className="grid grid-cols-1 md:grid-cols-3 gap-4">
          <div className="space-y-2">
            <Label htmlFor="bedrooms">Habitaciones</Label>
            <Input
              id="bedrooms"
              type="number"
              min="0"
              {...register('bedrooms', { valueAsNumber: true })}
            />
            {errors.bedrooms && (
              <p className="text-sm text-destructive">{errors.bedrooms.message}</p>
            )}
          </div>

          <div className="space-y-2">
            <Label htmlFor="bathrooms">Baños</Label>
            <Input
              id="bathrooms"
              type="number"
              min="0"
              {...register('bathrooms', { valueAsNumber: true })}
            />
            {errors.bathrooms && (
              <p className="text-sm text-destructive">{errors.bathrooms.message}</p>
            )}
          </div>

          <div className="space-y-2">
            <Label htmlFor="area">Área (m²)</Label>
            <Input
              id="area"
              type="number"
              min="1"
              {...register('area', { valueAsNumber: true })}
            />
            {errors.area && (
              <p className="text-sm text-destructive">{errors.area.message}</p>
            )}
          </div>
        </CardContent>
      </Card>

      {/* Botón de envío */}

      <Button type="submit" size="lg" className="w-full" disabled={isSubmitting}>
        {isSubmitting ? 'Guardando...' : 'Guardar Propiedad'}
      </Button>
    </form>
  );
}
