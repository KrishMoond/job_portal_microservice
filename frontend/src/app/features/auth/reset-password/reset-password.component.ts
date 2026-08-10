import { Component, signal, inject, OnInit, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink, ActivatedRoute } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { LucideAngularModule } from 'lucide-angular';
import { ToastService } from '../../../core/services/toast.service';
import { environment } from '../../../../environments/environment';

@Component({
  selector: 'app-reset-password',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink, LucideAngularModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="min-h-screen flex items-center justify-center bg-gradient-to-br from-indigo-50 via-white to-purple-50 px-4 py-12">
      <div class="w-full max-w-md">

        <div class="text-center mb-8">
          <div class="inline-flex items-center justify-center w-16 h-16 rounded-2xl bg-gradient-to-br from-indigo-600 to-purple-600 shadow-lg mb-4">
            <lucide-icon name="lock-keyhole" class="w-8 h-8 text-white"></lucide-icon>
          </div>
          <h1 class="text-3xl font-extrabold text-gray-900 tracking-tight">Reset password</h1>
          <p class="text-gray-600 mt-2">
            Enter the 6-digit code sent to<br/>
            <span class="font-semibold text-indigo-600">{{ email() }}</span>
          </p>
        </div>

        <div class="card">
          <form [formGroup]="form" (ngSubmit)="submit()">

            <!-- OTP code -->
            <div class="mb-5">
              <label class="form-label" for="otp">
                <lucide-icon name="key-round" [size]="16" class="inline mr-1"></lucide-icon>
                6-Digit Reset Code
              </label>
              <input id="otp" type="text" formControlName="otp"
                class="form-input tracking-[0.5em] text-center text-xl font-bold"
                [class.error]="form.get('otp')?.touched && form.get('otp')?.invalid"
                placeholder="000000" maxlength="6" inputmode="numeric" autocomplete="one-time-code" />
              @if (form.get('otp')?.touched && form.get('otp')?.invalid) {
                <div class="form-error">
                  <lucide-icon name="alert-circle" [size]="14"></lucide-icon>
                  Code must be exactly 6 digits
                </div>
              }
              <p class="text-xs text-gray-500 mt-1">Code expires in 10 minutes</p>
            </div>

            <!-- New password -->
            <div class="mb-5">
              <label class="form-label" for="newPassword">
                <lucide-icon name="lock" [size]="16" class="inline mr-1"></lucide-icon>
                New Password
              </label>
              <div class="relative">
                <input id="newPassword" [type]="showPw() ? 'text' : 'password'" formControlName="newPassword"
                  class="form-input pr-11"
                  [class.error]="form.get('newPassword')?.touched && form.get('newPassword')?.invalid"
                  placeholder="Min. 8 characters" />
                <button type="button" (click)="showPw.update(v => !v)"
                  class="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-indigo-600 transition-colors">
                  <lucide-icon [name]="showPw() ? 'eye-off' : 'eye'" [size]="16"></lucide-icon>
                </button>
              </div>
              @if (form.get('newPassword')?.touched && form.get('newPassword')?.invalid) {
                <div class="form-error">
                  <lucide-icon name="alert-circle" [size]="14"></lucide-icon>
                  Password must be at least 8 characters
                </div>
              }
            </div>

            <!-- Confirm password -->
            <div class="mb-6">
              <label class="form-label" for="confirmPassword">
                <lucide-icon name="lock" [size]="16" class="inline mr-1"></lucide-icon>
                Confirm Password
              </label>
              <input id="confirmPassword" [type]="showPw() ? 'text' : 'password'" formControlName="confirmPassword"
                class="form-input"
                [class.error]="form.get('confirmPassword')?.touched && passwordMismatch()"
                placeholder="Repeat new password" />
              @if (form.get('confirmPassword')?.touched && passwordMismatch()) {
                <div class="form-error">
                  <lucide-icon name="alert-circle" [size]="14"></lucide-icon>
                  Passwords do not match
                </div>
              }
            </div>

            @if (errorMessage()) {
              <div class="alert alert-error mb-5">
                <lucide-icon name="alert-circle" [size]="18"></lucide-icon>
                <span>{{ errorMessage() }}</span>
              </div>
            }

            <button type="submit" class="btn btn-primary w-full btn-lg"
              [disabled]="loading() || form.invalid || passwordMismatch()">
              @if (loading()) {
                <lucide-icon name="loader-2" [size]="20" class="animate-spin"></lucide-icon>
                <span>Resetting...</span>
              } @else {
                <lucide-icon name="shield-check" [size]="18"></lucide-icon>
                <span>Reset Password</span>
              }
            </button>

            <p class="text-center text-sm text-gray-500 mt-4">
              Didn't receive a code?
              <button type="button" (click)="resend()" [disabled]="resending()"
                class="font-semibold text-indigo-600 hover:text-indigo-700 transition-colors ml-1 disabled:opacity-50">
                @if (resending()) { Sending... } @else { Resend }
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
export class ResetPasswordComponent implements OnInit {
  private fb     = inject(FormBuilder);
  private http   = inject(HttpClient);
  private router = inject(Router);
  private route  = inject(ActivatedRoute);
  private toast  = inject(ToastService);

  loading      = signal(false);
  resending    = signal(false);
  errorMessage = signal('');
  showPw       = signal(false);
  email        = signal('');

  form = this.fb.nonNullable.group({
    otp:             ['', [Validators.required, Validators.pattern(/^\d{6}$/)]],
    newPassword:     ['', [Validators.required, Validators.minLength(8)]],
    confirmPassword: ['', Validators.required]
  });

  passwordMismatch = signal(false);

  ngOnInit() {
    const e = this.route.snapshot.queryParamMap.get('email') ?? '';
    if (!e) { this.router.navigate(['/forgot-password']); return; }
    this.email.set(e);

    this.form.valueChanges.subscribe(() => {
      const { newPassword, confirmPassword } = this.form.getRawValue();
      this.passwordMismatch.set(!!confirmPassword && newPassword !== confirmPassword);
    });
  }

  resend() {
    this.resending.set(true);
    this.http.post(`${environment.apiUrl}/api/users/forgot-password`, { email: this.email() }).subscribe({
      next: () => { this.resending.set(false); this.toast.success('A new code has been sent.'); },
      error: () => { this.resending.set(false); this.toast.success('A new code has been sent.'); }
    });
  }

  submit() {
    if (this.form.invalid || this.passwordMismatch()) return;
    this.loading.set(true);
    this.errorMessage.set('');
    const { otp, newPassword } = this.form.getRawValue();
    this.http.post(`${environment.apiUrl}/api/users/reset-password`, {
      email: this.email(), token: otp, newPassword
    }).subscribe({
      next: () => {
        this.loading.set(false);
        this.toast.success('Password reset successful. Please log in.');
        this.router.navigate(['/login']);
      },
      error: (err: any) => {
        this.loading.set(false);
        this.errorMessage.set(err.error?.message || 'Invalid or expired code.');
      }
    });
  }
}
