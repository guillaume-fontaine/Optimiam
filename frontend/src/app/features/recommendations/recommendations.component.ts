import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-recommendations',
  standalone: true,
  imports: [CommonModule],
  template: `<div class="feature-container"><h2>Recommandations Intelligentes</h2><p>Moteur de Scoring Déterministe (Sprint 4)</p></div>`
})
export class RecommendationsComponent {}
