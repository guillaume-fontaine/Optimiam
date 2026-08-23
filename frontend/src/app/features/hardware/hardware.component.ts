import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTabsModule } from '@angular/material/tabs';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSliderModule } from '@angular/material/slider';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatChipsModule } from '@angular/material/chips';
import { MatTooltipModule } from '@angular/material/tooltip';

import { HardwareService } from '../../core/services/hardware.service';
import { ProductService } from '../../core/services/product.service';
import { NotificationService } from '../../core/services/notification.service';
import { PrintJob, ScaleMeasurement, ScanResult } from '../../core/models/hardware.model';
import { Product } from '../../core/models/product.model';
import { LoadingSpinnerComponent } from '../../shared/ui/loading-spinner/loading-spinner.component';

@Component({
  selector: 'app-hardware',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatTabsModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatSliderModule,
    MatSlideToggleModule,
    MatChipsModule,
    MatTooltipModule
  ],
  templateUrl: './hardware.component.html',
  styleUrls: ['./hardware.component.scss']
})
export class HardwareComponent implements OnInit {
  private hardwareService = inject(HardwareService);
  private productService = inject(ProductService);
  private fb = inject(FormBuilder);
  private notificationService = inject(NotificationService);

  // Balance state
  scaleMeasurement: ScaleMeasurement | null = null;
  simulatedWeight = 0.75;
  isStable = true;
  isLoadingScale = false;

  // Imprimante state
  printForm!: FormGroup;
  printHistory: PrintJob[] = [];
  products: Product[] = [];
  lastPrintJob: PrintJob | null = null;

  // Scanner state
  scanInput = '';
  scanResult: ScanResult | null = null;
  isScanning = false;

  quickBarcodes = [
    { name: 'Tomate', code: '3017620422003', icon: 'eco' },
    { name: 'Courgette', code: '3228857000166', icon: 'spa' },
    { name: 'Œufs', code: '3560070048501', icon: 'egg' },
    { name: 'Emmental', code: '3123456789012', icon: 'lunch_dining' },
    { name: 'Code Inconnu', code: '9999999999999', icon: 'help_outline' }
  ];

  ngOnInit(): void {
    this.initPrintForm();
    this.loadScale();
    this.loadPrinterHistory();
    this.loadProducts();
  }

  // --- BALANCE CONNECTÉE ---

  loadScale(): void {
    this.isLoadingScale = true;
    this.hardwareService.getScaleMeasurement().subscribe({
      next: (m) => {
        this.scaleMeasurement = m;
        this.simulatedWeight = m.weight;
        this.isStable = m.stable;
        this.isLoadingScale = false;
      },
      error: () => this.isLoadingScale = false
    });
  }

  onWeightSliderChange(val: number): void {
    this.simulatedWeight = Math.round(val * 1000) / 1000;
    this.applyWeightSimulation();
  }

  addWeight(delta: number): void {
    this.simulatedWeight = Math.max(0, Math.round((this.simulatedWeight + delta) * 1000) / 1000);
    this.applyWeightSimulation();
  }

  tare(): void {
    this.hardwareService.tareScale().subscribe({
      next: (m) => {
        this.scaleMeasurement = m;
        this.simulatedWeight = 0;
        this.notificationService.info('⚖️ Balance tarée à 0.000 kg');
      }
    });
  }

  applyWeightSimulation(): void {
    this.hardwareService.simulateWeight({
      weight: this.simulatedWeight,
      unit: 'KG',
      stable: this.isStable
    }).subscribe({
      next: (m) => this.scaleMeasurement = m
    });
  }

  // --- IMPRIMANTE ---

  private initPrintForm(): void {
    const today = new Date().toISOString().split('T')[0];
    const expiry = new Date();
    expiry.setDate(expiry.getDate() + 7);
    const expiryStr = expiry.toISOString().split('T')[0];

    this.printForm = this.fb.group({
      productName: ['Tomate', Validators.required],
      barcode: ['3017620422003'],
      quantityWithUnit: ['0.750 kg', Validators.required],
      location: ['FRIDGE', Validators.required],
      entryDate: [today, Validators.required],
      expirationDate: [expiryStr, Validators.required]
    });
  }

  private loadProducts(): void {
    this.productService.getAllProducts().subscribe({
      next: (prods) => this.products = prods
    });
  }

  private loadPrinterHistory(): void {
    this.hardwareService.getPrintHistory().subscribe({
      next: (history) => {
        this.printHistory = history;
        if (history.length > 0) this.lastPrintJob = history[0];
      }
    });
  }

  onProductSelectForPrint(productId: string): void {
    const p = this.products.find(prod => prod.id === productId);
    if (p) {
      this.printForm.patchValue({
        productName: p.name,
        barcode: p.barcode || '3017620422003'
      });
    }
  }

  printCurrentLabel(): void {
    if (this.printForm.invalid) return;

    this.hardwareService.printLabel(this.printForm.value).subscribe({
      next: (job) => {
        this.lastPrintJob = job;
        this.loadPrinterHistory();
        this.notificationService.success(`🏷️ Étiquette pour "${job.productName}" imprimée avec succès !`);
      }
    });
  }

  // --- SCANNER ---

  scan(barcode?: string): void {
    const code = barcode || this.scanInput;
    if (!code || !code.trim()) return;

    this.isScanning = true;
    this.scanInput = code;

    this.hardwareService.scanBarcode(code).subscribe({
      next: (res) => {
        this.scanResult = res;
        this.isScanning = false;
        if (res.productFound) {
          this.notificationService.success(`📱 Scan réussi : ${res.matchedProduct?.name}`);
        } else {
          this.notificationService.warning(`⚠️ Produit inconnu pour le code "${code}"`);
        }
      },
      error: () => this.isScanning = false
    });
  }
}
