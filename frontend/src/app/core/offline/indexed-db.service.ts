import { Injectable } from '@angular/core';
import { PendingOperation } from './offline.model';

@Injectable({
  providedIn: 'root'
})
export class IndexedDbService {
  private dbName = 'optimiam_pwa_db';
  private dbVersion = 1;
  private db: IDBDatabase | null = null;
  private isReadyPromise: Promise<IDBDatabase>;

  constructor() {
    this.isReadyPromise = this.initDb();
  }

  private initDb(): Promise<IDBDatabase> {
    return new Promise((resolve, reject) => {
      if (typeof window === 'undefined' || !window.indexedDB) {
        return reject(new Error('IndexedDB non supporté'));
      }

      const request = window.indexedDB.open(this.dbName, this.dbVersion);

      request.onupgradeneeded = (event: IDBVersionChangeEvent) => {
        const db = (event.target as IDBOpenDBRequest).result;
        if (!db.objectStoreNames.contains('pending_operations')) {
          db.createObjectStore('pending_operations', { keyPath: 'id' });
        }
        if (!db.objectStoreNames.contains('cached_data')) {
          db.createObjectStore('cached_data', { keyPath: 'key' });
        }
      };

      request.onsuccess = () => {
        this.db = request.result;
        resolve(request.result);
      };

      request.onerror = () => {
        reject(request.error);
      };
    });
  }

  async addPendingOperation(op: PendingOperation): Promise<void> {
    const db = await this.isReadyPromise;
    return new Promise((resolve, reject) => {
      const tx = db.transaction('pending_operations', 'readwrite');
      const store = tx.objectStore('pending_operations');
      const req = store.put(op);
      req.onsuccess = () => resolve();
      req.onerror = () => reject(req.error);
    });
  }

  async getPendingOperations(): Promise<PendingOperation[]> {
    const db = await this.isReadyPromise;
    return new Promise((resolve, reject) => {
      const tx = db.transaction('pending_operations', 'readonly');
      const store = tx.objectStore('pending_operations');
      const req = store.getAll();
      req.onsuccess = () => resolve(req.result || []);
      req.onerror = () => reject(req.error);
    });
  }

  async removePendingOperation(id: string): Promise<void> {
    const db = await this.isReadyPromise;
    return new Promise((resolve, reject) => {
      const tx = db.transaction('pending_operations', 'readwrite');
      const store = tx.objectStore('pending_operations');
      const req = store.delete(id);
      req.onsuccess = () => resolve();
      req.onerror = () => reject(req.error);
    });
  }

  async clearPendingOperations(): Promise<void> {
    const db = await this.isReadyPromise;
    return new Promise((resolve, reject) => {
      const tx = db.transaction('pending_operations', 'readwrite');
      const store = tx.objectStore('pending_operations');
      const req = store.clear();
      req.onsuccess = () => resolve();
      req.onerror = () => reject(req.error);
    });
  }

  async cacheData(key: string, data: any): Promise<void> {
    const db = await this.isReadyPromise;
    return new Promise((resolve, reject) => {
      const tx = db.transaction('cached_data', 'readwrite');
      const store = tx.objectStore('cached_data');
      const req = store.put({ key, data, updatedAt: Date.now() });
      req.onsuccess = () => resolve();
      req.onerror = () => reject(req.error);
    });
  }

  async getCachedData<T>(key: string): Promise<T | null> {
    const db = await this.isReadyPromise;
    return new Promise((resolve, reject) => {
      const tx = db.transaction('cached_data', 'readonly');
      const store = tx.objectStore('cached_data');
      const req = store.get(key);
      req.onsuccess = () => resolve(req.result ? req.result.data : null);
      req.onerror = () => reject(req.error);
    });
  }
}
