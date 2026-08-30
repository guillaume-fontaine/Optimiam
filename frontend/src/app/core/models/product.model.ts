import { Location, LocationOption, LOCATION_OPTIONS } from './stock.model';

export type Unit = 'KG' | 'G' | 'L' | 'ML' | 'PIECE';

export interface UnitOption {
  value: Unit;
  label: string;
  symbol: string;
}

export const UNIT_OPTIONS: UnitOption[] = [
  { value: 'KG', label: 'Kilogramme', symbol: 'kg' },
  { value: 'G', label: 'Gramme', symbol: 'g' },
  { value: 'L', label: 'Litre', symbol: 'L' },
  { value: 'ML', label: 'Millilitre', symbol: 'ml' },
  { value: 'PIECE', label: 'Pièce', symbol: 'pièce' }
];

// Re-export Location types and options from stock.model
export type { Location, LocationOption };
export { LOCATION_OPTIONS };

export interface Category {
  id: string;
  name: string;
  icon?: string;
  color?: string;
  description?: string;
  createdAt: string;
  updatedAt: string;
}

export interface CreateCategoryRequest {
  name: string;
  icon?: string;
  color?: string;
  description?: string;
}

export interface UpdateCategoryRequest {
  name: string;
  icon?: string;
  color?: string;
  description?: string;
}

export interface Product {
  id: string;
  name: string;
  barcode?: string;
  defaultUnit: Unit;
  unitLabel?: string;
  unitSymbol?: string;
  defaultLocation?: Location;
  defaultLocationLabel?: string;
  defaultLocationIcon?: string;
  category?: Category;
  averageShelfLifeDays?: number;
  imageUrl?: string;
  createdAt: string;
  updatedAt: string;
}

export interface CreateProductRequest {
  name: string;
  barcode?: string;
  defaultUnit: Unit;
  defaultLocation?: Location;
  categoryId?: string;
  averageShelfLifeDays?: number;
  imageUrl?: string;
}

export interface UpdateProductRequest {
  name: string;
  barcode?: string;
  defaultUnit: Unit;
  defaultLocation?: Location;
  categoryId?: string;
  averageShelfLifeDays?: number;
  imageUrl?: string;
}

export interface PageResponse<T> {
  content: T[];
  pageNumber: number;
  pageSize: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
  first: boolean;
}
