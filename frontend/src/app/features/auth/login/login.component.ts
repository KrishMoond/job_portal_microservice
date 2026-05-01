import {
  Component, inject, signal, computed, ChangeDetectionStrategy,
  ElementRef, ViewChild, AfterViewInit, OnDestroy, NgZone
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { ToastService } from '../../../core/services/toast.service';
import { LucideAngularModule } from 'lucide-angular';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, LucideAngularModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  styles: [`
    :host { display: block; }

    /* ── Light Background ────────────────────────────────────────────────── */
    .login-bg {
      background: linear-gradient(135deg, #f0f4ff 0%, #ffffff 50%, #faf5ff 100%);
      position: relative;
      overflow: hidden;
    }

    /* ── Subtle decorative blobs ─────────────────────────────────────────── */
    .blob {
      position: absolute;
      border-radius: 50%;
      filter: blur(80px);
      will-change: transform;
      pointer-events: none;
      opacity: 0.15;
    }
    .blob-1 {
      width: 560px; height: 560px;
      background: radial-gradient(circle at 40% 40%, #7c3aed 0%, #6366f1 50%, transparent 70%);
      top: -140px; left: -140px;
      animation: floatA 9s ease-in-out infinite;
    }
    .blob-2 {
      width: 500px; height: 500px;
      background: radial-gradient(circle at 60% 60%, #0ea5e9 0%, #6366f1 50%, transparent 70%);
      bottom: -120px; right: -120px;
      animation: floatB 11s ease-in-out infinite;
    }

    @keyframes floatA {
      0%,100% { transform: translate(0,0) scale(1); }
      33%      { transform: translate(60px,40px) scale(1.08); }
      66%      { transform: translate(-30px,70px) scale(0.95); }
    }
    @keyframes floatB {
      0%,100% { transform: translate(0,0) scale(1); }
      40%     { transform: translate(-70px,-50px) scale(1.1); }
      70%     { transform: translate(40px,-30px) scale(0.92); }
    }

    /* ── White card with shadow ──────────────────────────────────────────── */
    .login-card {
      background: #ffffff;
      border: 1px solid #e5e7eb;
      box-shadow: 0 10px 40px -10px rgba(0, 0, 0, 0.1);
      transition: transform 0.15s ease, box-shadow 0.15s ease;
      will-change: transform;
    }

    /* ── Form inputs ─────────────────────────────────────────────────────── */
    .field-wrap { position: relative; }

    .float-input {
      background: #ffffff;
      border: 1.5px solid #d1d5db;
      color: #111827;
      width: 100%;
      padding: 22px 44px 8px 44px;
      border-radius: 14px;
      font-size: 0.9rem;
      outline: none;
      transition: border-color 0.22s, box-shadow 0.22s, background 0.22s;
    }
    .float-input::placeholder { color: transparent; }
    .float-input:focus {
      background: #ffffff;
      border-color: #6c63ff;
      box-shadow: 0 0 0 3.5px rgba(108, 99, 255, 0.15);
    }
    .float-input.error-field {
      border-color: #ef4444;
      box-shadow: 0 0 0 3.5px rgba(239, 68, 68, 0.1);
    }

    /* Floating label */
    .float-label {
      position: absolute;
      left: 44px;
      top: 50%;
      transform: translateY(-50%);
      font-size: 0.875rem;
      color: #9ca3af;
      pointer-events: none;
      transition: top 0.2s ease, font-size 0.2s ease, color 0.2s ease, transform 0.2s ease;
      transform-origin: left center;
    }
    .float-input:focus ~ .float-label,
    .float-input.has-value ~ .float-label {
      top: 10px;
      transform: translateY(0) scale(0.78);
      color: #6c63ff;
      font-size: 0.875rem;
    }
    .float-input.error-field ~ .float-label {
      color: #ef4444;
    }

    /* Leading icon */
    .field-icon {
      position: absolute;
      left: 14px;
      top: 50%;
      transform: translateY(-50%);
      color: #9ca3af;
      pointer-events: none;
      transition: color 0.2s;
    }
    .float-input:focus ~ .float-label ~ .field-icon,
    .float-input:focus + .float-label + .field-icon {
      color: #6c63ff;
    }

    /* Trailing icon (eye toggle) */
    .eye-btn {
      position: absolute;
      right: 14px;
      top: 50%;
      transform: translateY(-50%);
      color: #9ca3af;
      background: none;
      border: none;
      cursor: pointer;
      padding: 4px;
      border-radius: 6px;
      transition: color 0.2s, background 0.2s;
      display: flex;
      align-items: center;
    }
    .eye-btn:hover { color: #6c63ff; background: rgba(108, 99, 255, 0.1); }

    /* ── Sign in button ──────────────────────────────────────────────────── */
    .signin-btn {
      background: linear-gradient(135deg, #6c63ff 0%, #8b5cf6 100%);
      box-shadow: 0 4px 24px -4px rgba(108, 99, 255, 0.4);
      transition: transform 0.18s ease, box-shadow 0.18s ease;
      position: relative;
      overflow: hidden;
    }
    .signin-btn::after {
      content: '';
      position: absolute;
      inset: 0;
      background: linear-gradient(135deg, rgba(255,255,255,0.12) 0%, transparent 60%);
      opacity: 0;
      transition: opacity 0.2s;
    }
    .signin-btn:hover:not(:disabled)::after { opacity: 1; }
    .signin-btn:hover:not(:disabled) {
      transform: translateY(-2px);
      box-shadow: 0 10px 36px -4px rgba(108, 99, 255, 0.5);
    }
    .signin-btn:active:not(:disabled) {
      transform: translateY(0) scale(0.98);
    }
    .signin-btn:disabled { opacity: 0.5; cursor: not-allowed; }

    /* ── Google button ───────────────────────────────────────────────────── */
    .google-btn {
      background: #ffffff;
      border: 1.5px solid #e5e7eb;
      transition: background 0.2s, border-color 0.2s, transform 0.15s;
    }
    .google-btn:hover {
      background: #f9fafb;
      border-color: #d1d5db;
      transform: translateY(-1px);
    }

    /* ── Shake animation ─────────────────────────────────────────────────── */
    @keyframes shake {
      0%,100% { transform: translateX(0); }
      15%     { transform: translateX(-8px); }
      30%     { transform: translateX(8px); }
      45%     { transform: translateX(-6px); }
      60%     { transform: translateX(6px); }
      75%     { transform: translateX(-3px); }
      90%     { transform: translateX(3px); }
    }
    .shake { animation: shake 0.5s cubic-bezier(0.36,0.07,0.19,0.97) both; }

    /* ── Logo ring ───────────────────────────────────────────────────────── */
    @keyframes spinRing {
      from { transform: rotate(0deg); }
      to   { transform: rotate(360deg); }
    }
    .logo-ring { animation: spinRing 8s linear infinite; }

    /* ── Card entrance ───────────────────────────────────────────────────── */
    @keyframes cardIn {
      from { opacity: 0; transform: translateY(24px) scale(0.97); }
      to   { opacity: 1; transform: translateY(0) scale(1); }
    }
    .card-in { animation: cardIn 0.5s cubic-bezier(0.34,1.2,0.64,1) both; }

    /* ── Checkbox custom ─────────────────────────────────────────────────── */
    .custom-check {
      appearance: none;
      width: 16px; height: 16px;
      border: 1.5px solid #d1d5db;
      border-radius: 5px;
      background: #ffffff;
      cursor: pointer;
      position: relative;
      transition: border-color 0.2s, background 0.2s;
      flex-shrink: 0;
    }
    .custom-check:checked {
      background: #6c63ff;
      border-color: #6c63ff;
    }
    .custom-check:checked::after {
      content: '';
      position: absolute;
      left: 4px; top: 1px;
      width: 5px; height: 9px;
      border: 2px solid #fff;
      border-top: none; border-left: none;
      transform: rotate(45deg);
    }
  `],
  template: `
    <div #container class="login-bg min-h-screen flex items-center justify-center px-4 py-12">

      <!-- Subtle decorative blobs -->
      <div class="blob blob-1"></div>
      <div class="blob blob-2"></div>

      <!-- Card -->
      <div #card class="login-card card-in relative z-10 w-full max-w-sm rounded-3xl px-8 py-10"
        [class.shake]="shaking()">

        <!-- Logo -->
        <div class="flex flex-col items-center mb-8">
          <div class="relative w-14 h-14 mb-4">
            <div class="absolute inset-0 rounded-2xl bg-gradient-to-br from-indigo-600 to-purple-600 flex items-center justify-center shadow-lg">
              <lucide-icon name="briefcase" class="w-6 h-6 text-white"></lucide-icon>
            </div>
          </div>
          <h1 class="text-2xl font-black text-gray-900 tracking-tight">Welcome back</h1>
          <p class="text-gray-600 text-sm mt-1">Sign in to continue to HireHub</p>
        </div>

        <form (ngSubmit)="onSubmit()" #f="ngForm" class="space-y-4">

          <!-- Email field with floating label -->
          <div class="field-wrap">
            <input
              type="email" name="email"
              [ngModel]="email()" (ngModelChange)="email.set($event)"
              (focus)="emailFocused.set(true)" (blur)="emailFocused.set(false)"
              required
              placeholder="Email address"
              class="float-input"
              [class.has-value]="email().length > 0"
              [class.error-field]="emailTouched() && !isEmailValid()" />
            <span class="float-label">Email address</span>
            <lucide-icon name="mail" class="field-icon w-4 h-4"></lucide-icon>
            <!-- Valid tick -->
            @if (email().length > 0 && isEmailValid()) {
              <lucide-icon name="check" class="absolute right-3.5 top-1/2 -translate-y-1/2 w-4 h-4 text-green-400 pointer-events-none"></lucide-icon>
            }
          </div>

          <!-- Password field with floating label + strength -->
          <div>
            <div class="field-wrap">
              <input
                [type]="showPassword() ? 'text' : 'password'"
                name="password"
                [ngModel]="password()" (ngModelChange)="password.set($event)"
                (focus)="pwFocused.set(true)" (blur)="pwFocused.set(false)"
                required
                placeholder="Password"
                class="float-input pr-11"
                [class.has-value]="password().length > 0"
                [class.error-field]="errorState()" />
              <span class="float-label">Password</span>
              <lucide-icon name="lock" class="field-icon w-4 h-4"></lucide-icon>
              <button type="button" class="eye-btn" (click)="showPassword.update(v => !v)">
                @if (showPassword()) {
                  <lucide-icon name="eye-off" class="w-4 h-4"></lucide-icon>
                } @else {
                  <lucide-icon name="eye" class="w-4 h-4"></lucide-icon>
                }
              </button>
            </div>

            <!-- Password strength bar (shows while typing) -->
            @if (password().length > 0 && pwFocused()) {
              <div class="mt-2 space-y-1">
                <div class="flex gap-1">
                  @for (seg of [1,2,3,4]; track seg) {
                    <div class="flex-1 h-1 rounded-full transition-all duration-300"
                      [style.background]="seg <= pwStrength() ? pwStrengthColor() : '#e5e7eb'"></div>
                  }
                </div>
                <p class="text-[11px] transition-colors duration-200 font-medium"
                  [style.color]="pwStrengthColor()">
                  {{ pwStrengthLabel() }}
                </p>
              </div>
            }
          </div>

          <!-- Remember me -->
          <div class="flex items-center pt-1">
            <label class="flex items-center gap-2 cursor-pointer group select-none">
              <input type="checkbox" [(ngModel)]="rememberMe" name="remember" class="custom-check" />
              <span class="text-xs text-gray-600 group-hover:text-gray-900 transition-colors">Remember me</span>
            </label>
          </div>

          <!-- Error message -->
          @if (errorState()) {
            <div class="flex items-center gap-2.5 text-red-700 text-xs
                        bg-red-50 border border-red-200 rounded-xl px-4 py-3 animate-fade-in">
              <lucide-icon name="alert-circle" class="w-4 h-4 flex-shrink-0"></lucide-icon>
              <span>{{ errorMessage() }}</span>
            </div>
          }

          <!-- Submit button -->
          <button #submitBtn type="submit" [disabled]="loading()"
            class="signin-btn w-full py-3.5 rounded-xl text-sm font-bold text-white
                   flex items-center justify-center gap-2 mt-1">
            @if (loading()) {
              <lucide-icon name="loader-2" class="w-4 h-4 animate-spin"></lucide-icon>
              <span>Signing in...</span>
            } @else {
              <span>Sign in</span>
              <lucide-icon name="arrow-right" class="w-4 h-4"></lucide-icon>
            }
          </button>
        </form>

        <!-- Register link -->
        <p class="text-center text-xs text-gray-600 mt-6">
          Don't have an account?
          <a routerLink="/register"
            class="text-indigo-600 hover:text-indigo-700 font-semibold transition-colors ml-1">
            Create one free
          </a>
        </p>

      </div>
    </div>
  `
})
export class LoginComponent implements AfterViewInit, OnDestroy {
  private auth   = inject(AuthService);
  private toast  = inject(ToastService);
  private router = inject(Router);
  private zone   = inject(NgZone);

