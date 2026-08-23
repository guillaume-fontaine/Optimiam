import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable } from 'rxjs';
import { API_CONFIG } from '../http/api-config';
import { IndexedDbService } from './indexed-db.service';
import { PendingOperation, SyncBatchResponse, SyncOperationType, SyncStatus } from './offline.model';
import { NotificationService } from '../services/notification.service';

@Injectable({
  providedIn: 'root'
})
export class SyncManagerService {
  private http = inject(HttpClient);
  private indexedDb = inject(IndexedDbService);
  private notificationService = inject(NotificationService);

  private isOnlineSubject = new BehaviorSubject<boolean>(typeof navigator !== 'undefined' ? navigator.onLine : true);
  private syncStatusSubject = new BehaviorSubject<SyncStatus>('ONLINE');
  private pendingCountSubject = new BehaviorSubject<number>(0);
  private conflictCountSubject = new BehaviorSubject<number>(0);

  isOnline$: Observable<boolean> = this.isOnlineSubject.asObservable();
  syncStatus$: Observable<SyncStatus> = this.syncStatusSubject.asObservable();
  pendingCount$: Observable<number> = this.pendingCountSubject.asObservable();
  conflictCount$: Observable<number> = this.conflictCountSubject.asObservable();

  constructor() {
    this.initNetworkListeners();
    this.refreshPendingCount();
  }

  private initNetworkListeners(): void {
    if (typeof window !== 'undefined') {
      window.addEventListener('online', () => {
        this.isOnlineSubject.next(true);
        this.notificationService.info('🌐 Connexion rétablie : synchronisation des données...');
        this.triggerSync();
      });

      window.addEventListener('offline', () => {
        this.isOnlineSubject.next(false);
        this.syncStatusSubject.next('OFFLINE');
        this.notificationService.warning('📡 Mode hors-ligne activé. Vos modifications seront sauvegardées localement.');
      });
    }
  }

  async refreshPendingCount(): Promise<void> {
    try {
      const ops = await this.indexedDb.getPendingOperations();
      this.pendingCountSubject.next(ops.length);
      const conflicts = ops.filter(o => o.status === 'CONFLICT').length;
      this.conflictCountSubject.next(conflicts);

      if (conflicts > 0) {
        this.syncStatusSubject.next('CONFLICT');
      } else if (!this.isOnlineSubject.value) {
        this.syncStatusSubject.next('OFFLINE');
      } else {
        this.syncStatusSubject.next('ONLINE');
      }
    } catch {
      // Ignorer si indexeddb pas encore initialisé
    }
  }

  async queueOperation(type: SyncOperationType, entityId?: string, payload?: any): Promise<void> {
    const op: PendingOperation = {
      id: crypto.randomUUID(),
      type,
      entityId,
      payload: payload || {},
      timestamp: Date.now(),
      status: 'PENDING',
      retryCount: 0
    };

    await this.indexedDb.addPendingOperation(op);
    await this.refreshPendingCount();

    if (this.isOnlineSubject.value) {
      this.triggerSync();
    }
  }

  async triggerSync(): Promise<void> {
    if (!this.isOnlineSubject.value) {
      this.syncStatusSubject.next('OFFLINE');
      return;
    }

    const pendingOps = await this.indexedDb.getPendingOperations();
    if (pendingOps.length === 0) {
      this.syncStatusSubject.next('ONLINE');
      return;
    }

    this.syncStatusSubject.next('SYNCING');

    const batchPayload = {
      clientId: 'pwa-client-session',
      operations: pendingOps.map(op => ({
        operationId: op.id,
        type: op.type,
        entityId: op.entityId,
        payload: op.payload,
        timestamp: new Date(op.timestamp).toISOString()
      }))
    };

    this.http.post<SyncBatchResponse>(`${API_CONFIG.baseUrl}/sync/batch`, batchPayload).subscribe({
      next: async (response) => {
        let conflictsCount = 0;

        for (const res of response.results) {
          if (res.success) {
            await this.indexedDb.removePendingOperation(res.operationId);
          } else if (res.conflict) {
            conflictsCount++;
            const op = pendingOps.find(o => o.id === res.operationId);
            if (op) {
              op.status = 'CONFLICT';
              op.errorMessage = res.message;
              await this.indexedDb.addPendingOperation(op);
            }
          }
        }

        await this.refreshPendingCount();

        if (conflictsCount > 0) {
          this.syncStatusSubject.next('CONFLICT');
          this.notificationService.error(`⚠️ Synchronisation : ${conflictsCount} conflit(s) détecté(s)`);
        } else {
          this.syncStatusSubject.next('ONLINE');
          this.notificationService.success(`✅ ${response.syncedCount} opération(s) synchronisée(s) avec succès`);
        }
      },
      error: () => {
        this.syncStatusSubject.next('OFFLINE');
      }
    });
  }
}
