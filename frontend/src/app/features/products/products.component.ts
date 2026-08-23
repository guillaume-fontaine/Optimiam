import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-products',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="feature-container">
      <h2>Catalogue Produits</h2>
      <p>Module Produits (Sprint 1)</p>
    </div>
  `
})
export class ProductsComponent {}
