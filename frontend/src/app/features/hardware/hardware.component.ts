import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-hardware',
  standalone: true,
  imports: [CommonModule],
  template: `<div class="feature-container"><h2>Hardware Simulé</h2><p>Simulateurs Balance, Ticket et Scanner (Sprint 8)</p></div>`
})
export class HardwareComponent {}
