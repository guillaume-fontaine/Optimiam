import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialogModule, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { StockItem } from '../../../core/models/stock.model';
import { StockService } from '../../../core/services/stock.service';
import { NotificationService } from '../../../core/services/notification.service';

export interface StockExitDialogData {
  stockItem: StockItem;
}

@Component({
  selector: 'app-stock-exit-dialog',
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
      <mat-icon color="primary">restaurant</mat-icon>
      Consommer du stock
    </h2>

    <mat-dialog-content>
      <div class="product-summary">
        <span class="product-name">{{ data.stockItem.product.name }}</span>
        <span class="available-qty">Disponible : {{ data.stockItem.quantity }} {{ data.stockItem.unitSymbol }}</span>
      </div>

      <div class="quick-actions">
        <button type="button" mat-stroked-button (click)="setPercentage(0.25)">25%</button>
        <button type="button" mat-stroked-button (click)="setPercentage(0.5)">50%</button>
        <button type="button" mat-stroked-button (click)="setPercentage(1)">Tout consommer</button>
      </div>

      <form [formGroup]="form" class="exit-form">
        <mat-form-field appearance="outline" class="full-width">
          <mat-label>Quantité à consommer ({{ data.stockItem.unitSymbol }}) *</mat-label>
          <input matInput type="number" step="0.01" min="0.001" [max]="data.stockItem.quantity" formControlName="quantity" />
          <mat-error *ngIf="form.get('quantity')?.hasError('required')">La quantité est obligatoire</mat-error>
          <mat-error *ngIf="form.get('quantity')?.hasError('max')">Ne peut pas dépasser la quantité disponible ({{ data.stockItem.quantity }})</mat-error>
        </mat-form-field>

        <mat-form-field appearance="outline" class="full-width">
          <mat-label>Motif / Recette préparée</mat-label>
          <input matInput formControlName="reason" placeholder="Ex: Préparation repas midi, goûter..." />
        </mat-form-field>
      </form>
    </mat-dialog-content>

    <mat-dialog-actions align="end">
      <button mat-button mat-dialog-close [disabled]="isSubmitting">Annuler</button>
      <button mat-raised-button color="primary" (click)="onSubmit()" [disabled]="form.invalid || isSubmitting">
        <mat-icon>done</mat-icon>
        Valider la consommation
      </button>
    </mat-dialog-actions>
  `,
  styles: [`
    .product-summary {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 12px 16px;
      background-color: #f1f5f9;
      border-radius: 8px;
      margin-bottom: 16px;
      .product-name { font-weight: 600; font-size: 1.1rem; color: #0f172a; }
      .available-qty { font-size: 0.9rem; color: #475569; font-weight: 500; }
    }
    .quick-actions {
      display: flex;
      gap: 8px;
      margin-bottom: 16px;
    }
    .exit-form {
      display: flex;
      flex-direction: column;
      gap: 8px;
      min-width: 400px;
    }
    @media (max-width: 500px) {
      .exit-form { min-width: 100%; }
    }
  `]
})
export class StockExitDialogComponent {
  private fb = inject(FormBuilder);
  private dialogRef = inject(MatDialogRef<StockExitDialogComponent>);
  readonly data: StockExitDialogData = inject(MAT_DIALOG_DATA);
  private stockService = inject(StockService);
  private notificationService = inject(NotificationService);

  isSubmitting = false;

  form: FormGroup = this.fb.group({
    quantity: [this.data.stockItem.quantity, [Validators.required, Validators.min(0.001), Validators.max(this.data.stockItem.quantity)]],
    reason: ['']
  });

  setPercentage(percent: number): void {
    const qty = Math.round((this.data.stockItem.quantity * percent) * 1000) / 1000;
    this.form.patchValue({ quantity: qty });
  }

  onSubmit(): void {
    if (this.form.invalid) return;

    this.isSubmitting = true;
    this.stockService.exitStock(this.data.stockItem.id, this.form.value).subscribe({
      next: (item) => {
        this.notificationService.success(`Consommation enregistrée pour ${item.product.name}`);
        this.dialogRef.close(item);
      },
      error: () => this.isSubmitting = false
    });
  }
}
