import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_CONFIG } from '../http/api-config';
import { PageResponse } from '../models/product.model';
import { CreateRecipeRequest, Difficulty, Recipe, UpdateRecipeRequest } from '../models/recipe.model';

@Injectable({
  providedIn: 'root'
})
export class RecipeService {
  private http = inject(HttpClient);
  private apiUrl = `${API_CONFIG.baseUrl}/recipes`;

  getRecipes(
    query?: string,
    tag?: string,
    maxPrepTime?: number,
    difficulty?: Difficulty,
    page = 0,
    size = 20,
    sortBy = 'name',
    direction = 'asc'
  ): Observable<PageResponse<Recipe>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sortBy', sortBy)
      .set('direction', direction);

    if (query && query.trim()) {
      params = params.set('query', query.trim());
    }
    if (tag && tag.trim()) {
      params = params.set('tag', tag.trim());
    }
    if (maxPrepTime) {
      params = params.set('maxPrepTime', maxPrepTime.toString());
    }
    if (difficulty) {
      params = params.set('difficulty', difficulty);
    }

    return this.http.get<PageResponse<Recipe>>(this.apiUrl, { params });
  }

  getAllRecipes(): Observable<Recipe[]> {
    return this.http.get<Recipe[]>(`${this.apiUrl}/all`);
  }

  getRecipeById(id: string): Observable<Recipe> {
    return this.http.get<Recipe>(`${this.apiUrl}/${id}`);
  }

  createRecipe(request: CreateRecipeRequest): Observable<Recipe> {
    return this.http.post<Recipe>(this.apiUrl, request);
  }

  updateRecipe(id: string, request: UpdateRecipeRequest): Observable<Recipe> {
    return this.http.put<Recipe>(`${this.apiUrl}/${id}`, request);
  }

  deleteRecipe(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
