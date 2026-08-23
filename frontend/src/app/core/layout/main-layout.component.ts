import { Component, inject, viewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatSidenavModule, MatSidenav } from '@angular/material/sidenav';
import { MatListModule } from '@angular/material/list';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { BreakpointObserver, Breakpoints } from '@angular/cdk/layout';
import { toSignal } from '@angular/core/rxjs-interop';
import { map } from 'rxjs';

interface NavItem {
  label: string;
  route: string;
  icon: string;
}

@Component({
  selector: 'app-main-layout',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatToolbarModule,
    MatSidenavModule,
    MatListModule,
    MatIconModule,
    MatButtonModule
  ],
  templateUrl: './main-layout.component.html',
  styleUrls: ['./main-layout.component.scss']
})
export class MainLayoutComponent {
  private breakpointObserver = inject(BreakpointObserver);
  private router = inject(Router);

  readonly isHandset = toSignal(
    this.breakpointObserver.observe([Breakpoints.Handset]).pipe(
      map(result => result.matches)
    ),
    { initialValue: false }
  );

  navItems: NavItem[] = [
    { label: 'Tableau de bord', route: '/dashboard', icon: 'dashboard' },
    { label: 'Catalogue Produits', route: '/products', icon: 'inventory_2' },
    { label: 'Mon Stock', route: '/stock', icon: 'kitchen' },
    { label: 'Recettes', route: '/recipes', icon: 'menu_book' },
    { label: 'Recommandations', route: '/recommendations', icon: 'auto_awesome' },
    { label: 'Planning Repas', route: '/planning', icon: 'calendar_month' },
    { label: 'Liste de Courses', route: '/shopping', icon: 'shopping_cart' },
    { label: 'Hardware Simulé', route: '/hardware', icon: 'scale' }
  ];

  onNavItemClick(drawer: MatSidenav): void {
    if (this.isHandset()) {
      drawer.close();
    }
  }
}
