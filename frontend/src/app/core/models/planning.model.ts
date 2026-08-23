import { Recipe } from './recipe.model';

export type MealType = 'BREAKFAST' | 'LUNCH' | 'SNACK' | 'DINNER';

export interface MealTypeOption {
  value: MealType;
  label: string;
  icon: string;
}

export const MEAL_TYPE_OPTIONS: MealTypeOption[] = [
  { value: 'BREAKFAST', label: 'Petit-déjeuner', icon: 'free_breakfast' },
  { value: 'LUNCH', label: 'Déjeuner (Midi)', icon: 'wb_sunny' },
  { value: 'SNACK', label: 'Goûter / Collation', icon: 'cookie' },
  { value: 'DINNER', label: 'Dîner (Soir)', icon: 'nights_stay' }
];

export type MealPlanStatus = 'PLANNED' | 'COOKED' | 'CANCELLED';

export interface MealPlan {
  id: string;
  date: string;
  mealType: MealType;
  mealTypeLabel?: string;
  mealTypeIcon?: string;
  recipe: Recipe;
  servings: number;
  status: MealPlanStatus;
  statusLabel?: string;
  notes?: string;
  createdAt: string;
  updatedAt: string;
}

export interface CreateMealPlanRequest {
  date: string;
  mealType: MealType;
  recipeId: string;
  servings?: number;
  notes?: string;
}

export interface UpdateMealPlanRequest {
  date: string;
  mealType: MealType;
  recipeId: string;
  servings?: number;
  status?: MealPlanStatus;
  notes?: string;
}
