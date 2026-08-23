export interface LoginRequest {
  email: string;
  password?: string;
}

export interface RegisterRequest {
  username: string;
  email: string;
  password?: string;
}

export interface User {
  id: string;
  email: string;
  username: string;
  role: 'ROLE_USER' | 'ROLE_MANAGER' | 'ROLE_ADMIN';
  maxPrepTimeMinutes?: number;
  vegetarian?: boolean;
  vegan?: boolean;
  glutenFree?: boolean;
}

export interface AuthResponse {
  token: string;
  tokenType: string;
  user: User;
}

export interface UpdatePreferencesRequest {
  maxPrepTimeMinutes?: number;
  vegetarian?: boolean;
  vegan?: boolean;
  glutenFree?: boolean;
}
