import type React from 'react';
import { GitCompareArrows } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { useCompare } from '@/context/CompareContext';
import type { Property } from '@/types/property';

interface CompareButtonProps {
  property: Property;
}

export function CompareButton({ property }: CompareButtonProps): React.ReactElement {
  const { compareList, addToCompare, removeFromCompare } = useCompare();

  const isSelected = compareList.some((p) => p.id === property.id);
  const isFull = compareList.length >= 3 && !isSelected;

  const handleClick = () => {
    if (isSelected) {
      removeFromCompare(property.id);
    } else {
      addToCompare(property);
    }
  };

  return (
    <Button
      variant={isSelected ? 'default' : 'outline'}
      size="sm"
      onClick={handleClick}
      disabled={isFull}
      title={isFull ? 'Máximo 3 propiedades para comparar' : undefined}
      className="flex-1"
    >
      <GitCompareArrows className="h-4 w-4 mr-1" />
      {isSelected ? 'Comparando' : 'Comparar'}
    </Button>
  );
}
