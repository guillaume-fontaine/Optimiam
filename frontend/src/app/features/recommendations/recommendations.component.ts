import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatTooltipModule } from '@angular/material/tooltip';

import { DIFFICULTY_OPTIONS, Recipe } from '../../core/models/recipe.model';
import { Recommendation } from '../../core/models/recommendation.model';
import { RecommendationService } from '../../core/services/recommendation.service';
import { StockService } from '../../core/services/stock.service';
import { NotificationService } from '../../core/services/notification.service';
import { RecipeDetailDialogComponent } from '../recipes/recipe-detail-dialog/recipe-detail-dialog.component';
import { LoadingSpinnerComponent } from '../../shared/ui/loading-spinner/loading-spinner.component';
import { EmptyStateComponent } from '../../shared/ui/empty-state/empty-state.component';

@Component({
  selector: 'app-recommendations',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatChipsModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatSlideToggleModule,
    MatProgressBarModule,
    MatDialogModule,
    MatTooltipModule,
    LoadingSpinnerComponent,
    EmptyStateComponent
  ],
  templateUrl: './recommendations.component.html',
  styleUrls: ['./recommendations.component.scss']
})
export class RecommendationsComponent implements OnInit {
  private fb = inject(FormBuilder);
  private recommendationService = inject(RecommendationService);
  private stockService = inject(StockService);
  private dialog = inject(MatDialog);
  private notificationService = inject(NotificationService);

  filterForm!: FormGroup;
  recommendations: Recommendation[] = [];
  isLoading = true;

  difficultyOptions = DIFFICULTY_OPTIONS;
  popularTags = ['Anti-gaspi', 'Végétarien', 'Rapide', 'Méditerranéen', 'Protéiné'];

  ngOnInit(): void {
    this.initForm();
    this.loadRecommendations();
  }

  private initForm(): void {
    this.filterForm = this.fb.group({
      servings: [null],
      maxTimeMinutes: [null],
      difficulty: [null],
      tag: [null],
      onlyFullStock: [false]
    });

    this.filterForm.valueChanges.subscribe(() => {
      this.loadRecommendations();
    });
  }

  loadRecommendations(): void {
    this.isLoading = true;
    const formVal = this.filterForm.value;

    const request = {
      servings: formVal.servings || undefined,
      maxTimeMinutes: formVal.maxTimeMinutes || undefined,
      difficulty: formVal.difficulty || undefined,
      tag: formVal.tag || undefined,
      onlyFullStock: formVal.onlyFullStock
    };

    this.recommendationService.getRecommendations(request).subscribe({
      next: (recs) => {
        this.recommendations = recs;
        this.isLoading = false;
      },
      error: () => {
        this.isLoading = false;
        this.notificationService.error('Erreur lors du calcul des recommandations');
      }
    });
  }

  onTagToggle(tag: string): void {
    const current = this.filterForm.get('tag')?.value;
    this.filterForm.patchValue({ tag: current === tag ? null : tag });
  }

  onMaxTimeSelect(time: number | null): void {
    this.filterForm.patchValue({ maxTimeMinutes: time });
  }

  openDetailDialog(recipe: Recipe): void {
    this.dialog.open(RecipeDetailDialogComponent, {
      width: '680px',
      data: { recipe }
    });
  }

  cookRecipe(rec: Recommendation): void {
    this.notificationService.success(`Plat "${rec.recipe.name}" préparé ! Ingrédients mis à jour.`);
    this.loadRecommendations();
  }
}
