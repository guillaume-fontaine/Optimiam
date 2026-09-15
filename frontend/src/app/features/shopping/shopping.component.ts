import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatInputModule } from '@angular/material/input';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatTooltipModule } from '@angular/material/tooltip';

import { ShoppingList, ShoppingListItem } from '../../core/models/shopping.model';
import { ShoppingService } from '../../core/services/shopping.service';
import { NotificationService } from '../../core/services/notification.service';
import { ShoppingItemDialogComponent } from './shopping-item-dialog/shopping-item-dialog.component';
import { GenerateShoppingDialogComponent } from './generate-shopping-dialog/generate-shopping-dialog.component';
import { ConfirmDialogComponent, ConfirmDialogData } from '../../shared/ui/confirm-dialog/confirm-dialog.component';
import { LoadingSpinnerComponent } from '../../shared/ui/loading-spinner/loading-spinner.component';
import { EmptyStateComponent } from '../../shared/ui/empty-state/empty-state.component';

export interface CategoryGroup {
  categoryName: string;
  categoryIcon: string;
  items: ShoppingListItem[];
}

@Component({
  selector: 'app-shopping',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatCheckboxModule,
    MatInputModule,
    MatProgressBarModule,
    MatDialogModule,
    MatTooltipModule,
    LoadingSpinnerComponent,
    EmptyStateComponent
  ],
  templateUrl: './shopping.component.html',
  styleUrls: ['./shopping.component.scss']
})
export class ShoppingComponent implements OnInit {
  private shoppingService = inject(ShoppingService);
  private dialog = inject(MatDialog);
  private notificationService = inject(NotificationService);

  activeList: ShoppingList | null = null;
  categoryGroups: CategoryGroup[] = [];
  isLoading = true;

  ngOnInit(): void {
    this.loadActiveList();
  }

  loadActiveList(): void {
    this.isLoading = true;
    this.shoppingService.getActiveShoppingList().subscribe({
      next: (list) => {
        this.activeList = list;
        this.groupItemsByCategory();
        this.isLoading = false;
      },
      error: () => {
        this.isLoading = false;
        this.notificationService.error('Erreur lors du chargement de la liste de courses');
      }
    });
  }

  private groupItemsByCategory(): void {
    if (!this.activeList || !this.activeList.items) {
      this.categoryGroups = [];
      return;
    }

    const groupsMap = new Map<string, { icon: string; items: ShoppingListItem[] }>();

    for (const item of this.activeList.items) {
      const catName = item.product.category?.name || 'Divers & Épicerie';
      const catIcon = item.product.category?.icon || 'shopping_bag';

      if (!groupsMap.has(catName)) {
        groupsMap.set(catName, { icon: catIcon, items: [] });
      }
      groupsMap.get(catName)!.items.push(item);
    }

    this.categoryGroups = Array.from(groupsMap.entries())
      .sort(([nameA], [nameB]) => nameA.localeCompare(nameB, 'fr'))
      .map(([name, data]) => ({
        categoryName: name,
        categoryIcon: data.icon,
        items: [...data.items].sort((a, b) => a.product.name.localeCompare(b.product.name, 'fr'))
      }));
  }

  toggleItem(item: ShoppingListItem): void {
    if (!this.activeList) return;
    const newChecked = !item.checked;

    this.shoppingService.updateItem(this.activeList.id, item.id, {
      checked: newChecked,
      purchasedQuantity: item.purchasedQuantity
    }).subscribe({
      next: (updatedList) => {
        this.activeList = updatedList;
        this.groupItemsByCategory();
      }
    });
  }

  updatePurchasedQuantity(item: ShoppingListItem, event: Event): void {
    if (!this.activeList) return;

    const input = event.target as HTMLInputElement;
    const purchasedQuantity = Number(input.value);
    if (!Number.isFinite(purchasedQuantity) || purchasedQuantity <= 0) return;

    item.purchasedQuantity = purchasedQuantity;
    this.shoppingService.updateItem(this.activeList.id, item.id, { purchasedQuantity }).subscribe({
      next: (updatedList) => {
        this.activeList = updatedList;
        this.groupItemsByCategory();
      }
    });
  }

  quantityStep(unit: string): number {
    switch (unit) {
      case 'PIECE':
        return 1;
      case 'KG':
        return 0.5;
      case 'G':
      case 'ML':
        return 50;
      default:
        return 1;
    }
  }

  openAddItemDialog(): void {
    if (!this.activeList) return;
    const dialogRef = this.dialog.open(ShoppingItemDialogComponent, {
      width: '480px',
      data: { shoppingListId: this.activeList.id }
    });

    dialogRef.afterClosed().subscribe((updatedList) => {
      if (updatedList) {
        this.activeList = updatedList;
        this.groupItemsByCategory();
      }
    });
  }

  openGenerateDialog(): void {
    const dialogRef = this.dialog.open(GenerateShoppingDialogComponent, {
      width: '500px'
    });

    dialogRef.afterClosed().subscribe((generated) => {
      if (generated) {
        this.activeList = generated;
        this.groupItemsByCategory();
      }
    });
  }

  validatePurchases(): void {
    if (!this.activeList) return;

    const checkedCount = this.activeList.items.filter(i => i.checked).length;
    if (checkedCount === 0) {
      this.notificationService.warning('Veuillez cocher au moins un article acheté à transférer dans votre stock.');
      return;
    }

    const dialogData: ConfirmDialogData = {
      title: 'Valider mes achats',
      message: `Voulez-vous transférer les ${checkedCount} articles cochés directement dans votre stock disponible ?`,
      confirmText: 'Ajouter au Stock',
      confirmColor: 'primary'
    };

    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      width: '440px',
      data: dialogData
    });

    dialogRef.afterClosed().subscribe((confirmed) => {
      if (confirmed && this.activeList) {
        this.shoppingService.validatePurchases(this.activeList.id).subscribe({
          next: () => {
            this.notificationService.success(`🎉 ${checkedCount} articles ont été ajoutés à votre stock !`);
            this.loadActiveList();
          }
        });
      }
    });
  }
}
