import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { LucideAngularModule } from 'lucide-angular';
import { ApiService } from '../../../core/services/api.service';
import { ToastService } from '../../../core/services/toast.service';

@Component({
  selector: 'app-company-edit',
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
            <h1 class="text-2xl font-bold text-gray-900 tracking-tight">Edit Company</h1>
            <p class="text-sm text-gray-500 mt-1">Update your company profile.</p>
          </div>

          @if (loadingCompany) {
            <div class="space-y-4 animate-pulse">
              <div class="h-12 bg-gray-100 rounded-lg"></div>
              <div class="h-12 bg-gray-100 rounded-lg"></div>
              <div class="h-32 bg-gray-100 rounded-lg"></div>
            </div>
          } @else {
            <form (ngSubmit)="onSubmit()" #f="ngForm" class="space-y-6">
              <div>
                <label class="block text-gray-700 text-sm font-medium mb-1.5">Company Name</label>
                <input type="text" [value]="company.name" disabled
                  class="form-input bg-gray-50 text-gray-400 cursor-not-allowed" />
                <p class="text-xs text-gray-400 mt-1">Company name cannot be changed.</p>
              </div>
              <div>
                <label class="block text-gray-700 text-sm font-medium mb-1.5">Website</label>
                <input type="text" name="website" [(ngModel)]="company.website" class="form-input" placeholder="https://example.com" />
              </div>
              <div>
                <label class="block text-gray-700 text-sm font-medium mb-1.5">Logo URL</label>
                <input type="text" name="logoUrl" [(ngModel)]="company.logoUrl" class="form-input" placeholder="https://..." />
                @if (company.logoUrl) {
                  <div class="mt-2 flex items-center gap-3">
                    <img [src]="company.logoUrl" alt="Logo preview" class="w-12 h-12 rounded-lg object-cover border border-gray-200" (error)="company.logoUrl = ''" />
                    <span class="text-xs text-gray-400">Preview</span>
                  </div>
                }
              </div>
              <div>
                <label class="block text-gray-700 text-sm font-medium mb-1.5">Description</label>
                <textarea name="description" [(ngModel)]="company.description" rows="5" class="form-input py-3"
                  placeholder="What does your company do?"></textarea>
              </div>
              <div class="pt-2 flex justify-end gap-3">
                <a routerLink="/recruiter/dashboard" class="px-6 py-2.5 rounded-lg text-sm font-medium text-gray-700 bg-white border border-gray-300 hover:bg-gray-50">
                  Cancel
                </a>
                <button type="submit" [disabled]="loading" class="btn-primary py-2.5 px-8 disabled:opacity-70 disabled:cursor-not-allowed">
                  @if (loading) { <lucide-icon name="loader-2" class="w-4 h-4 animate-spin"></lucide-icon> } @else { Save Changes }
                </button>
              </div>
            </form>
          }
        </div>
      </div>
    </div>
  `
})
export class CompanyEditComponent implements OnInit {
  private api = inject(ApiService);
  private toast = inject(ToastService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);

  company: any = { name: '', description: '', website: '', logoUrl: '' };
  loading = false;
  loadingCompany = true;
  private companyId = '';

  ngOnInit(): void {
    this.companyId = this.route.snapshot.paramMap.get('id') || '';
    if (!this.companyId) { this.router.navigate(['/recruiter/dashboard']); return; }

    this.api.getCompanyById(this.companyId).subscribe({
      next: (res: any) => {
        this.company = { ...(res?.data ?? res) };
        this.loadingCompany = false;
      },
      error: () => {
        this.toast.error('Failed to load company');
        this.router.navigate(['/recruiter/dashboard']);
      }
    });
  }

  onSubmit(): void {
    this.loading = true;
    this.api.updateCompany(this.companyId, {
      name: this.company.name,
      description: this.company.description,
      website: this.company.website,
      logoUrl: this.company.logoUrl
    }).subscribe({
      next: () => {
        this.toast.success('Company updated successfully');
        this.router.navigate(['/companies', this.companyId]);
      },
      error: (err: any) => {
        this.toast.error(err?.error?.message || 'Failed to update company');
        this.loading = false;
      }
    });
  }
}
