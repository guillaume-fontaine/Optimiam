import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, FormArray, Validators } from '@angular/forms';
import { MatDialogModule, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatDividerModule } from '@angular/material/divider';
import { Product, UNIT_OPTIONS } from '../../../core/models/product.model';
import { DIFFICULTY_OPTIONS, Recipe } from '../../../core/models/recipe.model';
import { ProductService } from '../../../core/services/product.service';
import { RecipeService } from '../../../core/services/recipe.service';
import { NotificationService } from '../../../core/services/notification.service';

export interface RecipeFormDialogData {
  recipe?: Recipe;
}

@Component({
  selector: 'app-recipe-form-dialog',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatIconModule,
    MatCheckboxModule,
    MatDividerModule
  ],
  template: `
    <h2 mat-dialog-title>
      <mat-icon color="primary">{{ isEditMode ? 'edit' : 'restaurant_menu' }}</mat-icon>
      {{ isEditMode ? 'Modifier la recette' : 'Créer une nouvelle recette' }}
    </h2>

    <mat-dialog-content class="form-dialog-content">
      <form [formGroup]="form" class="recipe-form">
        <!-- Informations Générales -->
        <mat-form-field appearance="outline" class="full-width">
          <mat-label>Nom de la recette *</mat-label>
          <input matInput formControlName="name" placeholder="Ex: Risotto aux courgettes" />
          <mat-error *ngIf="form.get('name')?.hasError('required')">Le nom est obligatoire</mat-error>
        </mat-form-field>

        <mat-form-field appearance="outline" class="full-width">
          <mat-label>Description / Astuce du chef</mat-label>
          <textarea matInput rows="2" formControlName="description" placeholder="Courte présentation de la recette..."></textarea>
        </mat-form-field>

        <div class="form-row">
          <mat-form-field appearance="outline" class="form-field-3">
            <mat-label>Préparation (min) *</mat-label>
            <input matInput type="number" formControlName="preparationTimeMinutes" min="0" />
          </mat-form-field>

          <mat-form-field appearance="outline" class="form-field-3">
            <mat-label>Cuisson (min)</mat-label>
            <input matInput type="number" formControlName="cookingTimeMinutes" min="0" />
          </mat-form-field>

          <mat-form-field appearance="outline" class="form-field-3">
            <mat-label>Portions *</mat-label>
            <input matInput type="number" formControlName="servings" min="1" />
          </mat-form-field>
        </div>

        <div class="form-row">
          <mat-form-field appearance="outline" class="half-width">
            <mat-label>Difficulté *</mat-label>
            <mat-select formControlName="difficulty">
              <mat-option *ngFor="let d of difficultyOptions" [value]="d.value">{{ d.label }}</mat-option>
            </mat-select>
          </mat-form-field>

          <mat-form-field appearance="outline" class="half-width">
            <mat-label>Tags (séparés par des virgules)</mat-label>
            <input matInput formControlName="tagsString" placeholder="Végétarien, Anti-gaspi, Rapide..." />
          </mat-form-field>
        </div>

        <mat-divider class="my-3"></mat-divider>

        <!-- Section Ingrédients dynamiques -->
        <div class="section-header">
          <h3>Ingrédients de la recette</h3>
          <button type="button" mat-stroked-button color="primary" (click)="addIngredient()">
            <mat-icon>add</mat-icon> Ajouter un ingrédient
          </button>
        </div>

        <div formArrayName="ingredients" class="array-container">
          <div *ngFor="let ing of ingredientsArray.controls; let i = index" [formGroupName]="i" class="ingredient-row">
            <mat-form-field appearance="outline" class="product-select">
              <mat-label>Produit *</mat-label>
              <mat-select formControlName="productId">
                <mat-option *ngFor="let p of products" [value]="p.id">{{ p.name }}</mat-option>
              </mat-select>
            </mat-form-field>

            <mat-form-field appearance="outline" class="qty-input">
              <mat-label>Qté *</mat-label>
              <input matInput type="number" step="0.01" min="0.001" formControlName="quantity" />
            </mat-form-field>

            <mat-form-field appearance="outline" class="unit-select">
              <mat-label>Unité</mat-label>
              <mat-select formControlName="unit">
                <mat-option *ngFor="let u of unitOptions" [value]="u.value">{{ u.symbol }}</mat-option>
              </mat-select>
            </mat-form-field>

            <mat-checkbox formControlName="optional" class="opt-check">Facultatif</mat-checkbox>

            <button type="button" mat-icon-button color="warn" (click)="removeIngredient(i)" [disabled]="ingredientsArray.length <= 1">
              <mat-icon>delete</mat-icon>
            </button>
          </div>
        </div>

        <mat-divider class="my-3"></mat-divider>

        <!-- Section Étapes dynamiques -->
        <div class="section-header">
          <h3>Étapes de préparation</h3>
          <button type="button" mat-stroked-button color="primary" (click)="addStep()">
            <mat-icon>add</mat-icon> Ajouter une étape
          </button>
        </div>

        <div formArrayName="steps" class="array-container">
          <div *ngFor="let step of stepsArray.controls; let i = index" [formGroupName]="i" class="step-row">
            <span class="step-badge">{{ i + 1 }}</span>
            <mat-form-field appearance="outline" class="step-instruction">
              <mat-label>Instruction de l'étape *</mat-label>
              <input matInput formControlName="instruction" placeholder="Ex: Couper les légumes en dés..." />
            </mat-form-field>

            <mat-form-field appearance="outline" class="step-duration">
              <mat-label>Min</mat-label>
              <input matInput type="number" formControlName="durationMinutes" min="0" />
            </mat-form-field>

            <button type="button" mat-icon-button color="warn" (click)="removeStep(i)">
              <mat-icon>delete</mat-icon>
            </button>
          </div>
        </div>

        <mat-divider class="my-3"></mat-divider>

        <!-- Section Nutrition -->
        <h3>Valeurs nutritionnelles estimées (par portion)</h3>
        <div formGroupName="nutrition" class="form-row">
          <mat-form-field appearance="outline" class="nutri-field">
            <mat-label>Calories (kcal)</mat-label>
            <input matInput type="number" formControlName="calories" />
          </mat-form-field>
          <mat-form-field appearance="outline" class="nutri-field">
            <mat-label>Protéines (g)</mat-label>
            <input matInput type="number" step="0.1" formControlName="protein" />
          </mat-form-field>
          <mat-form-field appearance="outline" class="nutri-field">
            <mat-label>Glucides (g)</mat-label>
            <input matInput type="number" step="0.1" formControlName="carbohydrates" />
          </mat-form-field>
          <mat-form-field appearance="outline" class="nutri-field">
            <mat-label>Lipides (g)</mat-label>
            <input matInput type="number" step="0.1" formControlName="fat" />
          </mat-form-field>
        </div>
      </form>
    </mat-dialog-content>

    <mat-dialog-actions align="end">
      <button mat-button mat-dialog-close [disabled]="isSubmitting">Annuler</button>
      <button mat-raised-button color="primary" (click)="onSubmit()" [disabled]="form.invalid || isSubmitting">
        <mat-icon>save</mat-icon>
        {{ isEditMode ? 'Enregistrer les modifications' : 'Créer la recette' }}
      </button>
    </mat-dialog-actions>
  `,
  styles: [`
    .form-dialog-content {
      padding: 16px 24px;
      max-height: 80vh;
      min-width: 620px;
    }
    .recipe-form {
      display: flex;
      flex-direction: column;
      gap: 10px;
    }
    .form-row {
      display: flex;
      gap: 16px;
    }
    .half-width { flex: 1; }
    .form-field-3 { flex: 1; }
    .nutri-field { flex: 1; }
    .section-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 8px;
      h3 { margin: 0; font-size: 1.05rem; font-weight: 600; }
    }
    .array-container {
      display: flex;
      flex-direction: column;
      gap: 8px;
    }
    .ingredient-row {
      display: flex;
      gap: 12px;
      align-items: center;
      .product-select { flex: 2; }
      .qty-input { width: 90px; }
      .unit-select { width: 80px; }
      .opt-check { margin-top: -12px; }
    }
    .step-row {
      display: flex;
      gap: 12px;
      align-items: center;
      .step-badge {
        width: 24px;
        height: 24px;
        border-radius: 50%;
        background-color: #16a34a;
        color: #ffffff;
        display: flex;
        align-items: center;
        justify-content: center;
        font-weight: 700;
        font-size: 0.8rem;
        margin-top: -16px;
      }
      .step-instruction { flex: 1; }
      .step-duration { width: 80px; }
    }
    .my-3 { margin: 12px 0; }
    @media (max-width: 650px) {
      .form-dialog-content { min-width: 100%; }
      .form-row { flex-direction: column; gap: 8px; }
      .ingredient-row { flex-wrap: wrap; }
    }
  `]
})
export class RecipeFormDialogComponent implements OnInit {
  private fb = inject(FormBuilder);
  private dialogRef = inject(MatDialogRef<RecipeFormDialogComponent>);
  readonly data: RecipeFormDialogData = inject(MAT_DIALOG_DATA);
  private productService = inject(ProductService);
  private recipeService = inject(RecipeService);
  private notificationService = inject(NotificationService);