  @ViewChild('container') containerRef!: ElementRef<HTMLElement>;
  @ViewChild('card')      cardRef!: ElementRef<HTMLElement>;

  // ── Form state ────────────────────────────────────────────────────────────
  email        = signal('');
  password     = signal('');
  loading      = signal(false);
  errorState   = signal(false);
  errorMessage = signal('');
  showPassword = signal(false);
  shaking      = signal(false);
  rememberMe   = false;

  // ── Focus tracking ────────────────────────────────────────────────────────
  emailFocused = signal(false);
  pwFocused    = signal(false);
  emailTouched = signal(false);

  // ── Validation ────────────────────────────────────────────────────────────
  isEmailValid = computed(() => /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(this.email()));

  // ── Password strength ─────────────────────────────────────────────────────
  pwStrength = computed(() => {
    const p = this.password();
    if (!p) return 0;
    let score = 0;
    if (p.length >= 8)              score++;
    if (/[A-Z]/.test(p))            score++;
    if (/[0-9]/.test(p))            score++;
    if (/[^A-Za-z0-9]/.test(p))     score++;
    return score;
  });
  pwStrengthColor = computed(() => {
    const s = this.pwStrength();
    if (s <= 1) return '#ef4444';
    if (s === 2) return '#f59e0b';
    if (s === 3) return '#3b82f6';
    return '#22c55e';
  });
  pwStrengthLabel = computed(() => {
    const s = this.pwStrength();
    if (s <= 1) return 'Weak';
    if (s === 2) return 'Fair';
    if (s === 3) return 'Good';
    return 'Strong';
  });

