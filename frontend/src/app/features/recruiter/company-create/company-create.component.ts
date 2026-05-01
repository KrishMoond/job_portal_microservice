import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { LucideAngularModule } from 'lucide-angular';
import { ApiService } from '../../../core/services/api.service';
import { AuthService } from '../../../core/services/auth.service';
import { ToastService } from '../../../core/services/toast.service';

@Component({
  selector: 'app-company-create',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, LucideAngularModule],
  template: `
    <div class="min-h-screen bg-gray-50 py-10 px-4">
      <div class="max-w-2xl mx-auto">
        <a routerLink="/recruiter/dashboard" class="inline-flex items-center gap-1 text-sm font-medium text-gray-500 hover:text-primary transition-colors mb-6">
          <lucide-icon name="arrow-left" class="w-4 h-4"></lucide-icon> Back to Dashboard
        </a>

        <div class="bg-white rounded-2xl p-8 border border-gray-200 shadow-sm">
          <div class="mb-8">
            <h1 class="text-2xl font-bold text-gray-900 tracking-tight">Register Your Company</h1>
            <p class="text-sm text-gray-500 mt-1">Recruiters must register a company before posting jobs.</p>
          </div>

          <form (ngSubmit)="onSubmit()" #f="ngForm" class="space-y-6">
            <div>
              <label class="block text-gray-700 text-sm font-medium mb-1.5">Company Name *</label>
              <input type="text" name="name" [(ngModel)]="company.name" required class="form-input" placeholder="e.g. TechCorp Inc" />
            </div>

            <div>
              <label class="block text-gray-700 text-sm font-medium mb-1.5">Website</label>
              <input type="text" name="website" [(ngModel)]="company.website" class="form-input" placeholder="e.g. https://example.com" />
            </div>

            <div>
              <label class="block text-gray-700 text-sm font-medium mb-1.5">Logo URL</label>
              <input type="text" name="logoUrl" [(ngModel)]="company.logoUrl" class="form-input" placeholder="https://..." />
            </div>

            <div>
              <label class="block text-gray-700 text-sm font-medium mb-1.5">Description</label>
              <textarea name="description" [(ngModel)]="company.description" rows="5" class="form-input py-3"
                placeholder="What does your company do?"></textarea>
            </div>

            <div class="pt-2 flex justify-end gap-3">
              <a routerLink="/recruiter/post-job" class="px-6 py-2.5 rounded-lg text-sm font-medium text-gray-700 bg-white border border-gray-300 hover:bg-gray-50">
                Skip for now
              </a>
              <button type="submit" [disabled]="loading || !f.valid" class="btn-primary py-2.5 px-8 disabled:opacity-70 disabled:cursor-not-allowed">
                @if (loading) { <lucide-icon name="loader-2" class="w-4 h-4 animate-spin"></lucide-icon> } @else { Create Company }
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  `
})
export class CompanyCreateComponent {
  private api = inject(ApiService);
  private auth = inject(AuthService);
  private toast = inject(ToastService);
  private router = inject(Router);

  loading = false;
  company: any = { name: '', description: '', website: '', logoUrl: '' };

  onSubmit(): void {
    if (!this.auth.isRecruiter()) {
      this.toast.error('Only recruiters can register a company');
      return;
    }
    this.loading = true;
    this.api.createCompany(this.company).subscribe({
      next: (res: any) => {
        const created = res?.data ?? res;
        this.toast.success('Company registered successfully');
        this.router.navigate(['/recruiter/post-job'], { queryParams: { companyId: created?.id || null } });
      },
      error: (err: any) => {
        this.toast.error(err.error?.message || 'Failed to create company');
        this.loading = false;
      }
    });
  }
}

