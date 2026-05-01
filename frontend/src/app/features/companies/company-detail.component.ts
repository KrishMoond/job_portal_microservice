import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { LucideAngularModule } from 'lucide-angular';
import { ApiService } from '../../core/services/api.service';
import { ToastService } from '../../core/services/toast.service';
import { AuthService } from '../../core/services/auth.service';
import { Job } from '../../shared/models/models';
import { forkJoin, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';

@Component({
  selector: 'app-company-detail',
  standalone: true,
  imports: [CommonModule, RouterLink, LucideAngularModule],
  template: `
    <div class="min-h-screen bg-gray-50">

      @if (loading()) {
        <div class="h-40 bg-gray-200 animate-pulse rounded-b-3xl"></div>
        <div class="max-w-5xl mx-auto px-6 pt-6 space-y-4 animate-pulse">
          <div class="h-8 bg-gray-200 rounded w-1/3"></div>
          <div class="h-4 bg-gray-100 rounded w-1/2"></div>
        </div>
      } @else if (!company()) {
        <div class="text-center py-32">
          <lucide-icon name="building-2" class="w-16 h-16 text-gray-300 mx-auto mb-4"></lucide-icon>
          <h3 class="text-lg font-semibold text-gray-900">Company not found</h3>
          <a routerLink="/companies" class="mt-4 inline-block text-primary hover:underline text-sm">Back to companies</a>
        </div>
      } @else {
        <!-- Banner -->
        <div class="h-40 bg-vibrant-gradient relative rounded-b-3xl overflow-hidden">
          <div class="absolute inset-0 bg-[url('https://www.transparenttextures.com/patterns/cubes.png')] opacity-20 mix-blend-overlay"></div>
        </div>

        <div class="max-w-5xl mx-auto px-6 pb-12">
          <!-- Company Header -->
          <div class="bg-white rounded-2xl border border-gray-200 shadow-sm p-8 -mt-10 relative z-10 mb-8">
            <div class="flex flex-col sm:flex-row items-start gap-6">
              <!-- Logo -->
              <div class="w-20 h-20 rounded-2xl border-4 border-white shadow-lg bg-gray-50 flex items-center justify-center overflow-hidden flex-shrink-0 -mt-14">
                @if (company()!.logoUrl) {
                  <img [src]="company()!.logoUrl" [alt]="company()!.name" class="w-full h-full object-cover" />
                } @else {
                  <span class="text-2xl font-black text-gray-400">{{ company()!.name.substring(0,2).toUpperCase() }}</span>
                }
              </div>
              <div class="flex-1">
                <div class="flex flex-wrap items-start justify-between gap-4">
                  <div>
                    <h1 class="text-2xl font-black text-gray-900 tracking-tight">{{ company()!.name }}</h1>
                    @if (company()!.website) {
                      <a [href]="company()!.website.startsWith('http') ? company()!.website : 'https://' + company()!.website"
                        target="_blank" rel="noopener noreferrer"
                        class="text-sm text-primary hover:underline flex items-center gap-1 mt-1">
                        <lucide-icon name="globe" class="w-3.5 h-3.5"></lucide-icon>
                        {{ company()!.website }}
                      </a>
                    }
                  </div>
                  <!-- Edit button for company owner -->
                  @if (isOwner()) {
                    <a [routerLink]="['/recruiter/company', company()!.id, 'edit']"
                      class="btn-secondary py-1.5 px-4 text-sm flex items-center gap-1.5">
                      <lucide-icon name="pencil" class="w-3.5 h-3.5"></lucide-icon> Edit
                    </a>
                  }
                </div>
                @if (company()!.description) {
                  <p class="text-gray-600 text-sm leading-relaxed mt-4 max-w-2xl">{{ company()!.description }}</p>
                }
              </div>
            </div>
          </div>

          <!-- Jobs at this company -->
          <div class="bg-white rounded-2xl border border-gray-200 shadow-sm overflow-hidden">
            <div class="px-6 py-4 border-b border-gray-100 flex items-center justify-between">
              <h2 class="font-semibold text-gray-900">Open Positions</h2>
              <span class="text-xs text-gray-400">{{ companyJobs().length }} jobs</span>
            </div>

            @if (jobsLoading()) {
              <div class="p-6 space-y-3 animate-pulse">
                @for (sk of [1,2,3]; track sk) {
                  <div class="h-16 bg-gray-100 rounded-xl"></div>
                }
              </div>
            } @else if (companyJobs().length === 0) {
              <div class="text-center py-16">
                <lucide-icon name="briefcase" class="w-12 h-12 text-gray-300 mx-auto mb-3"></lucide-icon>
                <p class="text-gray-500 text-sm">No open positions at this company right now.</p>
                <a routerLink="/jobs" class="mt-4 inline-block text-primary hover:underline text-sm">Browse all jobs</a>
              </div>
            } @else {
              <div class="divide-y divide-gray-100">
                @for (job of companyJobs(); track job.jobId) {
                  <a [routerLink]="['/jobs', job.jobId]"
                    class="flex items-center justify-between px-6 py-4 hover:bg-gray-50 transition-colors group">
                    <div>
                      <p class="font-semibold text-gray-900 group-hover:text-primary transition-colors">{{ job.title }}</p>
                      <div class="flex items-center gap-3 text-xs text-gray-500 mt-1">
                        <span class="flex items-center gap-1"><lucide-icon name="map-pin" class="w-3 h-3"></lucide-icon>{{ job.location }}</span>
                        @if (job.salary) {
                          <span class="flex items-center gap-1 text-green-600"><lucide-icon name="banknote" class="w-3 h-3"></lucide-icon>{{ job.salary }}</span>
                        }
                      </div>
                    </div>
                    <div class="flex items-center gap-3">
                      <span class="bg-green-50 text-green-700 text-xs font-semibold px-2.5 py-1 rounded-full border border-green-200">Open</span>
                      <lucide-icon name="arrow-right" class="w-4 h-4 text-gray-300 group-hover:text-primary transition-colors"></lucide-icon>
                    </div>
                  </a>
                }
              </div>
            }
          </div>
        </div>
      }
    </div>
  `
})
export class CompanyDetailComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private api = inject(ApiService);
  private auth = inject(AuthService);
  private toast = inject(ToastService);

  company = signal<any>(null);
  companyJobs = signal<Job[]>([]);
  loading = signal(true);
  jobsLoading = signal(true);

  isOwner = () => {
    const c = this.company();
    const userId = this.auth.getCurrentUser()?.userId;
    return c && userId && c.createdByUserId === userId;
  };

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id') || '';
    if (!id) { this.loading.set(false); return; }

    this.api.getCompanyById(id).subscribe({
      next: (res: any) => {
        this.company.set(res?.data ?? res);
        this.loading.set(false);
        this.loadCompanyJobs(res?.data?.name ?? res?.name);
      },
      error: () => {
        this.toast.error('Failed to load company');
        this.loading.set(false);
      }
    });
  }

  private loadCompanyJobs(companyName: string): void {
    if (!companyName) { this.jobsLoading.set(false); return; }
    // Use search-service to find jobs by company name
    this.api.searchJobs(companyName, '').pipe(
      map((res: any) => {
        const jobs: Job[] = Array.isArray(res) ? res : (res?.data ?? []);
        return jobs.filter((j: any) =>
          j.company?.toLowerCase() === companyName.toLowerCase() && j.status === 'OPEN'
        );
      }),
      catchError(() => of([]))
    ).subscribe(jobs => {
      this.companyJobs.set(jobs);
      this.jobsLoading.set(false);
    });
  }
}
