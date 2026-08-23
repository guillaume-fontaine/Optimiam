import { Product } from './product.model';
import { Difficulty, Recipe } from './recipe.model';

export interface MissingIngredient {
  product: Product;
  requiredQuantity: number;
  availableQuantity: number;
  missingQuantity: number;
  unitSymbol: string;
}

export interface Recommendation {
  recipe: Recipe;
  score: number;
  matchPercentage: number;
  urgencyScore: number;
  reasons: string[];
  totalIngredientsCount: number;
  availableIngredientsCount: number;
  missingIngredientsCount: number;
  missingIngredients: MissingIngredient[];
  expiringIngredientsUsed: string[];
  fullyAvailableInStock: boolean;
}

export interface RecommendationRequest {
  servings?: number;
  maxTimeMinutes?: number;
  difficulty?: Difficulty;
  tag?: string;
  onlyFullStock?: boolean;
}
