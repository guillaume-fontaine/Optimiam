export type SyncStatus = 'ONLINE' | 'OFFLINE' | 'SYNCING' | 'CONFLICT';

export type SyncOperationType = 
  | 'CREATE_STOCK_ENTRY'
  | 'EXIT_STOCK'
  | 'RECORD_LOSS'
  | 'COOK_MEAL'
  | 'UPDATE_SHOPPING_ITEM'
  | 'GENERIC_MUTATION';

export interface PendingOperation {
  id: string;
  type: SyncOperationType;
  entityId?: string;
  payload: any;
  timestamp: number;
  status: 'PENDING' | 'SYNCING' | 'FAILED' | 'CONFLICT';
  retryCount: number;
  errorMessage?: string;
}

export interface SyncBatchResponse {
  totalOperations: number;
  syncedCount: number;
  conflictCount: number;
  results: {
    operationId: string;
    success: boolean;
    conflict: boolean;
    message: string;
    resultEntity?: any;
  }[];
  serverTimestamp: string;
}
