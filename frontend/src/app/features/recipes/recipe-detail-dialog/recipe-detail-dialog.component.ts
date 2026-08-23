import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatDialogModule, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatDividerModule } from '@angular/material/divider';
import { MatTooltipModule } from '@angular/material/tooltip';
import { Recipe } from '../../../core/models/recipe.model';
import { StockService } from '../../../core/services/stock.service';
import { NotificationService } from '../../../core/services/notification.service';

export interface RecipeDetailDialogData {
  recipe: Recipe;
}

@Component({
  selector: 'app-recipe-detail-dialog',
  standalone: true,
  imports: [
    CommonModule,
    MatDialogModule,
    MatButtonModule,
    MatIconModule,
    MatChipsModule,
    MatDividerModule,
    MatTooltipModule
  ],
  template: `
    <div class="dialog-header">
      <div>
        <div class="tags-container" *ngIf="recipe.tags && recipe.tags.length > 0">
          <span class="recipe-tag" *ngFor="let tag of recipe.tags">{{ tag }}</span>
        </div>
        <h2 class="recipe-title">{{ recipe.name }}</h2>
        <p class="recipe-desc" *ngIf="recipe.description">{{ recipe.description }}</p>
      </div>
      <button mat-icon-button mat-dialog-close class="close-btn">
        <mat-icon>close</mat-icon>
      </button>
    </div>

    <mat-dialog-content class="dialog-content">
      <!-- Métriques de la recette -->
      <div class="metrics-bar">
        <div class="metric-item">
          <mat-icon color="primary">schedule</mat-icon>
          <div class="metric-text">
            <span class="metric-label">Préparation</span>
            <span class="metric-val">{{ recipe.preparationTimeMinutes }} min</span>
          </div>
        </div>
        <div class="metric-item" *ngIf="recipe.cookingTimeMinutes > 0">
          <mat-icon color="warn">local_fire_department</mat-icon>
          <div class="metric-text">
            <span class="metric-label">Cuisson</span>
            <span class="metric-val">{{ recipe.cookingTimeMinutes }} min</span>
          </div>
        </div>
        <div class="metric-item">
          <mat-icon color="accent">psychology</mat-icon>
          <div class="metric-text">
            <span class="metric-label">Difficulté</span>
            <span class="metric-val">{{ recipe.difficultyLabel }}</span>
          </div>
        </div>
        <div class="metric-item servings-adjuster">
          <mat-icon color="primary">people</mat-icon>
          <div class="metric-text">
            <span class="metric-label">Portions</span>
            <div class="portions-controls">
              <button mat-icon-button class="small-btn" (click)="adjustServings(-1)" [disabled]="currentServings <= 1">
                <mat-icon>remove</mat-icon>
              </button>
              <span class="metric-val">{{ currentServings }}</span>
              <button mat-icon-button class="small-btn" (click)="adjustServings(1)">
                <mat-icon>add</mat-icon>
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- Informations nutritionnelles par portion -->
      <div class="nutrition-card" *ngIf="recipe.nutrition">
        <div class="nutrition-title">
          <mat-icon color="primary">favorite</mat-icon>
          <span>Valeurs nutritionnelles (par portion)</span>
        </div>
        <div class="nutrition-grid">
          <div class="nutri-item" *ngIf="recipe.nutrition.calories">
            <span class="nutri-val">{{ recipe.nutrition.calories }}</span>
            <span class="nutri-unit">kcal</span>
          </div>
          <div class="nutri-item" *ngIf="recipe.nutrition.protein">
            <span class="nutri-val">{{ recipe.nutrition.protein }}g</span>
            <span class="nutri-unit">Protéines</span>
          </div>
          <div class="nutri-item" *ngIf="recipe.nutrition.carbohydrates">
            <span class="nutri-val">{{ recipe.nutrition.carbohydrates }}g</span>
            <span class="nutri-unit">Glucides</span>
          </div>
          <div class="nutri-item" *ngIf="recipe.nutrition.fat">
            <span class="nutri-val">{{ recipe.nutrition.fat }}g</span>
            <span class="nutri-unit">Lipides</span>
          </div>
          <div class="nutri-item" *ngIf="recipe.nutrition.fiber">
            <span class="nutri-val">{{ recipe.nutrition.fiber }}g</span>
            <span class="nutri-unit">Fibres</span>
          </div>
        </div>
      </div>

      <!-- Section Ingrédients -->
      <div class="section-title">
        <mat-icon color="primary">kitchen</mat-icon>
        <h3>Ingrédients requis ({{ recipe.ingredients.length }})</h3>
      </div>
      <div class="ingredients-list">
        <div class="ingredient-row" *ngFor="let ing of recipe.ingredients">
          <div class="ing-left">
            <mat-icon class="bullet-icon">check_circle_outline</mat-icon>
            <span class="ing-name">{{ ing.product.name }}</span>
            <span class="optional-badge" *ngIf="ing.optional">(facultatif)</span>
          </div>
          <div class="ing-qty">
            <span class="qty-number">{{ getScaledQuantity(ing.quantity) }}</span>
            <span class="qty-unit">{{ ing.unitSymbol || ing.unit }}</span>
          </div>
        </div>
      </div>

      <mat-divider class="my-4"></mat-divider>

      <!-- Section Étapes de préparation -->
      <div class="section-title">
        <mat-icon color="primary">format_list_numbered</mat-icon>
        <h3>Étapes de préparation</h3>
      </div>
      <div class="steps-list" *ngIf="recipe.steps && recipe.steps.length > 0">
        <div class="step-item" *ngFor="let step of recipe.steps">
          <div class="step-number">{{ step.stepNumber }}</div>
          <div class="step-content">
            <p class="step-instruction">{{ step.instruction }}</p>
            <span class="step-duration" *ngIf="step.durationMinutes">
              <mat-icon>timer</mat-icon> {{ step.durationMinutes }} min
            </span>
          </div>
        </div>
      </div>
      <div *ngIf="!recipe.steps || recipe.steps.length === 0" class="text-muted">
        Aucune étape détaillée pour cette recette.
      </div>
    </mat-dialog-content>

    <mat-dialog-actions align="end">
      <button mat-button mat-dialog-close>Fermer</button>
      <button mat-raised-button color="primary" (click)="cookRecipe()">
        <mat-icon>restaurant</mat-icon>
        Cuisiner ce plat
      </button>
    </mat-dialog-actions>
  `,
  styles: [`
    .dialog-header {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      padding: 20px 24px 12px 24px;
      .recipe-title {
        margin: 6px 0 4px 0;
        font-size: 1.5rem;
        font-weight: 700;
        color: #0f172a;
      }
      .recipe-desc {
        color: #64748b;
        margin: 0;
        font-size: 0.95rem;
      }
    }
    .tags-container {
      display: flex;
      gap: 6px;
      margin-bottom: 4px;
      flex-wrap: wrap;
      .recipe-tag {
        background-color: #f1f5f9;
        color: #475569;
        font-size: 0.75rem;
        font-weight: 600;
        padding: 2px 8px;
        border-radius: 4px;
      }
    }
    .dialog-content {
      padding: 12px 24px;
      max-height: 75vh;
      min-width: 540px;
    }
    .metrics-bar {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(110px, 1fr));
      gap: 12px;
      background-color: #f8fafc;
      padding: 12px 16px;
      border-radius: 10px;
      margin-bottom: 16px;

      .metric-item {
        display: flex;
        align-items: center;
        gap: 10px;
        .metric-text {
          display: flex;
          flex-direction: column;
          .metric-label { font-size: 0.75rem; color: #64748b; }
          .metric-val { font-weight: 600; font-size: 0.95rem; color: #1e293b; }
        }
      }
      .portions-controls {
        display: flex;
        align-items: center;
        gap: 4px;
        .small-btn {
          width: 24px;
          height: 24px;
          line-height: 24px;
          mat-icon { font-size: 16px; width: 16px; height: 16px; }
        }
      }
    }
    .nutrition-card {
      background-color: #f0fdf4;
      border: 1px solid #bbf7d0;
      border-radius: 8px;
      padding: 12px 16px;
      margin-bottom: 16px;
      .nutrition-title {
        display: flex;
        align-items: center;
        gap: 6px;
        font-size: 0.85rem;
        font-weight: 600;
        color: #166534;
        margin-bottom: 8px;
        mat-icon { font-size: 18px; width: 18px; height: 18px; }
      }
      .nutrition-grid {
        display: flex;
        gap: 16px;
        flex-wrap: wrap;
        .nutri-item {
          display: flex;
          flex-direction: column;
          align-items: center;
          background: #ffffff;
          padding: 6px 12px;
          border-radius: 6px;
          min-width: 60px;
          .nutri-val { font-weight: 700; color: #16a34a; font-size: 0.95rem; }
          .nutri-unit { font-size: 0.75rem; color: #64748b; }
        }
      }
    }
    .section-title {
      display: flex;
      align-items: center;
      gap: 8px;
      margin: 16px 0 8px 0;
      h3 { margin: 0; font-size: 1.1rem; font-weight: 600; color: #1e293b; }
    }
    .ingredients-list {
      display: flex;
      flex-direction: column;
      gap: 8px;
      .ingredient-row {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 8px 12px;
        background-color: #f8fafc;
        border-radius: 6px;
        .ing-left {
          display: flex;
          align-items: center;
          gap: 8px;
          .bullet-icon { font-size: 18px; width: 18px; height: 18px; color: #16a34a; }
          .ing-name { font-weight: 500; color: #1e293b; }
          .optional-badge { font-size: 0.75rem; color: #94a3b8; font-style: italic; }
        }
        .ing-qty {
          font-weight: 700;
          color: #0f172a;
          .qty-unit { font-size: 0.85rem; margin-left: 4px; color: #64748b; }
        }
      }
    }
    .steps-list {
      display: flex;
      flex-direction: column;
      gap: 12px;
      .step-item {
        display: flex;
        gap: 12px;
        align-items: flex-start;
        .step-number {
          width: 28px;
          height: 28px;
          border-radius: 50%;
          background-color: #16a34a;
          color: #ffffff;
          display: flex;
          align-items: center;
          justify-content: center;
          font-weight: 700;
          font-size: 0.85rem;
          flex-shrink: 0;
        }
        .step-content {
          flex: 1;
          .step-instruction { margin: 0 0 4px 0; color: #334155; line-height: 1.4; }
          .step-duration {
            display: inline-flex;
            align-items: center;
            gap: 4px;
            font-size: 0.75rem;
            color: #64748b;
            mat-icon { font-size: 14px; width: 14px; height: 14px; }
          }
        }
      }
    }
    .my-4 { margin: 16px 0; }
    .text-muted { color: #94a3b8; }
    @media (max-width: 600px) {
      .dialog-content { min-width: 100%; }
    }
  `]
})
export class RecipeDetailDialogComponent implements OnInit {
  private dialogRef = inject(MatDialogRef<RecipeDetailDialogComponent>);
  readonly data: RecipeDetailDialogData = inject(MAT_DIALOG_DATA);
  private notificationService = inject(NotificationService);

  recipe!: Recipe;
  currentServings = 4;

  ngOnInit(): void {
    this.recipe = this.data.recipe;
    this.currentServings = this.recipe.servings || 4;
  }

  adjustServings(delta: number): void {
    this.currentServings = Math.max(1, this.currentServings + delta);
  }

  getScaledQuantity(baseQuantity: number): number {
    if (!this.recipe.servings || this.recipe.servings === 0) return baseQuantity;
    const ratio = this.currentServings / this.recipe.servings;
    const scaled = baseQuantity * ratio;
    return Math.round(scaled * 100) / 100;
  }

  cookRecipe(): void {
    this.notificationService.success(`Bon appétit ! Recette "${this.recipe.name}" prête à cuisiner.`);
    this.dialogRef.close();
  }
}
