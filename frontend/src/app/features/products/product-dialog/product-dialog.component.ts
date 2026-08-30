import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialogModule, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { Category, Product, UNIT_OPTIONS, LOCATION_OPTIONS } from '../../../core/models/product.model';
import { CategoryService } from '../../../core/services/category.service';
import { ProductService } from '../../../core/services/product.service';
import { NotificationService } from '../../../core/services/notification.service';

export interface ProductDialogData {
  product?: Product;
}

@Component({
  selector: 'app-product-dialog',
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
      <mat-icon color="primary">{{ isEditMode ? 'edit' : 'add_circle' }}</mat-icon>
      {{ isEditMode ? 'Modifier le produit' : 'Nouveau produit alimentaire' }}
    </h2>

    <mat-dialog-content>
      <form [formGroup]="form" class="product-form">
        <mat-form-field appearance="outline" class="full-width">
          <mat-label>Nom du produit *</mat-label>
          <input matInput formControlName="name" placeholder="Ex: Tomate, Blanc de poulet, Riz..." />
          <mat-error *ngIf="form.get('name')?.hasError('required')">Le nom du produit est obligatoire</mat-error>
        </mat-form-field>

        <div class="form-row">
          <mat-form-field appearance="outline" class="half-width">
            <mat-label>Catégorie</mat-label>
            <mat-select formControlName="categoryId">
              <mat-option [value]="null">-- Aucune catégorie --</mat-option>
              <mat-option *ngFor="let cat of categories" [value]="cat.id">
                <span class="cat-option">
                  <span class="color-dot" [style.background-color]="cat.color || '#64748b'"></span>
                  {{ cat.name }}
                </span>
              </mat-option>
            </mat-select>
          </mat-form-field>

          <mat-form-field appearance="outline" class="half-width">
            <mat-label>Unité de mesure par défaut *</mat-label>
            <mat-select formControlName="defaultUnit">
              <mat-option *ngFor="let unit of unitOptions" [value]="unit.value">
                {{ unit.label }} ({{ unit.symbol }})
              </mat-option>
            </mat-select>
            <mat-error *ngIf="form.get('defaultUnit')?.hasError('required')">L'unité est obligatoire</mat-error>
          </mat-form-field>
        </div>

        <div class="form-row">
          <mat-form-field appearance="outline" class="half-width">
            <mat-label>Code-barres (EAN)</mat-label>
            <input matInput formControlName="barcode" placeholder="Ex: 300001..." />
          </mat-form-field>

          <mat-form-field appearance="outline" class="half-width">
            <mat-label>Durée de conservation (jours)</mat-label>
            <input matInput type="number" min="1" formControlName="averageShelfLifeDays" placeholder="Ex: 7" />
            <mat-hint>Utilisé pour l'estimation de péremption</mat-hint>
          </mat-form-field>
        </div>

        <div class="form-row">
          <mat-form-field appearance="outline" class="half-width">
            <mat-label>Emplacement par défaut</mat-label>
            <mat-select formControlName="defaultLocation">
              <mat-option [value]="null">-- Aucun (réfrigérateur par défaut) --</mat-option>
              <mat-option *ngFor="let loc of locationOptions" [value]="loc.value">
                <mat-icon class="location-icon">{{ loc.icon }}</mat-icon>
                {{ loc.label }}
              </mat-option>
            </mat-select>
            <mat-hint>L'emplacement de stockage par défaut lors de l'ajout au stock</mat-hint>
          </mat-form-field>
        </div>
      </form>
    </mat-dialog-content>

    <mat-dialog-actions align="end">
      <button mat-button mat-dialog-close [disabled]="isSubmitting">Annuler</button>
      <button mat-raised-button color="primary" (click)="onSubmit()" [disabled]="form.invalid || isSubmitting">
        <mat-icon>{{ isEditMode ? 'check' : 'save' }}</mat-icon>
        {{ isEditMode ? 'Mettre à jour' : 'Enregistrer' }}
      </button>
    </mat-dialog-actions>
  `,
  styles: [`
    .product-form {
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
    .cat-option {
      display: flex;
      align-items: center;
      gap: 8px;
    }
    .color-dot {
      width: 10px;
      height: 10px;
      border-radius: 50%;
      display: inline-block;
    }
    .location-icon {
      margin-right: 8px;
      vertical-align: middle;
    }
    @media (max-width: 600px) {
      .product-form {
        min-width: 100%;
      }
      .form-row {
        flex-direction: column;
        gap: 8px;
      }
    }
  `]
})
export class ProductDialogComponent implements OnInit {
  private fb = inject(FormBuilder);
  private dialogRef = inject(MatDialogRef<ProductDialogComponent>);
  private data: ProductDialogData = inject(MAT_DIALOG_DATA);
  private categoryService = inject(CategoryService);
  private productService = inject(ProductService);
  private notificationService = inject(NotificationService);

  form!: FormGroup;
  categories: Category[] = [];
  unitOptions = UNIT_OPTIONS;
  locationOptions = LOCATION_OPTIONS;
  isEditMode = false;
  isSubmitting = false;

  ngOnInit(): void {
    this.isEditMode = !!this.data?.product;
    this.initForm();
    this.loadCategories();
  }

  private initForm(): void {
    const p = this.data?.product;
    this.form = this.fb.group({
      name: [p?.name || '', [Validators.required, Validators.maxLength(150)]],
      categoryId: [p?.category?.id || null],
      defaultUnit: [p?.defaultUnit || 'KG', [Validators.required]],
      barcode: [p?.barcode || '', [Validators.maxLength(100)]],
      averageShelfLifeDays: [p?.averageShelfLifeDays || null, [Validators.min(1)]],
      defaultLocation: [p?.defaultLocation || null]
    });
  }

  private loadCategories(): void {
    this.categoryService.getAllCategories().subscribe({
      next: (cats) => this.categories = cats,
      error: () => this.notificationService.error('Erreur lors du chargement des catégories')
    });
  }

  onSubmit(): void {
    if (this.form.invalid) return;

    this.isSubmitting = true;
    const formValue = this.form.value;

    const request = {
      name: formValue.name,
      barcode: formValue.barcode || null,
      defaultUnit: formValue.defaultUnit,
      defaultLocation: formValue.defaultLocation || null,
      categoryId: formValue.categoryId || null,
      averageShelfLifeDays: formValue.averageShelfLifeDays || null
    };

    if (this.isEditMode && this.data.product) {
      this.productService.updateProduct(this.data.product.id, request).subscribe({
        next: (updatedProduct) => {
          this.notificationService.success(`Produit "${updatedProduct.name}" mis à jour avec succès`);
          this.dialogRef.close(updatedProduct);
        },
        error: () => this.isSubmitting = false
      });
    } else {
      this.productService.createProduct(request).subscribe({
        next: (createdProduct) => {
          this.notificationService.success(`Produit "${createdProduct.name}" créé avec succès`);
          this.dialogRef.close(createdProduct);
        },
        error: () => this.isSubmitting = false
      });
    }
  }
}
