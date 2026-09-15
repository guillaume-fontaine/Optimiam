import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, ValidationErrors, ValidatorFn, Validators } from '@angular/forms';
import { MatDialog, MatDialogModule, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { DateAdapter, MAT_DATE_FORMATS, MAT_DATE_LOCALE, MatNativeDateModule } from '@angular/material/core';
import { CreateMealPlanRequest, MealPlan, MEAL_TYPE_OPTIONS, MealType } from '../../../core/models/planning.model';
import { Recipe } from '../../../core/models/recipe.model';
import { RecipeService } from '../../../core/services/recipe.service';
import { PlanningService } from '../../../core/services/planning.service';
import { NotificationService } from '../../../core/services/notification.service';
import { MealPlanConflictDialogComponent } from './meal-plan-conflict-dialog.component';
import { forkJoin, switchMap } from 'rxjs';

export interface MealPlanDialogData {
  initialDate?: string;
  initialMealType?: MealType;
  initialRecipeId?: string;
}

function notPastDateValidator(): ValidatorFn {
  return (control): ValidationErrors | null => {
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    return control.value && control.value < today ? { pastDate: true } : null;
  };
}

function parseLocalDate(dateString: string): Date {
  const [year, month, day] = dateString.split('-').map(Number);
  return new Date(year, month - 1, day);
}

function formatLocalDate(date: Date): string {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
}

const FRENCH_DATE_FORMATS = {
  parse: { dateInput: 'DD/MM/YYYY' },
  display: {
    dateInput: 'dd/MM/yyyy',
    monthYearLabel: 'MMMM yyyy',
    dateA11yLabel: 'dd/MM/yyyy',
    monthYearA11yLabel: 'MMMM yyyy'
  }
};

@Component({
  selector: 'app-meal-plan-dialog',
  standalone: true,
  providers: [
    { provide: MAT_DATE_LOCALE, useValue: 'fr-FR' },
    { provide: MAT_DATE_FORMATS, useValue: FRENCH_DATE_FORMATS }
  ],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatIconModule,
    MatDatepickerModule,
    MatNativeDateModule
  ],
  template: `
    <h2 mat-dialog-title>
      <mat-icon color="primary">calendar_month</mat-icon>
      Planifier un repas
    </h2>

    <mat-dialog-content>
      <form [formGroup]="form" class="plan-form">
        <div class="form-row">
          <mat-form-field appearance="outline" class="half-width">
            <mat-label>Date du repas *</mat-label>
            <input matInput [matDatepicker]="datePicker" formControlName="date" [min]="today"
              (click)="datePicker.open()" (focus)="datePicker.open()" />
            <mat-datepicker-toggle matIconSuffix [for]="datePicker"></mat-datepicker-toggle>
            <mat-datepicker #datePicker></mat-datepicker>
            <mat-error *ngIf="form.get('date')?.hasError('required')">La date est obligatoire</mat-error>
            <mat-error *ngIf="form.get('date')?.hasError('pastDate')">La date doit être aujourd'hui ou ultérieure</mat-error>
          </mat-form-field>

          <mat-form-field appearance="outline" class="half-width">
            <mat-label>Créneau *</mat-label>
            <mat-select formControlName="mealType">
              <mat-option *ngFor="let opt of mealTypeOptions" [value]="opt.value">
                <mat-icon class="opt-icon">{{ opt.icon }}</mat-icon>
                {{ opt.label }}
              </mat-option>
            </mat-select>
            <mat-error *ngIf="form.get('mealType')?.hasError('required')">Le créneau est obligatoire</mat-error>
          </mat-form-field>
        </div>

        <!-- Sélection de la recette -->
        <mat-form-field appearance="outline" class="full-width">
          <mat-label>Recette de cuisine *</mat-label>
          <mat-select formControlName="recipeId" (selectionChange)="onRecipeSelected($event.value)">
            <mat-option *ngFor="let r of recipes" [value]="r.id">
              {{ r.name }} ({{ r.totalTimeMinutes }} min • {{ r.servings }} pers.)
            </mat-option>
          </mat-select>
          <mat-error *ngIf="form.get('recipeId')?.hasError('required')">Veuillez sélectionner une recette</mat-error>
        </mat-form-field>

        <div class="form-row">
          <mat-form-field appearance="outline" class="half-width">
            <mat-label>Nombre de portions *</mat-label>
            <input matInput type="number" formControlName="servings" min="1" />
            <mat-error *ngIf="form.get('servings')?.hasError('required')">Les portions sont obligatoires</mat-error>
          </mat-form-field>

          <mat-form-field appearance="outline" class="half-width">
            <mat-label>Note / Remarque (facultatif)</mat-label>
            <input matInput formControlName="notes" placeholder="Ex: Déjeuner en famille" />
          </mat-form-field>
        </div>
      </form>
    </mat-dialog-content>

    <mat-dialog-actions align="end">
      <button mat-button mat-dialog-close [disabled]="isSubmitting">Annuler</button>
      <button mat-raised-button color="primary" (click)="onSubmit()" [disabled]="form.invalid || isSubmitting">
        <mat-icon>done</mat-icon>
        Ajouter au planning
      </button>
    </mat-dialog-actions>
  `,
  styles: [`
    .plan-form {
      display: flex;
      flex-direction: column;
      gap: 12px;
      padding-top: 12px;
      min-width: 480px;
    }
    .form-row {
      display: flex;
      gap: 16px;
    }
    .half-width { flex: 1; }
    .opt-icon {
      font-size: 18px;
      width: 18px;
      height: 18px;
      vertical-align: middle;
      margin-right: 6px;
    }
    @media (max-width: 550px) {
      .plan-form { min-width: 100%; }
      .form-row { flex-direction: column; gap: 8px; }
    }
  `]
})
export class MealPlanDialogComponent implements OnInit {
  private fb = inject(FormBuilder);
  private dialogRef = inject(MatDialogRef<MealPlanDialogComponent>);
  readonly data: MealPlanDialogData = inject(MAT_DIALOG_DATA);
  private recipeService = inject(RecipeService);
  private planningService = inject(PlanningService);
  private notificationService = inject(NotificationService);
  private dateAdapter = inject(DateAdapter<Date>);

