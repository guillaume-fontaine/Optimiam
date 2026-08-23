import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';

@Component({
  selector: 'app-empty-state',
  standalone: true,
  imports: [CommonModule, MatIconModule, MatButtonModule],
  template: `
    <div class="empty-state-container">
      <mat-icon class="empty-icon">{{ icon }}</mat-icon>
      <h3 class="empty-title">{{ title }}</h3>
      <p class="empty-description" *ngIf="description">{{ description }}</p>
      <ng-content></ng-content>
    </div>
  `,
  styles: [`
    .empty-state-container {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      text-align: center;
      padding: 48px 16px;
      color: #64748b;

      .empty-icon {
        font-size: 56px;
        width: 56px;
        height: 56px;
        color: #94a3b8;
        margin-bottom: 12px;
      }
      .empty-title {
        font-size: 1.15rem;
        font-weight: 600;
        color: #334155;
        margin: 0 0 6px 0;
      }
      .empty-description {
        font-size: 0.9rem;
        margin: 0 0 16px 0;
        max-width: 400px;
      }
    }
  `]
})
export class EmptyStateComponent {
  @Input() icon = 'inbox';
  @Input() title = 'Aucun élément trouvé';
  @Input() description?: string;
}
