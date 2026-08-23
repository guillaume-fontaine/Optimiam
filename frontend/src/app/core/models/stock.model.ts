import { Product, Unit } from './product.model';

export type Location = 'FRIDGE' | 'FREEZER' | 'PANTRY' | 'OTHER';

export interface LocationOption {
  value: Location;
  label: string;
  icon: string;
}

export const LOCATION_OPTIONS: LocationOption[] = [
  { value: 'FRIDGE', label: 'Réfrigérateur', icon: 'kitchen' },
  { value: 'FREEZER', label: 'Congélateur', icon: 'ac_unit' },
  { value: 'PANTRY', label: 'Placard / Garde-manger', icon: 'inventory' },
  { value: 'OTHER', label: 'Autre', icon: 'shelves' }
];

export type StockStatus = 'AVAILABLE' | 'EXPIRING_SOON' | 'EXPIRED' | 'CONSUMED' | 'DISCARDED';

export type TransactionType = 'ENTRY' | 'EXIT' | 'CONSUMPTION' | 'LOSS' | 'ADJUSTMENT';

export type LossReason = 'EXPIRED' | 'SPOILED' | 'OVERCOOKED' | 'DAMAGED' | 'OTHER';

export interface LossReasonOption {
  value: LossReason;
  label: string;
}

export const LOSS_REASON_OPTIONS: LossReasonOption[] = [
  { value: 'EXPIRED', label: 'Date de péremption dépassée (DLC)' },
  { value: 'SPOILED', label: 'Produit abîmé ou moisi' },
  { value: 'OVERCOOKED', label: 'Surplus non consommé / Trop préparé' },
  { value: 'DAMAGED', label: 'Emballage détérioré' },
  { value: 'OTHER', label: 'Autre motif' }
];

export interface StockItem {
  id: string;
  product: Product;
  quantity: number;
  unit: Unit;
  unitLabel?: string;
  unitSymbol?: string;
  entryDate: string;
  expirationDate?: string;
  daysUntilExpiration?: number;
  location: Location;
  locationLabel?: string;
  locationIcon?: string;
  status: StockStatus;
  statusLabel?: string;
  version: number;
  createdAt: string;
  updatedAt: string;
}

export interface CreateStockEntryRequest {
  productId: string;
  quantity: number;
  unit?: Unit;
  entryDate?: string;
  expirationDate?: string;
  location?: Location;
  deviceId?: string;
}

export interface StockExitRequest {
  quantity: number;
  reason?: string;
  deviceId?: string;
}

export interface StockLossRequest {
  quantity: number;
  lossReason: LossReason;
  comment?: string;
  deviceId?: string;
}

export interface StockSummary {
  totalAvailableItems: number;
  expiringSoonItems: number;
  expiredItems: number;
  totalLossesWeightKg: number;
  totalLossesCount: number;
}

export interface StockTransaction {
  id: string;
  stockItemId?: string;
  product: Product;
  type: TransactionType;
  typeLabel?: string;
  quantity: number;
  unit: Unit;
  unitSymbol?: string;
  lossReason?: LossReason;
  lossReasonLabel?: string;
  reason?: string;
  deviceId?: string;
  timestamp: string;
}

export interface LossStatistics {
  totalLossWeightKg: number;
  totalLossOperations: number;
  lossesByReason: Record<LossReason, number>;
  topLostProducts: {
    product: Product;
    totalQuantity: number;
    unitSymbol: string;
    occurrences: number;
  }[];
}
