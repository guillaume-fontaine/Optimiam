import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-planning',
  standalone: true,
  imports: [CommonModule],
  template: `<div class="feature-container"><h2>Planning des Repas</h2><p>Calendrier Hebdomadaire (Sprint 5)</p></div>`
})
export class PlanningComponent {}
