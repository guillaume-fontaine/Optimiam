import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormControl } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatTooltipModule } from '@angular/material/tooltip';
import { debounceTime, distinctUntilChanged } from 'rxjs';

import { Category, Product } from '../../core/models/product.model';
import { ProductService } from '../../core/services/product.service';
import { CategoryService } from '../../core/services/category.service';
import { NotificationService } from '../../core/services/notification.service';
import { ProductDialogComponent } from './product-dialog/product-dialog.component';
import { ConfirmDialogComponent, ConfirmDialogData } from '../../shared/ui/confirm-dialog/confirm-dialog.component';
import { LoadingSpinnerComponent } from '../../shared/ui/loading-spinner/loading-spinner.component';
import { EmptyStateComponent } from '../../shared/ui/empty-state/empty-state.component';

@Component({
  selector: 'app-products',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    MatCardModule,
    MatTableModule,
    MatPaginatorModule,
    MatButtonModule,
    MatIconModule,
    MatChipsModule,
    MatFormFieldModule,
    MatInputModule,
    MatDialogModule,
    MatTooltipModule,
    LoadingSpinnerComponent,
    EmptyStateComponent
  ],
  templateUrl: './products.component.html',
  styleUrls: ['./products.component.scss']
})
export class ProductsComponent implements OnInit {
  private productService = inject(ProductService);
  private categoryService = inject(CategoryService);
  private dialog = inject(MatDialog);
  private notificationService = inject(NotificationService);

  searchControl = new FormControl('');
  categories: Category[] = [];
  products: Product[] = [];
  selectedCategoryId: string | null = null;

  isLoading = true;
  totalElements = 0;
  pageSize = 10;
  pageIndex = 0;

  displayedColumns: string[] = ['name', 'category', 'unit', 'shelfLife', 'barcode', 'actions'];

  ngOnInit(): void {
    this.loadCategories();
    this.loadProducts();

    this.searchControl.valueChanges
      .pipe(debounceTime(350), distinctUntilChanged())
      .subscribe(() => {
        this.pageIndex = 0;
        this.loadProducts();
      });
  }

  loadCategories(): void {
    this.categoryService.getAllCategories().subscribe({
      next: (cats) => this.categories = cats,
      error: () => this.notificationService.error('Erreur lors du chargement des catégories')
    });
  }

  loadProducts(): void {
    this.isLoading = true;
    const query = this.searchControl.value || undefined;
    const categoryId = this.selectedCategoryId || undefined;

    this.productService.getProducts(query, categoryId, this.pageIndex, this.pageSize)
      .subscribe({
        next: (page) => {
          this.products = page.content;
          this.totalElements = page.totalElements;
          this.isLoading = false;
        },
        error: () => {
          this.isLoading = false;
          this.notificationService.error('Erreur lors du chargement des produits');
        }
      });
  }

  onCategorySelect(categoryId: string | null): void {
    this.selectedCategoryId = this.selectedCategoryId === categoryId ? null : categoryId;
    this.pageIndex = 0;
    this.loadProducts();
  }

  onPageChange(event: PageEvent): void {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadProducts();
  }

  openCreateDialog(): void {
    const dialogRef = this.dialog.open(ProductDialogComponent, {
      width: '540px',
      data: {}
    });

    dialogRef.afterClosed().subscribe((createdProduct) => {
      if (createdProduct) {
        this.loadProducts();
      }
    });
  }

  openEditDialog(product: Product): void {
    const dialogRef = this.dialog.open(ProductDialogComponent, {
      width: '540px',
      data: { product }
    });

    dialogRef.afterClosed().subscribe((updatedProduct) => {
      if (updatedProduct) {
        this.loadProducts();
      }
    });
  }

  deleteProduct(product: Product): void {
    const dialogData: ConfirmDialogData = {
      title: 'Supprimer le produit',
      message: `Êtes-vous sûr de vouloir supprimer "${product.name}" du catalogue ?`,
      confirmText: 'Supprimer',
      confirmColor: 'warn'
    };

    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      width: '400px',
      data: dialogData
    });

    dialogRef.afterClosed().subscribe((confirmed) => {
      if (confirmed) {
        this.productService.deleteProduct(product.id).subscribe({
          next: () => {
            this.notificationService.success(`Produit "${product.name}" supprimé`);
            this.loadProducts();
          }
        });
      }
    });
  }
}
