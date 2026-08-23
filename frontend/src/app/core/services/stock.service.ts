import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_CONFIG } from '../http/api-config';
import { PageResponse } from '../models/product.model';
import {
  CreateStockEntryRequest,
  Location,
  LossStatistics,
  StockExitRequest,
  StockItem,
  StockLossRequest,
  StockSummary,
  StockTransaction,
  TransactionType
} from '../models/stock.model';

@Injectable({
  providedIn: 'root'
})
export class StockService {
  private http = inject(HttpClient);
  private stockApiUrl = `${API_CONFIG.baseUrl}/stock`;
  private transactionApiUrl = `${API_CONFIG.baseUrl}/transactions`;

  getStockItems(
    location?: Location,
    query?: string,
    page = 0,
    size = 20,
    sortBy = 'expirationDate',
    direction = 'asc'
  ): Observable<PageResponse<StockItem>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sortBy', sortBy)
      .set('direction', direction);

    if (location) {
      params = params.set('location', location);
    }
    if (query && query.trim()) {
      params = params.set('query', query.trim());
    }

    return this.http.get<PageResponse<StockItem>>(this.stockApiUrl, { params });
  }

  getStockSummary(): Observable<StockSummary> {
    return this.http.get<StockSummary>(`${this.stockApiUrl}/summary`);
  }

  getExpiringStock(daysAhead = 3): Observable<StockItem[]> {
    const params = new HttpParams().set('daysAhead', daysAhead.toString());
    return this.http.get<StockItem[]>(`${this.stockApiUrl}/expiring`, { params });
  }

  createStockEntry(request: CreateStockEntryRequest): Observable<StockItem> {
    return this.http.post<StockItem>(`${this.stockApiUrl}/entries`, request);
  }

  exitStock(id: string, request: StockExitRequest): Observable<StockItem> {
    return this.http.post<StockItem>(`${this.stockApiUrl}/${id}/exits`, request);
  }

  recordLoss(id: string, request: StockLossRequest): Observable<StockItem> {
    return this.http.post<StockItem>(`${this.stockApiUrl}/${id}/losses`, request);
  }

  deleteStockItem(id: string): Observable<void> {
    return this.http.delete<void>(`${this.stockApiUrl}/${id}`);
  }

  getTransactions(type?: TransactionType, page = 0, size = 20): Observable<PageResponse<StockTransaction>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    if (type) {
      params = params.set('type', type);
    }

    return this.http.get<PageResponse<StockTransaction>>(this.transactionApiUrl, { params });
  }

  getLossStatistics(days = 30): Observable<LossStatistics> {
    const params = new HttpParams().set('days', days.toString());
    return this.http.get<LossStatistics>(`${this.transactionApiUrl}/losses/stats`, { params });
  }
}
