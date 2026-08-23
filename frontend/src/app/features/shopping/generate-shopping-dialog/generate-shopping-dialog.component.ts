import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { ShoppingService } from '../../../core/services/shopping.service';
import { NotificationService } from '../../../core/services/notification.service';

@Component({
  selector: 'app-generate-shopping-dialog',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule
  ],
  template: `
    <h2 mat-dialog-title>
      <mat-icon color="primary">auto_mode</mat-icon>
      Générer la liste depuis le planning
    </h2>

    <mat-dialog-content>
      <p class="dialog-desc">
        OptiMiam va comparer tous les ingrédients des recettes prévues dans votre planning avec votre stock actuel et calculer exactement ce qu'il vous manque.
      </p>

      <form [formGroup]="form" class="gen-form">
        <mat-form-field appearance="outline" class="full-width">
          <mat-label>Nom de la liste</mat-label>
          <input matInput formControlName="name" placeholder="Ex: Courses semaine du 23 août" />
        </mat-form-field>

        <div class="form-row">
          <mat-form-field appearance="outline" class="half-width">
            <mat-label>Du (début planning)</mat-label>
            <input matInput type="date" formControlName="startDate" />
          </mat-form-field>

          <mat-form-field appearance="outline" class="half-width">
            <mat-label>Au (fin planning)</mat-label>
            <input matInput type="date" formControlName="endDate" />
          </mat-form-field>
        </div>
      </form>
    </mat-dialog-content>

    <mat-dialog-actions align="end">
      <button mat-button mat-dialog-close [disabled]="isSubmitting">Annuler</button>
      <button mat-raised-button color="primary" (click)="onSubmit()" [disabled]="form.invalid || isSubmitting">
        <mat-icon>auto_awesome</mat-icon>
        Calculer & Générer
      </button>
    </mat-dialog-actions>
  `,
  styles: [`
    .dialog-desc {
      color: #64748b;
      font-size: 0.9rem;
      margin: 0 0 16px 0;
    }
    .gen-form {
      display: flex;
      flex-direction: column;
      gap: 12px;
      min-width: 440px;
    }
    .full-width { width: 100%; }
    .form-row { display: flex; gap: 16px; }
    .half-width { flex: 1; }
  `]
})
export class GenerateShoppingDialogComponent implements OnInit {
  private fb = inject(FormBuilder);
  private dialogRef = inject(MatDialogRef<GenerateShoppingDialogComponent>);
  private shoppingService = inject(ShoppingService);
  private notificationService = inject(NotificationService);

  form!: FormGroup;
  isSubmitting = false;

  ngOnInit(): void {
    const now = new Date();
    const dayOfWeek = now.getDay();
    const diff = now.getDate() - dayOfWeek + (dayOfWeek === 0 ? -6 : 1);
    const monday = new Date(now.setDate(diff));
    const sunday = new Date(monday);
    sunday.setDate(sunday.getDate() + 6);

    const startStr = monday.toISOString().split('T')[0];
    const endStr = sunday.toISOString().split('T')[0];

    this.form = this.fb.group({
      name: ['Courses de la semaine'],
      startDate: [startStr, Validators.required],
      endDate: [endStr, Validators.required]
    });
  }

  onSubmit(): void {
    if (this.form.invalid) return;

    this.isSubmitting = true;
    this.shoppingService.generateFromPlanning(this.form.value).subscribe({
      next: (created) => {
        this.notificationService.success(`Liste générée : ${created.items.length} articles calculés`);
        this.dialogRef.close(created);
      },
      error: () => this.isSubmitting = false
    });
  }
}
