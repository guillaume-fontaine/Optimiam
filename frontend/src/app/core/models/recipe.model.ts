import { Product, Unit } from './product.model';

export type Difficulty = 'EASY' | 'MEDIUM' | 'HARD';

export interface DifficultyOption {
  value: Difficulty;
  label: string;
}

export const DIFFICULTY_OPTIONS: DifficultyOption[] = [
  { value: 'EASY', label: 'Facile' },
  { value: 'MEDIUM', label: 'Moyen' },
  { value: 'HARD', label: 'Difficile' }
];

export interface Nutrition {
  calories?: number;
  protein?: number;
  carbohydrates?: number;
  fat?: number;
  fiber?: number;
  salt?: number;
}

export interface RecipeIngredient {
  id?: string;
  product: Product;
  quantity: number;
  unit: Unit;
  unitLabel?: string;
  unitSymbol?: string;
  optional: boolean;
}

export interface RecipeStep {
  id?: string;
  stepNumber: number;
  instruction: string;
  durationMinutes?: number;
}

export interface Recipe {
  id: string;
  name: string;
  description?: string;
  preparationTimeMinutes: number;
  cookingTimeMinutes: number;
  totalTimeMinutes: number;
  difficulty: Difficulty;
  difficultyLabel?: string;
  servings: number;
  imageUrl?: string;
  nutrition?: Nutrition;
  ingredients: RecipeIngredient[];
  steps: RecipeStep[];
  tags: string[];
  createdAt: string;
  updatedAt: string;
}

export interface CreateRecipeIngredientRequest {
  productId: string;
  quantity: number;
  unit: Unit;
  optional?: boolean;
}

export interface CreateRecipeStepRequest {
  stepNumber: number;
  instruction: string;
  durationMinutes?: number;
}

export interface CreateRecipeRequest {
  name: string;
  description?: string;
  preparationTimeMinutes?: number;
  cookingTimeMinutes?: number;
  difficulty?: Difficulty;
  servings?: number;
  imageUrl?: string;
  nutrition?: Nutrition;
  ingredients: CreateRecipeIngredientRequest[];
  steps?: CreateRecipeStepRequest[];
  tags?: string[];
}

export interface UpdateRecipeRequest extends CreateRecipeRequest {}
