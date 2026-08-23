import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { SyncManagerService } from '../../../core/offline/sync-manager.service';

@Component({
  selector: 'app-sync-status-indicator',
  standalone: true,
  imports: [
    CommonModule,
    MatButtonModule,
    MatIconModule,
    MatTooltipModule
  ],
  template: `
    <div class="sync-badge" 
      [ngClass]="'status-' + (syncStatus$ | async)?.toLowerCase()"
      [matTooltip]="getTooltipText((syncStatus$ | async), (pendingCount$ | async), (conflictCount$ | async))"
      (click)="onSyncClick()">
      
      <mat-icon class="sync-icon" [class.spinning]="(syncStatus$ | async) === 'SYNCING'">
        <ng-container [ngSwitch]="syncStatus$ | async">
          <span *ngSwitchCase="'ONLINE'">cloud_done</span>
          <span *ngSwitchCase="'SYNCING'">sync</span>
          <span *ngSwitchCase="'OFFLINE'">cloud_off</span>
          <span *ngSwitchCase="'CONFLICT'">sync_problem</span>
          <span *ngSwitchDefault>cloud_queue</span>
        </ng-container>
      </mat-icon>

      <span class="sync-text">
        <ng-container [ngSwitch]="syncStatus$ | async">
          <span *ngSwitchCase="'ONLINE'">En ligne</span>
          <span *ngSwitchCase="'SYNCING'">Sync...</span>
          <span *ngSwitchCase="'OFFLINE'">Hors-ligne ({{ pendingCount$ | async }})</span>
          <span *ngSwitchCase="'CONFLICT'">Conflit ({{ conflictCount$ | async }})</span>
          <span *ngSwitchDefault>Connecté</span>
        </ng-container>
      </span>
    </div>
  `,
  styles: [`
    .sync-badge {
      display: inline-flex;
      align-items: center;
      gap: 6px;
      padding: 4px 10px;
      border-radius: 16px;
      font-size: 0.78rem;
      font-weight: 600;
      cursor: pointer;
      user-select: none;
      transition: all 0.2s ease;

      .sync-icon {
        font-size: 16px;
        width: 16px;
        height: 16px;
        line-height: 16px;

        &.spinning {
          animation: spin 1s linear infinite;
        }
      }

      &.status-online {
        background-color: #dcfce7;
        color: #15803d;
        border: 1px solid #bbf7d0;
      }

      &.status-syncing {
        background-color: #e0f2fe;
        color: #0369a1;
        border: 1px solid #bae6fd;
      }

      &.status-offline {
        background-color: #fef3c7;
        color: #b45309;
        border: 1px solid #fde68a;
      }

      &.status-conflict {
        background-color: #fee2e2;
        color: #b91c1c;
        border: 1px solid #fecaca;
      }

      &:hover {
        opacity: 0.9;
        transform: translateY(-1px);
      }
    }

    @keyframes spin {
      from { transform: rotate(0deg); }
      to { transform: rotate(360deg); }
    }
  `]
})
export class SyncStatusIndicatorComponent {
  private syncManager = inject(SyncManagerService);

  syncStatus$ = this.syncManager.syncStatus$;
  pendingCount$ = this.syncManager.pendingCount$;
  conflictCount$ = this.syncManager.conflictCount$;

  onSyncClick(): void {
    this.syncManager.triggerSync();
  }

  getTooltipText(status: string | null, pending: number | null, conflicts: number | null): string {
    switch (status) {
      case 'ONLINE': return 'Connecté au serveur. Toutes les données sont synchronisées.';
      case 'SYNCING': return 'Synchronisation des modifications avec le serveur en cours...';
      case 'OFFLINE': return `Mode déconnecté. ${pending || 0} modification(s) en attente de synchronisation.`;
      case 'CONFLICT': return `Attention : ${conflicts || 0} conflit(s) détecté(s). Cliquez pour tenter une réconciliation.`;
      default: return 'État de synchronisation';
    }
  }
}
