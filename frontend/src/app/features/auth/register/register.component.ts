import { Component, signal, computed, inject, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { LucideAngularModule } from 'lucide-angular';
import { AuthService } from '../../../core/services/auth.service';
import { ToastService } from '../../../core/services/toast.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, LucideAngularModule, RouterLink],
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.css'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class RegisterComponent {
  private static readonly PASSWORD_RULE =
    /^(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&]).{8,}$/;

  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private router = inject(Router);
  private toastService = inject(ToastService);

  // ✅ All reactive state as signals
  isLoading = signal<boolean>(false);
  errorMessage = signal<string>('');
  showPassword = signal<boolean>(false);
  
  // ✅ Computed signal for form validation state
  isFormValid = computed(() => this.registerForm.valid);

  registerForm = this.fb.nonNullable.group({
    name: ['', [Validators.required, Validators.minLength(2)]],
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.pattern(RegisterComponent.PASSWORD_RULE)]],
    role: ['JOB_SEEKER' as 'JOB_SEEKER' | 'RECRUITER', Validators.required]
  });

  roles = [
    { value: 'JOB_SEEKER' as const, label: 'Job Seeker', icon: 'user' },
    { value: 'RECRUITER' as const, label: 'Recruiter', icon: 'briefcase' }
  ];

  togglePasswordVisibility() {
    this.showPassword.update(value => !value);
  }

  onSubmit() {
    if (this.registerForm.invalid) {
      this.errorMessage.set('Please fill in all required fields correctly');
      return;
    }

    // ✅ Update signal - no NG0100 error
    this.isLoading.set(true);
    this.errorMessage.set('');

    const formValue = this.registerForm.getRawValue();

    this.authService.register(formValue).subscribe({
      next: () => {
        this.isLoading.set(false);
        this.toastService.success('Registration successful! Please login.');
        this.router.navigate(['/login']);
      },
      error: (error: Error) => {
        // ✅ Update signals in error handler
        this.isLoading.set(false);
        this.errorMessage.set(error.message || 'Registration failed. Please try again.');
      }
    });
  }

  // Helper for template
  getFieldError(fieldName: string): string {
    const field = this.registerForm.get(fieldName);
    if (!field?.touched || !field?.errors) return '';

    if (field.errors['required']) return `${fieldName} is required`;
    if (field.errors['email']) return 'Invalid email format';
    if (field.errors['pattern'] && fieldName === 'password') {
      return 'Use 8+ chars, 1 uppercase, 1 number, and 1 special character';
    }
    if (field.errors['minlength']) {
      const minLength = field.errors['minlength'].requiredLength;
      return `Minimum ${minLength} characters required`;
    }
    return '';
  }
}
