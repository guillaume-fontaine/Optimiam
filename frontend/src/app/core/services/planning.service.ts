import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_CONFIG } from '../http/api-config';
import { CreateMealPlanRequest, MealPlan, UpdateMealPlanRequest } from '../models/planning.model';

@Injectable({
  providedIn: 'root'
})
export class PlanningService {
  private http = inject(HttpClient);
  private apiUrl = `${API_CONFIG.baseUrl}/planning`;

  getMealPlans(startDate?: string, endDate?: string): Observable<MealPlan[]> {
    let params = new HttpParams();
    if (startDate) {
      params = params.set('startDate', startDate);
    }
    if (endDate) {
      params = params.set('endDate', endDate);
    }
    return this.http.get<MealPlan[]>(this.apiUrl, { params });
  }

  getMealPlanById(id: string): Observable<MealPlan> {
    return this.http.get<MealPlan>(`${this.apiUrl}/${id}`);
  }

  createMealPlan(request: CreateMealPlanRequest): Observable<MealPlan> {
    return this.http.post<MealPlan>(this.apiUrl, request);
  }

  updateMealPlan(id: string, request: UpdateMealPlanRequest): Observable<MealPlan> {
    return this.http.put<MealPlan>(`${this.apiUrl}/${id}`, request);
  }

  markAsCooked(id: string, deductStock = true): Observable<MealPlan> {
    const params = new HttpParams().set('deductStock', deductStock.toString());
    return this.http.post<MealPlan>(`${this.apiUrl}/${id}/cook`, {}, { params });
  }

  deleteMealPlan(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
