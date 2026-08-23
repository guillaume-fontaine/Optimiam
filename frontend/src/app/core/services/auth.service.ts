import { Injectable, inject, signal, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { Router } from '@angular/router';
import { API_CONFIG } from '../http/api-config';
import { AuthResponse, LoginRequest, RegisterRequest, UpdatePreferencesRequest, User } from '../models/auth.model';
import { NotificationService } from './notification.service';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private http = inject(HttpClient);
  private router = inject(Router);
  private notificationService = inject(NotificationService);

  private readonly TOKEN_KEY = 'optimiam_auth_token';
  private readonly USER_KEY = 'optimiam_auth_user';

  readonly currentUser = signal<User | null>(this.getStoredUser());
  readonly isAuthenticated = computed(() => !!this.currentUser());

  private getStoredUser(): User | null {
    if (typeof localStorage === 'undefined') return null;
    const stored = localStorage.getItem(this.USER_KEY);
    return stored ? JSON.parse(stored) : {
      id: 'demo-id',
      email: 'demo@optimiam.fr',
      username: 'Utilisateur Démo',
      role: 'ROLE_USER',
      maxPrepTimeMinutes: 30
    };
  }

  getToken(): string | null {
    if (typeof localStorage === 'undefined') return null;
    return localStorage.getItem(this.TOKEN_KEY);
  }

  login(request: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${API_CONFIG.baseUrl}/auth/login`, request).pipe(
      tap((res) => {
        this.saveSession(res);
        this.notificationService.success(`👋 Bienvenue, ${res.user.username} !`);
      })
    );
  }

  register(request: RegisterRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${API_CONFIG.baseUrl}/auth/register`, request).pipe(
      tap((res) => {
        this.saveSession(res);
        this.notificationService.success(`🎉 Compte créé ! Bienvenue ${res.user.username} !`);
      })
    );
  }

  updatePreferences(prefs: UpdatePreferencesRequest): Observable<User> {
    return this.http.put<User>(`${API_CONFIG.baseUrl}/auth/preferences`, prefs).pipe(
      tap((updated) => {
        this.currentUser.set(updated);
        localStorage.setItem(this.USER_KEY, JSON.stringify(updated));
        this.notificationService.success('⚙️ Vos préférences ont été enregistrées !');
      })
    );
  }

  logout(): void {
    if (typeof localStorage !== 'undefined') {
      localStorage.removeItem(this.TOKEN_KEY);
      localStorage.removeItem(this.USER_KEY);
    }
    this.currentUser.set(null);
    this.notificationService.info('À bientôt sur OptiMiam !');
    this.router.navigate(['/login']);
  }

  private saveSession(res: AuthResponse): void {
    if (typeof localStorage !== 'undefined') {
      localStorage.setItem(this.TOKEN_KEY, res.token);
      localStorage.setItem(this.USER_KEY, JSON.stringify(res.user));
    }
    this.currentUser.set(res.user);
  }
}
