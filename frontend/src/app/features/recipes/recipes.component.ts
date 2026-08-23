import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormControl } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { debounceTime, distinctUntilChanged } from 'rxjs';

import { Difficulty, DIFFICULTY_OPTIONS, Recipe } from '../../core/models/recipe.model';
import { RecipeService } from '../../core/services/recipe.service';
import { NotificationService } from '../../core/services/notification.service';
import { RecipeDetailDialogComponent } from './recipe-detail-dialog/recipe-detail-dialog.component';
import { RecipeFormDialogComponent } from './recipe-form-dialog/recipe-form-dialog.component';
import { ConfirmDialogComponent, ConfirmDialogData } from '../../shared/ui/confirm-dialog/confirm-dialog.component';
import { LoadingSpinnerComponent } from '../../shared/ui/loading-spinner/loading-spinner.component';
import { EmptyStateComponent } from '../../shared/ui/empty-state/empty-state.component';

@Component({
  selector: 'app-recipes',
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
    MatDialogModule,
    MatTooltipModule,
    MatPaginatorModule,
    LoadingSpinnerComponent,
    EmptyStateComponent
  ],
  templateUrl: './recipes.component.html',
  styleUrls: ['./recipes.component.scss']
})
export class RecipesComponent implements OnInit {
  private recipeService = inject(RecipeService);
  private dialog = inject(MatDialog);
  private notificationService = inject(NotificationService);

  searchControl = new FormControl('');
  difficultyOptions = DIFFICULTY_OPTIONS;
  popularTags = ['Anti-gaspi', 'Végétarien', 'Rapide', 'Méditerranéen', 'Protéiné', 'Express'];

  selectedTag: string | null = null;
  selectedDifficulty: Difficulty | null = null;

  recipes: Recipe[] = [];
  isLoading = true;

  totalElements = 0;
  pageSize = 12;
  pageIndex = 0;

  ngOnInit(): void {
    this.loadRecipes();

    this.searchControl.valueChanges
      .pipe(debounceTime(350), distinctUntilChanged())
      .subscribe(() => {
        this.pageIndex = 0;
        this.loadRecipes();
      });
  }

  loadRecipes(): void {
    this.isLoading = true;
    const query = this.searchControl.value || undefined;
    const tag = this.selectedTag || undefined;
    const difficulty = this.selectedDifficulty || undefined;

    this.recipeService.getRecipes(query, tag, undefined, difficulty, this.pageIndex, this.pageSize)
      .subscribe({
        next: (page) => {
          this.recipes = page.content;
          this.totalElements = page.totalElements;
          this.isLoading = false;
        },
        error: () => {
          this.isLoading = false;
          this.notificationService.error('Erreur lors du chargement des recettes');
        }
      });
  }

  onTagSelect(tag: string | null): void {
    this.selectedTag = this.selectedTag === tag ? null : tag;
    this.pageIndex = 0;
    this.loadRecipes();
  }

  onDifficultySelect(difficulty: Difficulty | null): void {
    this.selectedDifficulty = difficulty;
    this.pageIndex = 0;
    this.loadRecipes();
  }

  onPageChange(event: PageEvent): void {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadRecipes();
  }

  openDetailDialog(recipe: Recipe): void {
    this.dialog.open(RecipeDetailDialogComponent, {
      width: '680px',
      data: { recipe }
    });
  }

  openCreateDialog(): void {
    const dialogRef = this.dialog.open(RecipeFormDialogComponent, {
      width: '680px',
      data: {}
    });

    dialogRef.afterClosed().subscribe((created) => {
      if (created) this.loadRecipes();
    });
  }

  openEditDialog(recipe: Recipe, event: Event): void {
    event.stopPropagation();
    const dialogRef = this.dialog.open(RecipeFormDialogComponent, {
      width: '680px',
      data: { recipe }
    });

    dialogRef.afterClosed().subscribe((updated) => {
      if (updated) this.loadRecipes();
    });
  }

  deleteRecipe(recipe: Recipe, event: Event): void {
    event.stopPropagation();
    const dialogData: ConfirmDialogData = {
      title: 'Supprimer la recette',
      message: `Êtes-vous sûr de vouloir supprimer la recette "${recipe.name}" ?`,
      confirmText: 'Supprimer',
      confirmColor: 'warn'
    };

    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      width: '420px',
      data: dialogData
    });

    dialogRef.afterClosed().subscribe((confirmed) => {
      if (confirmed) {
        this.recipeService.deleteRecipe(recipe.id).subscribe({
          next: () => {
            this.notificationService.success(`Recette "${recipe.name}" supprimée`);
            this.loadRecipes();
          }
        });
      }
    });
  }
}
