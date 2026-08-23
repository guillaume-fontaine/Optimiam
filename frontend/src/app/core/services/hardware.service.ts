import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_CONFIG } from '../http/api-config';
import { PrintJob, PrintLabelDto, ScaleMeasurement, ScanResult, SimulateWeightRequest } from '../models/hardware.model';

@Injectable({
  providedIn: 'root'
})
export class HardwareService {
  private http = inject(HttpClient);
  private apiUrl = `${API_CONFIG.baseUrl}/hardware`;

  getScaleMeasurement(): Observable<ScaleMeasurement> {
    return this.http.get<ScaleMeasurement>(`${this.apiUrl}/scale/measure`);
  }

  tareScale(): Observable<ScaleMeasurement> {
    return this.http.post<ScaleMeasurement>(`${this.apiUrl}/scale/tare`, {});
  }

  simulateWeight(request: SimulateWeightRequest): Observable<ScaleMeasurement> {
    return this.http.post<ScaleMeasurement>(`${this.apiUrl}/scale/simulate`, request);
  }

  printLabel(dto: PrintLabelDto): Observable<PrintJob> {
    return this.http.post<PrintJob>(`${this.apiUrl}/printer/print-label`, dto);
  }

  getPrintHistory(): Observable<PrintJob[]> {
    return this.http.get<PrintJob[]>(`${this.apiUrl}/printer/jobs`);
  }

  scanBarcode(barcode: string): Observable<ScanResult> {
    return this.http.post<ScanResult>(`${this.apiUrl}/scanner/scan`, { barcode });
  }
}
