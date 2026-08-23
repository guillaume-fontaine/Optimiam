import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-stock',
  standalone: true,
  imports: [CommonModule],
  template: `<div class="feature-container"><h2>Gestion des Stocks</h2><p>Module Stock & DLC (Sprint 2)</p></div>`
})
export class StockComponent {}