  form!: FormGroup;
  recipes: Recipe[] = [];
  mealTypeOptions = MEAL_TYPE_OPTIONS.filter(option => option.value === 'LUNCH' || option.value === 'DINNER');
  isSubmitting = false;
  today = new Date();
  private dialog = inject(MatDialog);

  ngOnInit(): void {
    this.dateAdapter.setLocale('fr-FR');
    this.today.setHours(0, 0, 0, 0);
    this.form = this.fb.group({
      date: [this.data?.initialDate ? parseLocalDate(this.data.initialDate) : this.today, [Validators.required, notPastDateValidator()]],
      mealType: [this.data?.initialMealType || 'LUNCH', Validators.required],
      recipeId: [this.data?.initialRecipeId || null, Validators.required],
      servings: [4, [Validators.required, Validators.min(1)]],
      notes: ['']
    });

    this.loadRecipes();
  }

  private loadRecipes(): void {
    this.recipeService.getAllRecipes().subscribe({
      next: (recs) => {
        this.recipes = recs;
        if (this.data?.initialRecipeId) {
          this.onRecipeSelected(this.data.initialRecipeId);
        }
      }
    });
  }

  onRecipeSelected(recipeId: string): void {
    const r = this.recipes.find(rec => rec.id === recipeId);
    if (r && r.servings) {
      this.form.patchValue({ servings: r.servings });
    }
  }

  onSubmit(): void {
    if (this.form.invalid) return;

    this.isSubmitting = true;
    const formValue = this.form.value;
    const request: CreateMealPlanRequest = { ...formValue, date: formatLocalDate(formValue.date) };
    this.planningService.getMealPlans(request.date, request.date).subscribe({
      next: (meals) => {
        const conflicts = meals.filter(meal => meal.mealType === request.mealType);
        if (conflicts.length === 0) {
          this.createMealPlan(request);
          return;
        }

        const conflictDialog = this.dialog.open(MealPlanConflictDialogComponent, {
          width: '460px',
          data: { existingMeals: conflicts }
        });

        conflictDialog.afterClosed().subscribe((action) => {
          if (action === 'replace') {
            this.replaceMealPlans(conflicts, request);
          } else {
            this.isSubmitting = false;
          }
        });
      },
      error: () => this.isSubmitting = false
    });
  }

  private createMealPlan(request: CreateMealPlanRequest): void {
    this.planningService.createMealPlan(request).subscribe({
      next: (created) => {
        this.notificationService.success(`Repas "${created.recipe.name}" planifié avec succès`);
        this.dialogRef.close(created);
      },
      error: () => this.isSubmitting = false
    });
  }

  private replaceMealPlans(existingMeals: MealPlan[], request: CreateMealPlanRequest): void {
    forkJoin(existingMeals.map(meal => this.planningService.deleteMealPlan(meal.id))).pipe(
      switchMap(() => this.planningService.createMealPlan(request))
    ).subscribe({
      next: (created) => {
        this.notificationService.success(`Repas "${created.recipe.name}" planifié en remplacement de l'ancien`);
        this.dialogRef.close(created);
      },
      error: () => this.isSubmitting = false
    });
  }
}
