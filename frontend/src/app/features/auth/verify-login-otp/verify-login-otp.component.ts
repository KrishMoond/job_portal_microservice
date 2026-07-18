import { Component, signal, inject, OnInit, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink, ActivatedRoute } from '@angular/router';
import { LucideAngularModule } from 'lucide-angular';
import { AuthService } from '../../../core/services/auth.service';
import { ToastService } from '../../../core/services/toast.service';

@Component({
  selector: 'app-verify-login-otp',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, LucideAngularModule, RouterLink],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="min-h-screen flex items-center justify-center bg-gradient-to-br from-indigo-50 via-white to-purple-50 px-4 py-12">
      <div class="w-full max-w-md">

        <div class="text-center mb-8">
          <div class="inline-flex items-center justify-center w-16 h-16 rounded-2xl bg-gradient-to-br from-indigo-600 to-purple-600 shadow-lg mb-4">
            <lucide-icon name="shield-check" class="w-8 h-8 text-white"></lucide-icon>
          </div>
          <h1 class="text-3xl font-extrabold text-gray-900 tracking-tight">Two-Factor Login</h1>
          <p class="text-gray-600 mt-2">
            We sent a 6-digit OTP to<br/>
            <span class="font-semibold text-indigo-600">{{ email() }}</span>
          </p>
        </div>

        <div class="card">
          <form [formGroup]="otpForm" (ngSubmit)="onSubmit()">

            <div class="mb-6">
              <label class="form-label" for="otp">
                <lucide-icon name="key-round" [size]="16" class="inline mr-1"></lucide-icon>
                6-Digit OTP
              </label>
              <input
                id="otp"
                type="text"
                formControlName="otp"
                class="form-input tracking-[0.5em] text-center text-xl font-bold"
                [class.error]="otpForm.get('otp')?.touched && otpForm.get('otp')?.invalid"
                placeholder="000000"
                maxlength="6"
                inputmode="numeric"
                autocomplete="one-time-code"
              />
              @if (otpForm.get('otp')?.touched && otpForm.get('otp')?.invalid) {
                <div class="form-error">
                  <lucide-icon name="alert-circle" [size]="14"></lucide-icon>
                  OTP must be exactly 6 digits
                </div>
              }
              <p class="text-xs text-gray-500 mt-1">OTP expires in 10 minutes</p>
            </div>

            @if (errorMessage()) {
              <div class="alert alert-error mb-5">
                <lucide-icon name="alert-circle" [size]="18"></lucide-icon>
                <span>{{ errorMessage() }}</span>
              </div>
            }

            <button
              type="submit"
              class="btn btn-primary w-full btn-lg"
              [disabled]="isLoading() || otpForm.invalid"
            >
              @if (isLoading()) {
                <lucide-icon name="loader-2" [size]="20" class="animate-spin"></lucide-icon>
                <span>Verifying...</span>
              } @else {
                <lucide-icon name="log-in" [size]="18"></lucide-icon>
                <span>Verify & Sign In</span>
              }
            </button>

            <p class="text-center text-sm text-gray-500 mt-4">
              Didn't receive it?
              <button type="button" (click)="resendOtp()" [disabled]="isResending()"
                class="font-semibold text-indigo-600 hover:text-indigo-700 transition-colors ml-1 disabled:opacity-50">
                @if (isResending()) { Sending... } @else { Resend OTP }
              </button>
            </p>

            <p class="text-center text-sm text-gray-600 mt-3">
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
export class VerifyLoginOtpComponent implements OnInit {
  private fb           = inject(FormBuilder);
  private authService  = inject(AuthService);
  private router       = inject(Router);
  private route        = inject(ActivatedRoute);
  private toastService = inject(ToastService);

  isLoading    = signal(false);
  isResending  = signal(false);
  errorMessage = signal('');
  email        = signal('');

  otpForm = this.fb.nonNullable.group({
    otp: ['', [Validators.required, Validators.pattern(/^\d{6}$/)]]
  });

  ngOnInit(): void {
    const emailParam = this.route.snapshot.queryParamMap.get('email') ?? '';
    if (!emailParam) { this.router.navigate(['/login']); return; }
    this.email.set(emailParam);
  }

  onSubmit(): void {
    if (this.otpForm.invalid) return;
    this.isLoading.set(true);
    this.errorMessage.set('');

    this.authService.verifyLoginOtp({ email: this.email(), otp: this.otpForm.getRawValue().otp }).subscribe({
      next: (res: any) => {
        this.isLoading.set(false);
        const role = res.body?.data?.role;
        this.router.navigate([role === 'RECRUITER' ? '/recruiter/dashboard' : '/jobs']);
      },
      error: (err: any) => {
        this.isLoading.set(false);
        this.errorMessage.set(err.error?.message || err.message || 'Verification failed.');
      }
    });
  }

  resendOtp(): void {
    this.isResending.set(true);
    this.authService.resendOtp(this.email()).subscribe({
      next: () => {
        this.isResending.set(false);
        this.toastService.success('A new OTP has been sent to your email.');
      },
      error: (err: any) => {
        this.isResending.set(false);
        this.errorMessage.set(err.error?.message || 'Failed to resend OTP.');
      }
    });
  }
}
