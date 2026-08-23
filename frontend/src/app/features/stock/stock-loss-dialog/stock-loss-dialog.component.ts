import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialogModule, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { LOSS_REASON_OPTIONS, StockItem } from '../../../core/models/stock.model';
import { StockService } from '../../../core/services/stock.service';
import { NotificationService } from '../../../core/services/notification.service';

export interface StockLossDialogData {
  stockItem: StockItem;
}

@Component({
  selector: 'app-stock-loss-dialog',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatIconModule
  ],
  template: `
    <h2 mat-dialog-title>
      <mat-icon color="warn">delete_sweep</mat-icon>
      Déclarer une perte / gaspillage
    </h2>

    <mat-dialog-content>
      <div class="product-summary">
        <span class="product-name">{{ data.stockItem.product.name }}</span>
        <span class="available-qty">Stock restant : {{ data.stockItem.quantity }} {{ data.stockItem.unitSymbol }}</span>
      </div>

      <form [formGroup]="form" class="loss-form">
        <mat-form-field appearance="outline" class="full-width">
          <mat-label>Quantité jetée / perdue ({{ data.stockItem.unitSymbol }}) *</mat-label>
          <input matInput type="number" step="0.01" min="0.001" [max]="data.stockItem.quantity" formControlName="quantity" />
          <mat-error *ngIf="form.get('quantity')?.hasError('required')">La quantité est obligatoire</mat-error>
          <mat-error *ngIf="form.get('quantity')?.hasError('max')">Ne peut pas dépasser la quantité en stock</mat-error>
        </mat-form-field>

        <mat-form-field appearance="outline" class="full-width">
          <mat-label>Motif de la perte *</mat-label>
          <mat-select formControlName="lossReason">
            <mat-option *ngFor="let opt of lossReasonOptions" [value]="opt.value">
              {{ opt.label }}
            </mat-option>
          </mat-select>
          <mat-error *ngIf="form.get('lossReason')?.hasError('required')">Le motif est obligatoire</mat-error>
        </mat-form-field>

        <mat-form-field appearance="outline" class="full-width">
          <mat-label>Commentaire (facultatif)</mat-label>
          <textarea matInput rows="2" formControlName="comment" placeholder="Précisions sur la cause du gaspillage..."></textarea>
        </mat-form-field>
      </form>
    </mat-dialog-content>

    <mat-dialog-actions align="end">
      <button mat-button mat-dialog-close [disabled]="isSubmitting">Annuler</button>
      <button mat-raised-button color="warn" (click)="onSubmit()" [disabled]="form.invalid || isSubmitting">
        <mat-icon>delete_forever</mat-icon>
        Enregistrer la perte
      </button>
    </mat-dialog-actions>
  `,
  styles: [`
    .product-summary {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 12px 16px;
      background-color: #fee2e2;
      border-radius: 8px;
      margin-bottom: 16px;
      .product-name { font-weight: 600; font-size: 1.1rem; color: #991b1b; }
      .available-qty { font-size: 0.9rem; color: #7f1d1d; font-weight: 500; }
    }
    .loss-form {
      display: flex;
      flex-direction: column;
      gap: 8px;
      min-width: 420px;
    }
    @media (max-width: 500px) {
      .loss-form { min-width: 100%; }
    }
  `]
})
export class StockLossDialogComponent {
  private fb = inject(FormBuilder);
  private dialogRef = inject(MatDialogRef<StockLossDialogComponent>);
  readonly data: StockLossDialogData = inject(MAT_DIALOG_DATA);
  private stockService = inject(StockService);
  private notificationService = inject(NotificationService);

  lossReasonOptions = LOSS_REASON_OPTIONS;
  isSubmitting = false;

  form: FormGroup = this.fb.group({
    quantity: [this.data.stockItem.quantity, [Validators.required, Validators.min(0.001), Validators.max(this.data.stockItem.quantity)]],
    lossReason: ['EXPIRED', [Validators.required]],
    comment: ['']
  });

  onSubmit(): void {
    if (this.form.invalid) return;

    this.isSubmitting = true;
    this.stockService.recordLoss(this.data.stockItem.id, this.form.value).subscribe({
      next: (item) => {
        this.notificationService.info(`Perte enregistrée pour "${item.product.name}"`);
        this.dialogRef.close(item);
      },
      error: () => this.isSubmitting = false
    });
  }
}
