import { Component, signal, inject, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { LucideAngularModule } from 'lucide-angular';
import { ToastService } from '../../../core/services/toast.service';
import { environment } from '../../../../environments/environment';

@Component({
  selector: 'app-forgot-password',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink, LucideAngularModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="min-h-screen flex items-center justify-center bg-gradient-to-br from-indigo-50 via-white to-purple-50 px-4 py-12">
      <div class="w-full max-w-md">

        <div class="text-center mb-8">
          <div class="inline-flex items-center justify-center w-16 h-16 rounded-2xl bg-gradient-to-br from-indigo-600 to-purple-600 shadow-lg mb-4">
            <lucide-icon name="key-round" class="w-8 h-8 text-white"></lucide-icon>
          </div>
          <h1 class="text-3xl font-extrabold text-gray-900 tracking-tight">Forgot password?</h1>
          <p class="text-gray-600 mt-2">Enter your email and we'll send a 6-digit reset code.</p>
        </div>

        <div class="card">
          <form [formGroup]="form" (ngSubmit)="submit()">

            <div class="mb-5">
              <label class="form-label" for="email">
                <lucide-icon name="mail" [size]="16" class="inline mr-1"></lucide-icon>
                Email Address
              </label>
              <input id="email" type="email" formControlName="email"
                class="form-input"
                [class.error]="form.get('email')?.touched && form.get('email')?.invalid"
                placeholder="your@email.com" />
              @if (form.get('email')?.touched && form.get('email')?.invalid) {
                <div class="form-error">
                  <lucide-icon name="alert-circle" [size]="14"></lucide-icon>
                  Valid email is required
                </div>
              }
            </div>

            @if (errorMessage()) {
              <div class="alert alert-error mb-5">
                <lucide-icon name="alert-circle" [size]="18"></lucide-icon>
                <span>{{ errorMessage() }}</span>
              </div>
            }

            <button type="submit" class="btn btn-primary w-full btn-lg" [disabled]="loading() || form.invalid">
              @if (loading()) {
                <lucide-icon name="loader-2" [size]="20" class="animate-spin"></lucide-icon>
                <span>Sending...</span>
              } @else {
                <lucide-icon name="send" [size]="18"></lucide-icon>
                <span>Send Reset Code</span>
              }
            </button>

            <p class="text-center text-sm text-gray-600 mt-6">
              <a routerLink="/login" class="font-semibold text-indigo-600 hover:text-indigo-700 transition-colors">
                ← Back to login
              </a>
            </p>

          </form>
        </div>

      </div>
    </div>
  `
})
export class ForgotPasswordComponent {
  private fb     = inject(FormBuilder);
  private http   = inject(HttpClient);
  private router = inject(Router);
  private toast  = inject(ToastService);

  loading      = signal(false);
  errorMessage = signal('');

  form = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.email]]
  });

  submit() {
    if (this.form.invalid) return;
    this.loading.set(true);
    this.errorMessage.set('');
    const { email } = this.form.getRawValue();
    this.http.post(`${environment.apiUrl}/api/users/forgot-password`, { email }).subscribe({
      next: () => {
        this.loading.set(false);
        this.toast.success('Reset code sent! Check your email.');
        this.router.navigate(['/reset-password'], { queryParams: { email } });
      },
      error: () => {
        // Navigate anyway — don't reveal if email exists
        this.loading.set(false);
        this.router.navigate(['/reset-password'], { queryParams: { email } });
      }
    });
  }
}
