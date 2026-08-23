import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, MatCardModule, MatIconModule, MatButtonModule, RouterModule],
  template: `
    <div class="dashboard-header">
      <h1>Bienvenue sur OptiMiam 🌱</h1>
      <p class="subtitle">Gérez vos produits, réduisez vos pertes et découvrez des recettes personnalisées.</p>
    </div>

    <div class="kpi-grid">
      <mat-card class="kpi-card">
        <mat-card-header>
          <mat-icon mat-card-avatar color="primary">inventory_2</mat-icon>
          <mat-card-title>126</mat-card-title>
          <mat-card-subtitle>Produits en stock</mat-card-subtitle>
        </mat-card-header>
      </mat-card>

      <mat-card class="kpi-card urgent">
        <mat-card-header>
          <mat-icon mat-card-avatar color="warn">warning</mat-icon>
          <mat-card-title>8</mat-card-title>
          <mat-card-subtitle>À consommer d'urgence</mat-card-subtitle>
        </mat-card-header>
      </mat-card>

      <mat-card class="kpi-card">
        <mat-card-header>
          <mat-icon mat-card-avatar color="accent">eco</mat-icon>
          <mat-card-title>2.4 kg</mat-card-title>
          <mat-card-subtitle>Pertes évitées ce mois</mat-card-subtitle>
        </mat-card-header>
      </mat-card>
    </div>
  `,
  styles: [`
    .dashboard-header {
      margin-bottom: 24px;
      h1 { margin: 0; font-size: 1.8rem; font-weight: 700; color: #1e293b; }
      .subtitle { color: #64748b; margin-top: 4px; }
    }
    .kpi-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
      gap: 16px;
      margin-bottom: 24px;
    }
    .kpi-card {
      padding: 16px;
      border-radius: 12px;
      mat-card-title { font-size: 1.75rem; font-weight: 700; }
      &.urgent {
        border-left: 4px solid #ef4444;
      }
    }
  `]
})
export class DashboardComponent {}
