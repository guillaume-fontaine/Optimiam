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
import { MatTabsModule } from '@angular/material/tabs';
import { debounceTime, distinctUntilChanged } from 'rxjs';

import { Location, LOCATION_OPTIONS, StockItem, StockSummary, StockTransaction, TransactionType } from '../../core/models/stock.model';
import { StockService } from '../../core/services/stock.service';
import { NotificationService } from '../../core/services/notification.service';
import { StockEntryDialogComponent } from './stock-entry-dialog/stock-entry-dialog.component';
import { StockExitDialogComponent } from './stock-exit-dialog/stock-exit-dialog.component';
import { StockLossDialogComponent } from './stock-loss-dialog/stock-loss-dialog.component';
import { ConfirmDialogComponent, ConfirmDialogData } from '../../shared/ui/confirm-dialog/confirm-dialog.component';
import { LoadingSpinnerComponent } from '../../shared/ui/loading-spinner/loading-spinner.component';
import { EmptyStateComponent } from '../../shared/ui/empty-state/empty-state.component';

@Component({
  selector: 'app-stock',
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
    MatTabsModule,
    LoadingSpinnerComponent,
    EmptyStateComponent
  ],
  templateUrl: './stock.component.html',
  styleUrls: ['./stock.component.scss']
})
export class StockComponent implements OnInit {
  private stockService = inject(StockService);
  private dialog = inject(MatDialog);
  private notificationService = inject(NotificationService);

  searchControl = new FormControl('');
  selectedLocation: Location | null = null;
  locationOptions = LOCATION_OPTIONS;

  stockItems: StockItem[] = [];
  summary: StockSummary | null = null;
  transactions: StockTransaction[] = [];

  isLoadingStock = true;
  isLoadingTx = false;

  totalStockElements = 0;
  stockPageSize = 10;
  stockPageIndex = 0;

  totalTxElements = 0;
  txPageSize = 10;
  txPageIndex = 0;

  displayedStockColumns: string[] = ['product', 'quantity', 'location', 'expiration', 'status', 'actions'];
  displayedTxColumns: string[] = ['timestamp', 'type', 'product', 'quantity', 'reason'];

  ngOnInit(): void {
    this.loadSummary();
    this.loadStock();

    this.searchControl.valueChanges
      .pipe(debounceTime(350), distinctUntilChanged())
      .subscribe(() => {
        this.stockPageIndex = 0;
        this.loadStock();
      });
  }

  loadSummary(): void {
    this.stockService.getStockSummary().subscribe({
      next: (sum) => this.summary = sum,
      error: () => {}
    });
  }

  loadStock(): void {
    this.isLoadingStock = true;
    const query = this.searchControl.value || undefined;
    const location = this.selectedLocation || undefined;

    this.stockService.getStockItems(location, query, this.stockPageIndex, this.stockPageSize)
      .subscribe({
        next: (page) => {
          this.stockItems = page.content;
          this.totalStockElements = page.totalElements;
          this.isLoadingStock = false;
        },
        error: () => {
          this.isLoadingStock = false;
          this.notificationService.error('Erreur lors du chargement des stocks');
        }
      });
  }

  loadTransactions(): void {
    this.isLoadingTx = true;
    this.stockService.getTransactions(undefined, this.txPageIndex, this.txPageSize)
      .subscribe({
        next: (page) => {
          this.transactions = page.content;
          this.totalTxElements = page.totalElements;
          this.isLoadingTx = false;
        },
        error: () => {
          this.isLoadingTx = false;
          this.notificationService.error('Erreur lors du chargement de l\'historique');
        }
      });
  }

  onTabChange(index: number): void {
    if (index === 1 && this.transactions.length === 0) {
      this.loadTransactions();
    }
  }

  onLocationSelect(loc: Location | null): void {
    this.selectedLocation = this.selectedLocation === loc ? null : loc;
    this.stockPageIndex = 0;
    this.loadStock();
  }

  onStockPageChange(event: PageEvent): void {
    this.stockPageIndex = event.pageIndex;
    this.stockPageSize = event.pageSize;
    this.loadStock();
  }

  onTxPageChange(event: PageEvent): void {
    this.txPageIndex = event.pageIndex;
    this.txPageSize = event.pageSize;
    this.loadTransactions();
  }

  openEntryDialog(): void {
    const dialogRef = this.dialog.open(StockEntryDialogComponent, {
      width: '540px',
      data: {}
    });

    dialogRef.afterClosed().subscribe((createdItem) => {
      if (createdItem) {
        this.loadStock();
        this.loadSummary();
      }
    });
  }

  openExitDialog(item: StockItem): void {
    const dialogRef = this.dialog.open(StockExitDialogComponent, {
      width: '460px',
      data: { stockItem: item }
    });

    dialogRef.afterClosed().subscribe((updatedItem) => {
      if (updatedItem) {
        this.loadStock();
        this.loadSummary();
      }
    });
  }

  openLossDialog(item: StockItem): void {
    const dialogRef = this.dialog.open(StockLossDialogComponent, {
      width: '480px',
      data: { stockItem: item }
    });

    dialogRef.afterClosed().subscribe((updatedItem) => {
      if (updatedItem) {
        this.loadStock();
        this.loadSummary();
      }
    });
  }

  deleteStockItem(item: StockItem): void {
    const dialogData: ConfirmDialogData = {
      title: 'Supprimer cet élément de stock',
      message: `Êtes-vous sûr de vouloir retirer "${item.product.name}" (${item.quantity} ${item.unitSymbol}) du stock ?`,
      confirmText: 'Supprimer',
      confirmColor: 'warn'
    };

    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      width: '420px',
      data: dialogData
    });

    dialogRef.afterClosed().subscribe((confirmed) => {
      if (confirmed) {
        this.stockService.deleteStockItem(item.id).subscribe({
          next: () => {
            this.notificationService.success(`Produit retiré du stock`);
            this.loadStock();
            this.loadSummary();
          }
        });
      }
    });
  }
}
