import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatIconModule } from '@angular/material/icon';
import { MatTabsModule } from '@angular/material/tabs';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    MatCardModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatIconModule,
    MatTabsModule
  ],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent {
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private router = inject(Router);

  isSubmitting = false;
  hidePassword = true;

  loginForm: FormGroup = this.fb.group({
    email: ['demo@optimiam.fr', [Validators.required, Validators.email]],
    password: ['demo123', [Validators.required, Validators.minLength(6)]]
  });

  registerForm: FormGroup = this.fb.group({
    username: ['', [Validators.required, Validators.minLength(2)]],
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(6)]]
  });

  onLogin(): void {
    if (this.loginForm.invalid) return;

    this.isSubmitting = true;
    this.authService.login(this.loginForm.value).subscribe({
      next: () => {
        this.isSubmitting = false;
        this.router.navigate(['/dashboard']);
      },
      error: () => this.isSubmitting = false
    });
  }

  onRegister(): void {
    if (this.registerForm.invalid) return;

    this.isSubmitting = true;
    this.authService.register(this.registerForm.value).subscribe({
      next: () => {
        this.isSubmitting = false;
        this.router.navigate(['/dashboard']);
      },
      error: () => this.isSubmitting = false
    });
  }

  quickLogin(role: 'demo' | 'admin'): void {
    const creds = role === 'admin'
      ? { email: 'admin@optimiam.fr', password: 'admin123' }
      : { email: 'demo@optimiam.fr', password: 'demo123' };

    this.loginForm.patchValue(creds);
    this.onLogin();
  }
}
