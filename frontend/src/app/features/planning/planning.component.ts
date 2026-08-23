import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatTooltipModule } from '@angular/material/tooltip';

import { MealPlan, MealType } from '../../core/models/planning.model';
import { PlanningService } from '../../core/services/planning.service';
import { NotificationService } from '../../core/services/notification.service';
import { MealPlanDialogComponent } from './meal-plan-dialog/meal-plan-dialog.component';
import { RecipeDetailDialogComponent } from '../recipes/recipe-detail-dialog/recipe-detail-dialog.component';
import { ConfirmDialogComponent, ConfirmDialogData } from '../../shared/ui/confirm-dialog/confirm-dialog.component';
import { LoadingSpinnerComponent } from '../../shared/ui/loading-spinner/loading-spinner.component';
import { EmptyStateComponent } from '../../shared/ui/empty-state/empty-state.component';

export interface DayColumn {
  date: Date;
  dateString: string;
  dayName: string;
  dayNumber: string;
  isToday: boolean;
  lunchMeal?: MealPlan;
  dinnerMeal?: MealPlan;
}

@Component({
  selector: 'app-planning',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatDialogModule,
    MatTooltipModule,
    LoadingSpinnerComponent
  ],
  templateUrl: './planning.component.html',
  styleUrls: ['./planning.component.scss']
})
export class PlanningComponent implements OnInit {
  private planningService = inject(PlanningService);
  private dialog = inject(MatDialog);
  private notificationService = inject(NotificationService);

  currentWeekStart!: Date;
  dayColumns: DayColumn[] = [];
  mealPlans: MealPlan[] = [];
  isLoading = true;

  ngOnInit(): void {
    this.initCurrentWeek();
    this.loadWeekPlanning();
  }

  private initCurrentWeek(): void {
    const now = new Date();
    const dayOfWeek = now.getDay();
    const diff = now.getDate() - dayOfWeek + (dayOfWeek === 0 ? -6 : 1); // Lundi
    this.currentWeekStart = new Date(now.setDate(diff));
    this.currentWeekStart.setHours(0, 0, 0, 0);
  }

  loadWeekPlanning(): void {
    this.isLoading = true;
    this.buildDayColumns();

    const startDate = this.dayColumns[0].dateString;
    const endDate = this.dayColumns[6].dateString;

    this.planningService.getMealPlans(startDate, endDate).subscribe({
      next: (plans) => {
        this.mealPlans = plans;
        this.mapPlansToDays();
        this.isLoading = false;
      },
      error: () => {
        this.isLoading = false;
        this.notificationService.error('Erreur lors du chargement du planning');
      }
    });
  }

  private buildDayColumns(): void {
    const days: DayColumn[] = [];
    const todayStr = new Date().toISOString().split('T')[0];

    for (let i = 0; i < 7; i++) {
      const d = new Date(this.currentWeekStart);
      d.setDate(d.getDate() + i);
      const dateStr = d.toISOString().split('T')[0];

      const dayName = d.toLocaleDateString('fr-FR', { weekday: 'long' });
      const capitalizedDayName = dayName.charAt(0).toUpperCase() + dayName.slice(1);
      const dayNumber = d.toLocaleDateString('fr-FR', { day: 'numeric', month: 'short' });

      days.push({
        date: d,
        dateString: dateStr,
        dayName: capitalizedDayName,
        dayNumber: dayNumber,
        isToday: dateStr === todayStr
      });
    }

    this.dayColumns = days;
  }

  private mapPlansToDays(): void {
    this.dayColumns.forEach(day => {
      day.lunchMeal = this.mealPlans.find(p => p.date === day.dateString && p.mealType === 'LUNCH');
      day.dinnerMeal = this.mealPlans.find(p => p.date === day.dateString && p.mealType === 'DINNER');
    });
  }

  previousWeek(): void {
    this.currentWeekStart.setDate(this.currentWeekStart.getDate() - 7);
    this.loadWeekPlanning();
  }

  nextWeek(): void {
    this.currentWeekStart.setDate(this.currentWeekStart.getDate() + 7);
    this.loadWeekPlanning();
  }

  goToToday(): void {
    this.initCurrentWeek();
    this.loadWeekPlanning();
  }

  getWeekRangeLabel(): string {
    if (this.dayColumns.length === 0) return '';
    const start = this.dayColumns[0].date.toLocaleDateString('fr-FR', { day: 'numeric', month: 'long' });
    const end = this.dayColumns[6].date.toLocaleDateString('fr-FR', { day: 'numeric', month: 'long', year: 'numeric' });
    return `Semaine du ${start} au ${end}`;
  }

  openPlanDialog(initialDate?: string, initialMealType?: MealType): void {
    const dialogRef = this.dialog.open(MealPlanDialogComponent, {
      width: '540px',
      data: { initialDate, initialMealType }
    });

    dialogRef.afterClosed().subscribe((created) => {
      if (created) this.loadWeekPlanning();
    });
  }

  openRecipeDetail(plan: MealPlan): void {
    this.dialog.open(RecipeDetailDialogComponent, {
      width: '680px',
      data: { recipe: plan.recipe }
    });
  }

  markAsCooked(plan: MealPlan): void {
    this.planningService.markAsCooked(plan.id, true).subscribe({
      next: () => {
        this.notificationService.success(`Repas "${plan.recipe.name}" marqué comme cuisiné ! Ingrédients déduits.`);
        this.loadWeekPlanning();
      }
    });
  }

  deletePlan(plan: MealPlan): void {
    const dialogData: ConfirmDialogData = {
      title: 'Supprimer du planning',
      message: `Êtes-vous sûr de vouloir retirer le repas "${plan.recipe.name}" du planning ?`,
      confirmText: 'Supprimer',
      confirmColor: 'warn'
    };

    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      width: '420px',
      data: dialogData
    });

    dialogRef.afterClosed().subscribe((confirmed) => {
      if (confirmed) {
        this.planningService.deleteMealPlan(plan.id).subscribe({
          next: () => {
            this.notificationService.success('Repas retiré du planning');
            this.loadWeekPlanning();
          }
        });
      }
    });
  }
}