  // ── Blob cursor tracking ──────────────────────────────────────────────────
  blobX = signal(-190);
  blobY = signal(-190);
  private targetX  = 0;
  private targetY  = 0;
  private currentX = -190;
  private currentY = -190;
  private rafId    = 0;
  private running  = false;

  // ── Card 3-D tilt ─────────────────────────────────────────────────────────
  private tiltRafId = 0;

  ngAfterViewInit(): void {
    this.zone.runOutsideAngular(() => {
      this.running = true;
      this.blobLoop();
    });
  }

  ngOnDestroy(): void {
    this.running = false;
    cancelAnimationFrame(this.rafId);
    cancelAnimationFrame(this.tiltRafId);
  }

  // ── Mouse move: blob + card tilt ─────────────────────────────────────────
  onMouseMove(e: MouseEvent): void {
    const rect = (e.currentTarget as HTMLElement).getBoundingClientRect();
    this.targetX = e.clientX - rect.left - 190;
    this.targetY = e.clientY - rect.top  - 190;

    // Card tilt (runs outside zone)
    if (this.cardRef) {
      cancelAnimationFrame(this.tiltRafId);
      this.tiltRafId = requestAnimationFrame(() => {
        const card = this.cardRef.nativeElement;
        const cr   = card.getBoundingClientRect();
        const cx   = cr.left + cr.width  / 2;
        const cy   = cr.top  + cr.height / 2;
        const dx   = (e.clientX - cx) / (cr.width  / 2);
        const dy   = (e.clientY - cy) / (cr.height / 2);
        // Max 6° tilt
        const rx   =  dy * 6;
        const ry   = -dx * 6;
        card.style.transform = `perspective(900px) rotateX(${rx}deg) rotateY(${ry}deg)`;
      });
    }
  }