  form!: FormGroup;
  products: Product[] = [];
  difficultyOptions = DIFFICULTY_OPTIONS;
  unitOptions = UNIT_OPTIONS;
  isEditMode = false;
  isSubmitting = false;

  get ingredientsArray(): FormArray {
    return this.form.get('ingredients') as FormArray;
  }

  get stepsArray(): FormArray {
    return this.form.get('steps') as FormArray;
  }

  ngOnInit(): void {
    this.isEditMode = !!this.data?.recipe;
    this.initForm();
    this.loadProducts();
  }

  private initForm(): void {
    const r = this.data?.recipe;
    this.form = this.fb.group({
      name: [r?.name || '', [Validators.required, Validators.maxLength(150)]],
      description: [r?.description || ''],
      preparationTimeMinutes: [r?.preparationTimeMinutes || 15, [Validators.required, Validators.min(0)]],
      cookingTimeMinutes: [r?.cookingTimeMinutes || 0, [Validators.min(0)]],
      difficulty: [r?.difficulty || 'EASY', [Validators.required]],
      servings: [r?.servings || 4, [Validators.required, Validators.min(1)]],
      tagsString: [r?.tags ? Array.from(r.tags).join(', ') : ''],
      ingredients: this.fb.array([]),
      steps: this.fb.array([]),
      nutrition: this.fb.group({
        calories: [r?.nutrition?.calories || null],
        protein: [r?.nutrition?.protein || null],
        carbohydrates: [r?.nutrition?.carbohydrates || null],
        fat: [r?.nutrition?.fat || null]
      })
    });

    if (r?.ingredients && r.ingredients.length > 0) {
      r.ingredients.forEach(ing => this.addIngredient(ing.product.id, ing.quantity, ing.unit, ing.optional));
    } else {
      this.addIngredient();
    }

    if (r?.steps && r.steps.length > 0) {
      r.steps.forEach(step => this.addStep(step.instruction, step.durationMinutes));
    } else {
      this.addStep();
    }
  }

