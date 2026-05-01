import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { ApiService } from '../../../core/services/api.service';
import { AuthService } from '../../../core/services/auth.service';
import { ToastService } from '../../../core/services/toast.service';
import { JobRequest } from '../../../shared/models/models';
import { LucideAngularModule } from 'lucide-angular';

@Component({
  selector: 'app-job-create',
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
            <h1 class="text-2xl font-bold text-gray-900 tracking-tight">Post a New Job</h1>
            <p class="text-sm text-gray-500 mt-1">Fill out the information below to create a new job listing.</p>
          </div>

          <form (ngSubmit)="onSubmit()" #f="ngForm" class="space-y-6">
            <div>
              <label class="block text-gray-700 text-sm font-medium mb-1.5">Job Title *</label>
              <input type="text" name="title" [(ngModel)]="job.title" required
                class="form-input transition-shadow"
                placeholder="e.g. Senior Backend Developer" />
            </div>
            
            <div class="grid grid-cols-1 sm:grid-cols-2 gap-6">
              <div>
                <label class="block text-gray-700 text-sm font-medium mb-1.5">Company *</label>
                @if (myCompanies.length === 0) {
                  <div class="mb-2 rounded-lg border border-amber-200 bg-amber-50 p-3 text-sm text-amber-800">
                    You haven't registered a company yet. Create one to start posting jobs.
                    <a routerLink="/recruiter/company" class="font-semibold underline ml-1">Register company</a>
                  </div>
                }
                <select name="companyId" [(ngModel)]="job.companyId" required
                  class="form-input transition-shadow bg-white text-gray-700">
                  <option value="" disabled selected>Select a company</option>
                  @for (c of myCompanies; track c.id) {
                    <option [value]="c.id">{{ c.name }}</option>
                  }
                </select>
              </div>
              <div>
                <label class="block text-gray-700 text-sm font-medium mb-1.5">Location *</label>
                <input type="text" name="location" [(ngModel)]="job.location" required
                  class="form-input transition-shadow"
                  placeholder="e.g. New York, NY or Remote" />
              </div>
            </div>
            
            <div>
              <label class="block text-gray-700 text-sm font-medium mb-1.5">Salary Range</label>
              <div class="relative">
                <lucide-icon name="banknote" class="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400"></lucide-icon>
                <input type="text" name="salary" [(ngModel)]="job.salary"
                  class="form-input pl-10 transition-shadow"
                  placeholder="e.g. $80,000 - $100,000" />
              </div>
            </div>
            
            <div>
              <label class="block text-gray-700 text-sm font-medium mb-1.5">Job Description *</label>
              <textarea name="description" [(ngModel)]="job.description" required rows="6"
                class="form-input transition-shadow py-3"
                placeholder="Job responsibilities, requirements, benefits..."></textarea>
            </div>

            <div>
              <label class="block text-gray-700 text-sm font-medium mb-1.5">Category</label>
              <select name="category" [(ngModel)]="job.category" class="form-input bg-white text-gray-700">
                <option value="">Auto-detect from title & description</option>
                <option value="Engineering">💻 Engineering</option>
                <option value="Design">🎨 Design</option>
                <option value="Marketing">📈 Marketing</option>
                <option value="Business">💼 Business</option>
                <option value="Finance">🏦 Finance</option>
                <option value="Support">📞 Support</option>
                <option value="Healthcare">🏥 Healthcare</option>
                <option value="Content">📝 Content</option>
                <option value="Intern">🎓 Intern</option>
              </select>
              <p class="text-xs text-gray-400 mt-1">Leave blank to auto-detect from your job title and description.</p>
            </div>
            
            <div class="pt-2 flex justify-end gap-3">
              <a routerLink="/recruiter/dashboard" class="px-6 py-2.5 rounded-lg text-sm font-medium text-gray-700 bg-white border border-gray-300 hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-gray-200 transition-colors">
                Cancel
              </a>
              <button type="submit" [disabled]="loading || !f.valid || myCompanies.length === 0"
                class="btn-primary py-2.5 px-8 disabled:opacity-70 disabled:cursor-not-allowed">
                @if (loading) {
                  <lucide-icon name="loader-2" class="w-4 h-4 animate-spin"></lucide-icon>
                } @else {
                  Post Job
                }
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  `
})
export class JobCreateComponent implements OnInit {
  private api = inject(ApiService);
  private toast = inject(ToastService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);
  private auth = inject(AuthService);

  job: JobRequest = { title: '', companyId: '', location: '', salary: '', description: '' };
  companies: any[] = [];
  myCompanies: any[] = [];
  loading = false;

  ngOnInit(): void {
    this.api.getCompanies().subscribe({
      next: (res: any) => {
        this.companies = res.data ?? res ?? [];
        const userId = this.auth.getCurrentUser()?.userId;
        this.myCompanies = userId ? this.companies.filter((c: any) => c.createdByUserId === userId) : [];

        this.route.queryParams.subscribe(params => {
          const cid = params['companyId'];
          if (cid) this.job.companyId = cid;
        });
      },
      error: () => this.toast.error('Failed to load companies')
    });
  }

  onSubmit(): void {
    this.loading = true;
    this.api.createJob(this.job).subscribe({
      next: () => { 
        this.toast.success('Job posted successfully!'); 
        this.router.navigate(['/recruiter/dashboard']); 
      },
      error: (err: any) => { 
        this.toast.error(err.error?.message || 'Failed to post job'); 
        this.loading = false; 
      }
    });
  }
}
