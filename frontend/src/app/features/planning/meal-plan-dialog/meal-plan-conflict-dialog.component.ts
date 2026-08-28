import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatDialogModule, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MealPlan } from '../../../core/models/planning.model';

export interface MealPlanConflictDialogData {
  existingMeals: MealPlan[];
}

export type MealPlanConflictAction = 'replace' | 'change-date' | 'cancel';

@Component({
  selector: 'app-meal-plan-conflict-dialog',
  standalone: true,
  imports: [CommonModule, MatButtonModule, MatDialogModule],
  template: `
    <h2 mat-dialog-title>Créneau déjà planifié</h2>
    <mat-dialog-content>
      <p>
        Un repas est déjà prévu à ce créneau :
        <strong>{{ data.existingMeals[0].recipe.name }}</strong>
        <span *ngIf="data.existingMeals.length > 1">
          ({{ data.existingMeals.length }} repas enregistrés)
        </span>.
      </p>
      <p>Que souhaitez-vous faire ?</p>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button (click)="close('cancel')">Annuler</button>
      <button mat-stroked-button color="primary" (click)="close('change-date')">Changer la date</button>
      <button mat-raised-button color="warn" (click)="close('replace')">Remplacer</button>
    </mat-dialog-actions>
  `
})
export class MealPlanConflictDialogComponent {
  readonly data: MealPlanConflictDialogData = inject(MAT_DIALOG_DATA);
  private dialogRef = inject(MatDialogRef<MealPlanConflictDialogComponent>);

  close(action: MealPlanConflictAction): void {
    this.dialogRef.close(action);
  }
}
