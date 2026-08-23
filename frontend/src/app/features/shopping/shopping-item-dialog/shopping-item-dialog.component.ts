import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialogModule, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { Product } from '../../../core/models/product.model';
import { ProductService } from '../../../core/services/product.service';
import { ShoppingService } from '../../../core/services/shopping.service';
import { NotificationService } from '../../../core/services/notification.service';

export interface ShoppingItemDialogData {
  shoppingListId: string;
}

@Component({
  selector: 'app-shopping-item-dialog',
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
      Ajouter un article libre
    </h2>

    <mat-dialog-content>
      <form [formGroup]="form" class="item-form">
        <mat-form-field appearance="outline" class="full-width">
          <mat-label>Produit *</mat-label>
          <mat-select formControlName="productId" (selectionChange)="onProductSelected($event.value)">
            <mat-option *ngFor="let p of products" [value]="p.id">
              {{ p.name }} ({{ p.category?.name || 'Général' }})
            </mat-option>
          </mat-select>
          <mat-error *ngIf="form.get('productId')?.hasError('required')">Le produit est obligatoire</mat-error>
        </mat-form-field>

        <div class="form-row">
          <mat-form-field appearance="outline" class="half-width">
            <mat-label>Quantité à acheter *</mat-label>
            <input matInput type="number" formControlName="quantity" step="0.1" min="0.01" />
            <mat-error *ngIf="form.get('quantity')?.hasError('required')">La quantité est obligatoire</mat-error>
          </mat-form-field>

          <mat-form-field appearance="outline" class="half-width">
            <mat-label>Unité *</mat-label>
            <mat-select formControlName="unit">
              <mat-option value="KG">Kilogramme (kg)</mat-option>
              <mat-option value="G">Gramme (g)</mat-option>
              <mat-option value="L">Litre (L)</mat-option>
              <mat-option value="ML">Millilitre (ml)</mat-option>
              <mat-option value="PIECE">Pièce (unité)</mat-option>
              <mat-option value="BUNCH">Botte</mat-option>
              <mat-option value="CAN">Boîte / Conserve</mat-option>
              <mat-option value="PACK">Paquet / Sachet</mat-option>
            </mat-select>
          </mat-form-field>
        </div>
      </form>
    </mat-dialog-content>

    <mat-dialog-actions align="end">
      <button mat-button mat-dialog-close [disabled]="isSubmitting">Annuler</button>
      <button mat-raised-button color="primary" (click)="onSubmit()" [disabled]="form.invalid || isSubmitting">
        <mat-icon>add</mat-icon>
        Ajouter à la liste
      </button>
    </mat-dialog-actions>
  `,
  styles: [`
    .item-form {
      display: flex;
      flex-direction: column;
      gap: 12px;
      padding-top: 12px;
      min-width: 440px;
    }
    .full-width { width: 100%; }
    .form-row { display: flex; gap: 16px; }
    .half-width { flex: 1; }
    @media (max-width: 500px) {
      .item-form { min-width: 100%; }
      .form-row { flex-direction: column; gap: 8px; }
    }
  `]
})
export class ShoppingItemDialogComponent implements OnInit {
  private fb = inject(FormBuilder);
  private dialogRef = inject(MatDialogRef<ShoppingItemDialogComponent>);
  readonly data: ShoppingItemDialogData = inject(MAT_DIALOG_DATA);
  private productService = inject(ProductService);
  private shoppingService = inject(ShoppingService);
  private notificationService = inject(NotificationService);

  form!: FormGroup;
  products: Product[] = [];
  isSubmitting = false;

  ngOnInit(): void {
    this.form = this.fb.group({
      productId: [null, Validators.required],
      quantity: [1, [Validators.required, Validators.min(0.01)]],
      unit: ['PIECE', Validators.required]
    });

    this.productService.getAllProducts().subscribe({
      next: (prods) => this.products = prods
    });
  }

  onProductSelected(productId: string): void {
    const prod = this.products.find(p => p.id === productId);
    if (prod && prod.defaultUnit) {
      this.form.patchValue({ unit: prod.defaultUnit });
    }
  }

  onSubmit(): void {
    if (this.form.invalid) return;

    this.isSubmitting = true;
    this.shoppingService.addItem(this.data.shoppingListId, this.form.value).subscribe({
      next: (updatedList) => {
        this.notificationService.success('Article ajouté à la liste');
        this.dialogRef.close(updatedList);
      },
      error: () => this.isSubmitting = false
    });
  }
}
