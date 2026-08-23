import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_CONFIG } from '../http/api-config';
import { Recommendation, RecommendationRequest } from '../models/recommendation.model';

@Injectable({
  providedIn: 'root'
})
export class RecommendationService {
  private http = inject(HttpClient);
  private apiUrl = `${API_CONFIG.baseUrl}/recommendations`;

  getRecommendations(request?: RecommendationRequest): Observable<Recommendation[]> {
    return this.http.post<Recommendation[]>(this.apiUrl, request || {});
  }

  getDailyRecommendations(limit = 3): Observable<Recommendation[]> {
    const params = new HttpParams().set('limit', limit.toString());
    return this.http.get<Recommendation[]>(`${this.apiUrl}/daily`, { params });
  }
}
