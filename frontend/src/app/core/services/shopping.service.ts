import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_CONFIG } from '../http/api-config';
import { AddShoppingItemRequest, GenerateShoppingListRequest, ShoppingList, ShoppingListStatus, UpdateShoppingItemRequest } from '../models/shopping.model';

@Injectable({
  providedIn: 'root'
})
export class ShoppingService {
  private http = inject(HttpClient);
  private apiUrl = `${API_CONFIG.baseUrl}/shopping-lists`;

  getActiveShoppingList(): Observable<ShoppingList> {
    return this.http.get<ShoppingList>(`${this.apiUrl}/active`);
  }

  getAllShoppingLists(status?: ShoppingListStatus): Observable<ShoppingList[]> {
    let params = new HttpParams();
    if (status) {
      params = params.set('status', status);
    }
    return this.http.get<ShoppingList[]>(this.apiUrl, { params });
  }

  getShoppingListById(id: string): Observable<ShoppingList> {
    return this.http.get<ShoppingList>(`${this.apiUrl}/${id}`);
  }

  generateFromPlanning(request?: GenerateShoppingListRequest): Observable<ShoppingList> {
    return this.http.post<ShoppingList>(`${this.apiUrl}/generate`, request || {});
  }

  addItem(listId: string, request: AddShoppingItemRequest): Observable<ShoppingList> {
    return this.http.post<ShoppingList>(`${this.apiUrl}/${listId}/items`, request);
  }

  updateItem(listId: string, itemId: string, request: UpdateShoppingItemRequest): Observable<ShoppingList> {
    return this.http.put<ShoppingList>(`${this.apiUrl}/${listId}/items/${itemId}`, request);
  }

  validatePurchases(listId: string): Observable<ShoppingList> {
    return this.http.post<ShoppingList>(`${this.apiUrl}/${listId}/validate-purchases`, {});
  }

  deleteShoppingList(listId: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${listId}`);
  }
}
