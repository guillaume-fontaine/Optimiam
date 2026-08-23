import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { RouterModule } from '@angular/router';

import { StockItem, StockSummary } from '../../core/models/stock.model';
import { StockService } from '../../core/services/stock.service';
import { StockEntryDialogComponent } from '../stock/stock-entry-dialog/stock-entry-dialog.component';
import { StockExitDialogComponent } from '../stock/stock-exit-dialog/stock-exit-dialog.component';
import { LoadingSpinnerComponent } from '../../shared/ui/loading-spinner/loading-spinner.component';
import { EmptyStateComponent } from '../../shared/ui/empty-state/empty-state.component';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatIconModule,
    MatButtonModule,
    MatDialogModule,
    RouterModule,
    LoadingSpinnerComponent,
    EmptyStateComponent
  ],
  template: `
    <div class="dashboard-header">
      <div>
        <h1 class="page-title">Tableau de bord OptiMiam 🌱</h1>
        <p class="page-subtitle">Valorisez vos produits disponibles et limitez le gaspillage alimentaire.</p>
      </div>
      <div class="header-actions">
        <button mat-raised-button color="primary" (click)="openStockEntryDialog()">
          <mat-icon>add_shopping_cart</mat-icon>
          Ajouter au stock
        </button>
      </div>
    </div>

    <app-loading-spinner *ngIf="isLoading" message="Chargement des données..."></app-loading-spinner>

    <ng-container *ngIf="!isLoading">
      <!-- Compteurs KPI -->
      <div class="kpi-grid">
        <mat-card class="kpi-card">
          <div class="kpi-icon-box bg-blue">
            <mat-icon>inventory_2</mat-icon>
          </div>
          <div>
            <div class="kpi-value">{{ summary?.totalAvailableItems || 0 }}</div>
            <div class="kpi-label">Produits disponibles</div>
          </div>
        </mat-card>

        <mat-card class="kpi-card" [class.urgent-alert]="(summary?.expiringSoonItems || 0) > 0">
          <div class="kpi-icon-box bg-orange">
            <mat-icon>alarm</mat-icon>
          </div>
          <div>
            <div class="kpi-value">{{ summary?.expiringSoonItems || 0 }}</div>
            <div class="kpi-label">À consommer d'urgence</div>
          </div>
        </mat-card>

        <mat-card class="kpi-card">
          <div class="kpi-icon-box bg-green">
            <mat-icon>eco</mat-icon>
          </div>
          <div>
            <div class="kpi-value">{{ summary?.totalLossesWeightKg || 0 }} kg</div>
            <div class="kpi-label">Pertes déclarées (30j)</div>
          </div>
        </mat-card>
      </div>

      <div class="dashboard-widgets-grid">
        <!-- Widget 1: Produits à consommer d'urgence -->
        <mat-card class="widget-card">
          <div class="widget-header">
            <div class="widget-title">
              <mat-icon color="warn">priority_high</mat-icon>
              <span>🔴 À consommer rapidement (DLC &lt; 3 jours)</span>
            </div>
            <a mat-button color="primary" routerLink="/stock">Voir tout le stock</a>
          </div>

          <app-empty-state 
            *ngIf="expiringItems.length === 0"
            icon="task_alt"
            title="Aucun produit urgent"
            description="Super ! Tous vos produits ont une date de consommation confortable.">
          </app-empty-state>

          <div class="expiring-list" *ngIf="expiringItems.length > 0">
            <div class="expiring-item" *ngFor="let item of expiringItems">
              <div class="item-details">
                <span class="item-name">{{ item.product.name }}</span>
                <span class="item-qty">{{ item.quantity }} {{ item.unitSymbol }} • {{ item.locationLabel }}</span>
              </div>
              <div class="item-status">
                <span class="badge-urgent" *ngIf="item.status === 'EXPIRED'">Périmé</span>
                <span class="badge-warning" *ngIf="item.status === 'EXPIRING_SOON'">
                  {{ item.daysUntilExpiration === 0 ? "Expire aujourd'hui" : item.daysUntilExpiration === 1 ? 'Expire demain' : 'dans ' + item.daysUntilExpiration + ' jours' }}
                </span>
                <button mat-stroked-button color="primary" (click)="openStockExitDialog(item)">
                  Consommer
                </button>
              </div>
            </div>
          </div>
        </mat-card>

        <!-- Widget 2: Actions rapides & Raccourcis -->
        <mat-card class="widget-card">
          <div class="widget-header">
            <div class="widget-title">
              <mat-icon color="primary">flash_on</mat-icon>
              <span>⚡ Actions Rapides</span>
            </div>
          </div>

          <div class="shortcuts-grid">
            <button mat-stroked-button class="shortcut-btn" (click)="openStockEntryDialog()">
              <mat-icon color="primary">add_shopping_cart</mat-icon>
              <span>Nouvelle entrée stock</span>
            </button>
            <button mat-stroked-button class="shortcut-btn" routerLink="/recipes">
              <mat-icon color="accent">menu_book</mat-icon>
              <span>Explorer les recettes</span>
            </button>
            <button mat-stroked-button class="shortcut-btn" routerLink="/recommendations">
              <mat-icon color="primary">auto_awesome</mat-icon>
              <span>Idées de repas du jour</span>
            </button>
            <button mat-stroked-button class="shortcut-btn" routerLink="/products">
              <mat-icon color="primary">inventory_2</mat-icon>
              <span>Gérer le catalogue</span>
            </button>
          </div>
        </mat-card>
      </div>
    </ng-container>
  `,
  styles: [`
    .dashboard-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 24px;
      flex-wrap: wrap;
      gap: 16px;

      .page-title {
        margin: 0;
        font-size: 1.8rem;
        font-weight: 700;
        color: #0f172a;
      }
      .page-subtitle {
        margin: 4px 0 0 0;
        color: #64748b;
      }
    }

    .kpi-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
      gap: 16px;
      margin-bottom: 24px;

      .kpi-card {
        padding: 18px;
        border-radius: 12px;
        background-color: #ffffff;
        display: flex;
        align-items: center;
        gap: 16px;

        &.urgent-alert {
          border-left: 4px solid #ef4444;
        }

        .kpi-icon-box {
          width: 50px;
          height: 50px;
          border-radius: 12px;
          display: flex;
          align-items: center;
          justify-content: center;

          &.bg-blue { background-color: #e0f2fe; color: #0284c7; }
          &.bg-orange { background-color: #fef3c7; color: #d97706; }
          &.bg-green { background-color: #dcfce7; color: #16a34a; }
        }

        .kpi-value {
          font-size: 1.6rem;
          font-weight: 700;
          color: #0f172a;
          line-height: 1.2;
        }
        .kpi-label {
          font-size: 0.85rem;
          color: #64748b;
          font-weight: 500;
        }
      }
    }

    .dashboard-widgets-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(380px, 1fr));
      gap: 20px;

      .widget-card {
        padding: 20px;
        border-radius: 12px;
        background-color: #ffffff;

        .widget-header {
          display: flex;
          justify-content: space-between;
          align-items: center;
          margin-bottom: 16px;
          border-bottom: 1px solid #f1f5f9;
          padding-bottom: 12px;

          .widget-title {
            display: flex;
            align-items: center;
            gap: 8px;
            font-size: 1.05rem;
            font-weight: 600;
            color: #1e293b;
          }
        }

        .expiring-list {
          display: flex;
          flex-direction: column;
          gap: 12px;

          .expiring-item {
            display: flex;
            justify-content: space-between;
            align-items: center;
            padding: 10px 14px;
            background-color: #f8fafc;
            border-radius: 8px;

            .item-details {
              display: flex;
              flex-direction: column;
              .item-name { font-weight: 600; color: #1e293b; }
              .item-qty { font-size: 0.8rem; color: #64748b; }
            }

            .item-status {
              display: flex;
              align-items: center;
              gap: 12px;
            }
          }
        }

        .shortcuts-grid {
          display: grid;
          grid-template-columns: 1fr 1fr;
          gap: 12px;

          .shortcut-btn {
            height: 64px;
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            gap: 4px;
            border-radius: 8px;
            span { font-size: 0.85rem; }
          }
        }
      }
    }
  `]
})
export class DashboardComponent implements OnInit {
  private stockService = inject(StockService);
  private dialog = inject(MatDialog);

  summary: StockSummary | null = null;
  expiringItems: StockItem[] = [];
  isLoading = true;

  ngOnInit(): void {
    this.loadDashboardData();
  }

  loadDashboardData(): void {
    this.isLoading = true;
    this.stockService.getStockSummary().subscribe({
      next: (sum) => this.summary = sum
    });

    this.stockService.getExpiringStock(3).subscribe({
      next: (items) => {
        this.expiringItems = items;
        this.isLoading = false;
      },
      error: () => this.isLoading = false
    });
  }

  openStockEntryDialog(): void {
    const dialogRef = this.dialog.open(StockEntryDialogComponent, {
      width: '540px',
      data: {}
    });

    dialogRef.afterClosed().subscribe((created) => {
      if (created) {
        this.loadDashboardData();
      }
    });
  }

  openStockExitDialog(item: StockItem): void {
    const dialogRef = this.dialog.open(StockExitDialogComponent, {
      width: '460px',
      data: { stockItem: item }
    });

    dialogRef.afterClosed().subscribe((updated) => {
      if (updated) {
        this.loadDashboardData();
      }
    });
  }
}
