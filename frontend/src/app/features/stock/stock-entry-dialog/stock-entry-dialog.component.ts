import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialogModule, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { Product, UNIT_OPTIONS } from '../../../core/models/product.model';
import { LOCATION_OPTIONS } from '../../../core/models/stock.model';
import { ProductService } from '../../../core/services/product.service';
import { StockService } from '../../../core/services/stock.service';
import { NotificationService } from '../../../core/services/notification.service';

export interface StockEntryDialogData {
  initialProductId?: string;
  initialWeight?: number;
}

@Component({
  selector: 'app-stock-entry-dialog',
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
      <mat-icon color="primary">add_shopping_cart</mat-icon>
      Ajouter un produit en stock
    </h2>

    <mat-dialog-content>
      <form [formGroup]="form" class="stock-form">
        <!-- Sélection du produit -->
        <mat-form-field appearance="outline" class="full-width">
          <mat-label>Produit alimentaire *</mat-label>
          <mat-select formControlName="productId" (selectionChange)="onProductSelected($event.value)">
            <mat-option *ngFor="let p of products" [value]="p.id">
              {{ p.name }} <span class="product-cat" *ngIf="p.category">({{ p.category.name }})</span>
            </mat-option>
          </mat-select>
          <mat-error *ngIf="form.get('productId')?.hasError('required')">Veuillez sélectionner un produit</mat-error>
        </mat-form-field>

        <div class="form-row">
          <mat-form-field appearance="outline" class="half-width">
            <mat-label>Quantité *</mat-label>
            <input matInput type="number" step="0.01" min="0.01" formControlName="quantity" placeholder="Ex: 1.5" />
            <mat-error *ngIf="form.get('quantity')?.hasError('required')">La quantité est obligatoire</mat-error>
          </mat-form-field>

          <mat-form-field appearance="outline" class="half-width">
            <mat-label>Unité</mat-label>
            <mat-select formControlName="unit">
              <mat-option *ngFor="let u of unitOptions" [value]="u.value">
                {{ u.label }} ({{ u.symbol }})
              </mat-option>
            </mat-select>
          </mat-form-field>
        </div>

        <div class="form-row">
          <mat-form-field appearance="outline" class="half-width">
            <mat-label>Emplacement</mat-label>
            <mat-select formControlName="location">
              <mat-option *ngFor="let loc of locationOptions" [value]="loc.value">
                <mat-icon class="loc-icon">{{ loc.icon }}</mat-icon>
                {{ loc.label }}
              </mat-option>
            </mat-select>
          </mat-form-field>

          <mat-form-field appearance="outline" class="half-width">
            <mat-label>Date de péremption (DLC)</mat-label>
            <input matInput type="date" formControlName="expirationDate" />
            <mat-hint>Date limite de consommation recommandée</mat-hint>
          </mat-form-field>
        </div>
      </form>
    </mat-dialog-content>

    <mat-dialog-actions align="end">
      <button mat-button mat-dialog-close [disabled]="isSubmitting">Annuler</button>
      <button mat-raised-button color="primary" (click)="onSubmit()" [disabled]="form.invalid || isSubmitting">
        <mat-icon>check</mat-icon>
        Ajouter au stock
      </button>
    </mat-dialog-actions>
  `,
  styles: [`
    .stock-form {
      display: flex;
      flex-direction: column;
      gap: 8px;
      padding-top: 12px;
      min-width: 480px;
    }
    .form-row {
      display: flex;
      gap: 16px;
    }
    .half-width {
      flex: 1;
    }
    .product-cat {
      color: #64748b;
      font-size: 0.85rem;
    }
    .loc-icon {
      margin-right: 6px;
      vertical-align: middle;
      font-size: 18px;
      width: 18px;
      height: 18px;
    }
    @media (max-width: 600px) {
      .stock-form {
        min-width: 100%;
      }
      .form-row {
        flex-direction: column;
        gap: 8px;
      }
    }
  `]
})
export class StockEntryDialogComponent implements OnInit {
  private fb = inject(FormBuilder);
  private dialogRef = inject(MatDialogRef<StockEntryDialogComponent>);
  private data: StockEntryDialogData = inject(MAT_DIALOG_DATA);
  private productService = inject(ProductService);
  private stockService = inject(StockService);
  private notificationService = inject(NotificationService);

  form!: FormGroup;
  products: Product[] = [];
  unitOptions = UNIT_OPTIONS;
  locationOptions = LOCATION_OPTIONS;
  isSubmitting = false;

  ngOnInit(): void {
    this.initForm();
    this.loadProducts();
  }

  private initForm(): void {
    this.form = this.fb.group({
      productId: [this.data?.initialProductId || null, [Validators.required]],
      quantity: [this.data?.initialWeight || 1, [Validators.required, Validators.min(0.001)]],
      unit: ['KG', [Validators.required]],
      location: ['FRIDGE', [Validators.required]],
      expirationDate: ['']
    });
  }

  private loadProducts(): void {
    this.productService.getAllProducts().subscribe({
      next: (prods) => {
        this.products = prods;
        if (this.data?.initialProductId) {
          this.onProductSelected(this.data.initialProductId);
        }
      },
      error: () => this.notificationService.error('Erreur lors du chargement des produits')
    });
  }

  onProductSelected(productId: string): void {
    const selected = this.products.find(p => p.id === productId);
    if (selected) {
      this.form.patchValue({ unit: selected.defaultUnit });
      if (selected.averageShelfLifeDays) {
        const d = new Date();
        d.setDate(d.getDate() + selected.averageShelfLifeDays);
        const dateString = d.toISOString().split('T')[0];
        this.form.patchValue({ expirationDate: dateString });
      }
    }
  }

  onSubmit(): void {
    if (this.form.invalid) return;

    this.isSubmitting = true;
    const formValue = this.form.value;

    const request = {
      productId: formValue.productId,
      quantity: formValue.quantity,
      unit: formValue.unit,
      location: formValue.location,
      expirationDate: formValue.expirationDate ? formValue.expirationDate : undefined
    };

    this.stockService.createStockEntry(request).subscribe({
      next: (item) => {
        this.notificationService.success(`Produit "${item.product.name}" ajouté au stock`);
        this.dialogRef.close(item);
      },
      error: () => this.isSubmitting = false
    });
  }
}
