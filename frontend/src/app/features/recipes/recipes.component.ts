import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-recipes',
  standalone: true,
  imports: [CommonModule],
  template: `<div class="feature-container"><h2>Catalogue Recettes</h2><p>Module Recettes & Nutrition (Sprint 3)</p></div>`
})
export class RecipesComponent {}
