import { Product } from './product.model';
import { StockItem } from './stock.model';

export interface ScaleMeasurement {
  weight: number;
  unit: string;
  stable: boolean;
  timestamp: string;
}

export interface SimulateWeightRequest {
  weight: number;
  unit?: string;
  stable?: boolean;
}

export interface PrintLabelDto {
  productName: string;
  barcode?: string;
  entryDate?: string;
  expirationDate?: string;
  quantityWithUnit: string;
  location?: string;
}

export interface PrintJob {
  jobId: string;
  productName: string;
  barcode?: string;
  entryDate?: string;
  expirationDate?: string;
  quantityWithUnit: string;
  location?: string;
  labelContent: string;
  printedAt: string;
  success: boolean;
}

export interface ScanResult {
  rawBarcode: string;
  productFound: boolean;
  matchedProduct?: Product;
  matchingStockItems?: StockItem[];
  scannedAt: string;
  message: string;
}