  onMouseLeave(): void {
    if (this.cardRef) {
      this.cardRef.nativeElement.style.transform = 'perspective(900px) rotateX(0deg) rotateY(0deg)';
    }
  }

  // ── Blob lerp loop ────────────────────────────────────────────────────────
  private blobLoop(): void {
    if (!this.running) return;
    const f = 0.07;
    this.currentX += (this.targetX - this.currentX) * f;
    this.currentY += (this.targetY - this.currentY) * f;

    if (
      Math.abs(this.currentX - this.blobX()) > 0.3 ||
      Math.abs(this.currentY - this.blobY()) > 0.3
    ) {
      this.zone.run(() => {
        this.blobX.set(Math.round(this.currentX * 10) / 10);
        this.blobY.set(Math.round(this.currentY * 10) / 10);
      });
    }
    this.rafId = requestAnimationFrame(() => this.blobLoop());
  }

  // ── Ripple on button click ────────────────────────────────────────────────
  onBtnClick(e: MouseEvent): void {
    const btn  = e.currentTarget as HTMLElement;
    const rect = btn.getBoundingClientRect();
    const size = Math.max(rect.width, rect.height);
    const x    = e.clientX - rect.left - size / 2;
    const y    = e.clientY - rect.top  - size / 2;
    const rip  = document.createElement('span');
    rip.className = 'ripple';
    rip.style.cssText = `width:${size}px;height:${size}px;left:${x}px;top:${y}px`;
    btn.appendChild(rip);
    setTimeout(() => rip.remove(), 600);
  }

  // ── Submit ────────────────────────────────────────────────────────────────
  onSubmit(): void {
    this.emailTouched.set(true);
    if (!this.email() || !this.password()) return;
    this.loading.set(true);
    this.errorState.set(false);
    this.errorMessage.set('');

    this.auth.login({ email: this.email(), password: this.password() }).subscribe({
      next: (res: any) => {
        const role = res.body?.data?.role;
        this.router.navigate([role === 'RECRUITER' ? '/recruiter/dashboard' : '/jobs']);
      },
      error: (err: any) => {
        this.loading.set(false);
        this.errorState.set(true);
        this.errorMessage.set(err.error?.message || 'Invalid email or password');
        this.toast.error(this.errorMessage());
        // Shake the card
        this.shaking.set(true);
        setTimeout(() => this.shaking.set(false), 520);
      }
    });
  }
}