  private loadProducts(): void {
    this.productService.getAllProducts().subscribe({
      next: (prods) => this.products = prods
    });
  }

  addIngredient(productId = '', quantity = 1, unit = 'KG', optional = false): void {
    const ingGroup = this.fb.group({
      productId: [productId, Validators.required],
      quantity: [quantity, [Validators.required, Validators.min(0.001)]],
      unit: [unit, Validators.required],
      optional: [optional]
    });
    this.ingredientsArray.push(ingGroup);
  }

  removeIngredient(index: number): void {
    this.ingredientsArray.removeAt(index);
  }

  addStep(instruction = '', durationMinutes?: number): void {
    const stepGroup = this.fb.group({
      instruction: [instruction, Validators.required],
      durationMinutes: [durationMinutes || null]
    });
    this.stepsArray.push(stepGroup);
  }

  removeStep(index: number): void {
    this.stepsArray.removeAt(index);
  }

  onSubmit(): void {
    if (this.form.invalid) return;

    this.isSubmitting = true;
    const formVal = this.form.value;

    const tags = formVal.tagsString
      ? formVal.tagsString.split(',').map((t: string) => t.trim()).filter((t: string) => t.length > 0)
      : [];

    const formattedSteps = formVal.steps.map((s: any, idx: number) => ({
      stepNumber: idx + 1,
      instruction: s.instruction,
      durationMinutes: s.durationMinutes ? s.durationMinutes : undefined
    }));

    const request = {
      name: formVal.name,
      description: formVal.description,
      preparationTimeMinutes: formVal.preparationTimeMinutes,
      cookingTimeMinutes: formVal.cookingTimeMinutes,
      difficulty: formVal.difficulty,
      servings: formVal.servings,
      tags: tags,
      ingredients: formVal.ingredients,
      steps: formattedSteps,
      nutrition: formVal.nutrition
    };

    if (this.isEditMode && this.data.recipe) {
      this.recipeService.updateRecipe(this.data.recipe.id, request).subscribe({
        next: (saved) => {
          this.notificationService.success(`Recette "${saved.name}" mise à jour`);
          this.dialogRef.close(saved);
        },
        error: () => this.isSubmitting = false
      });
    } else {
      this.recipeService.createRecipe(request).subscribe({
        next: (saved) => {
          this.notificationService.success(`Recette "${saved.name}" créée avec succès`);
          this.dialogRef.close(saved);
        },
        error: () => this.isSubmitting = false
      });
    }
  }
}
