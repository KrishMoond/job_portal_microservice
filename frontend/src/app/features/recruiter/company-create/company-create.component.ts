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
              <label class="block text-gray-700 text-sm font-medium mb-1.5">Website *</label>
              <input type="text" name="website" [(ngModel)]="company.website" required class="form-input" placeholder="e.g. https://example.com" />
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

            <div class="border-t border-gray-200 pt-6 space-y-5">
              <div>
                <h2 class="text-lg font-semibold text-gray-900">Verification Details</h2>
                <p class="text-sm text-gray-500 mt-1">These details are sent to admin review after the company is created.</p>
              </div>

              <div>
                <label class="block text-gray-700 text-sm font-medium mb-1.5">Contact Name *</label>
                <input type="text" name="contactName" [(ngModel)]="verification.contactName" required class="form-input" placeholder="e.g. Priya Rao" />
              </div>

              <div>
                <label class="block text-gray-700 text-sm font-medium mb-1.5">Work Email *</label>
                <input type="email" name="workEmail" [(ngModel)]="verification.workEmail" required class="form-input" placeholder="name@company.com" />
              </div>

              <div>
                <label class="block text-gray-700 text-sm font-medium mb-1.5">Company Registration Number *</label>
                <input type="text" name="registrationNumber" [(ngModel)]="verification.registrationNumber" required class="form-input" placeholder="e.g. CIN, LLC, GST, or business registration ID" />
              </div>

              <div>
                <label class="block text-gray-700 text-sm font-medium mb-1.5">Phone</label>
                <input type="text" name="phone" [(ngModel)]="verification.phone" class="form-input" placeholder="+1 555 0100" />
              </div>

              <div>
                <label class="block text-gray-700 text-sm font-medium mb-1.5">Verification Document URL *</label>
                <input type="text" name="documentUrl" [(ngModel)]="verification.documentUrl" required class="form-input" placeholder="https://.../registration.pdf" />
              </div>

              <div>
                <label class="block text-gray-700 text-sm font-medium mb-1.5">Document Type *</label>
                <select name="documentType" [(ngModel)]="verification.documentType" required class="form-input">
                  <option value="application/pdf">PDF</option>
                  <option value="image/png">PNG image</option>
                  <option value="image/jpeg">JPEG image</option>
                  <option value="image/svg+xml">SVG image</option>
                </select>
              </div>
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
  verification: any = {
    contactName: this.auth.getCurrentUser()?.name || '',
    workEmail: this.auth.getCurrentUser()?.email || '',
    registrationNumber: '',
    phone: '',
    documentUrl: '',
    documentType: 'application/pdf'
  };

  onSubmit(): void {
    if (!this.auth.isRecruiter()) {
      this.toast.error('Only recruiters can register a company');
      return;
    }
    this.loading = true;
    this.api.createCompany(this.company).subscribe({
      next: (res: any) => {
        const created = res?.data ?? res;
        this.api.createRecruiterVerification({
          companyName: created?.name || this.company.name,
          workEmail: this.verification.workEmail,
          companyWebsite: created?.website || this.company.website,
          registrationNumber: this.verification.registrationNumber,
          contactName: this.verification.contactName,
          phone: this.verification.phone,
          documentUrl: this.verification.documentUrl,
          documentType: this.verification.documentType,
          recruiterId: this.auth.getUserId() ?? undefined
        }).subscribe({
          next: () => {
            this.toast.success('Company registered and sent for admin verification');
            this.router.navigate(['/recruiter/post-job'], { queryParams: { companyId: created?.id || null } });
          },
          error: (err: any) => {
            this.toast.error(err.error?.message || 'Company created, but verification submission failed');
            this.loading = false;
          }
        });
      },
      error: (err: any) => {
        this.toast.error(err.error?.message || 'Failed to create company');
        this.loading = false;
      }
    });
  }
}
