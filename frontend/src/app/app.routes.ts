import { Routes } from '@angular/router';
import { MainLayoutComponent } from './core/layout/main-layout.component';
import { DashboardComponent } from './features/dashboard/dashboard.component';
import { ProductsComponent } from './features/products/products.component';
import { StockComponent } from './features/stock/stock.component';
import { RecipesComponent } from './features/recipes/recipes.component';
import { RecommendationsComponent } from './features/recommendations/recommendations.component';
import { PlanningComponent } from './features/planning/planning.component';
import { ShoppingComponent } from './features/shopping/shopping.component';
import { HardwareComponent } from './features/hardware/hardware.component';
import { LoginComponent } from './features/auth/login/login.component';

export const routes: Routes = [
  { path: 'login', component: LoginComponent },
  {
    path: '',
    component: MainLayoutComponent,
    children: [
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
      { path: 'dashboard', component: DashboardComponent },
      { path: 'products', component: ProductsComponent },
      { path: 'stock', component: StockComponent },
      { path: 'recipes', component: RecipesComponent },
      { path: 'recommendations', component: RecommendationsComponent },
      { path: 'planning', component: PlanningComponent },
      { path: 'shopping', component: ShoppingComponent },
      { path: 'hardware', component: HardwareComponent }
    ]
  },
  { path: '**', redirectTo: '' }
];
