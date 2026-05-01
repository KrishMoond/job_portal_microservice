import { Component, OnInit, inject, signal, computed, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { ApiService } from '../../../core/services/api.service';
import { AuthService } from '../../../core/services/auth.service';
import { ToastService } from '../../../core/services/toast.service';
import { Job } from '../../../shared/models/models';
import { LucideAngularModule } from 'lucide-angular';
import { forkJoin, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';

interface JobWithCount extends Job { applicantCount: number; }

@Component({
  selector: 'app-recruiter-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink, LucideAngularModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="min-h-screen bg-gray-50 py-10 px-6">
      <div class="max-w-7xl mx-auto">

        <!-- Header -->
        <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-8">
          <div>
            <h1 class="text-2xl font-bold text-gray-900 tracking-tight">Recruiter Dashboard</h1>
            <p class="text-gray-500 text-sm mt-1">Manage jobs, track applicants, and hire top talent.</p>
          </div>
          <a routerLink="/recruiter/post-job" class="btn-primary py-2.5 px-6 shadow-sm flex items-center justify-center gap-2">
            <lucide-icon name="plus" class="w-4 h-4"></lucide-icon> Post a Job
          </a>
        </div>

        <!-- Bento Box Analytics — derived from real job + applicant data -->
        <div class="bento-grid mb-10">
          <div class="bento-card col-span-1 md:col-span-2 bg-animated-gradient text-white flex flex-col justify-center relative overflow-hidden">
            <!-- Animated mesh gradient blobs -->
            <div class="absolute -right-20 -top-20 w-80 h-80 bg-white/10 rounded-full blur-3xl animate-blob"></div>
            <div class="absolute -left-20 -bottom-20 w-80 h-80 bg-purple-500/20 rounded-full blur-3xl animate-blob-delay"></div>
            <div class="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-96 h-96 bg-blue-500/15 rounded-full blur-3xl animate-blob-slow"></div>
            
            <h2 class="text-3xl font-black mb-2 relative z-10">Welcome Back!</h2>
            <p class="text-white/80 font-medium mb-6 relative z-10 max-w-sm">Here is a summary of your entire recruitment pipeline based on real-time database analytics.</p>
            <div class="flex gap-4 relative z-10">
              <div class="glass flex flex-col px-6 py-4 rounded-2xl w-full">
                <span class="text-xs uppercase tracking-wider font-bold text-white/70 mb-1">Total Jobs</span>
                @if (loading()) { <div class="h-8 bg-white/20 rounded animate-pulse w-12"></div> }
                @else { <span class="text-4xl font-black text-white">{{ totalJobs() }}</span> }
              </div>
              <div class="glass flex flex-col px-6 py-4 rounded-2xl w-full">
                <span class="text-xs uppercase tracking-wider font-bold text-white/70 mb-1">Open Positions</span>
                @if (loading()) { <div class="h-8 bg-white/20 rounded animate-pulse w-12"></div> }
                @else { <span class="text-4xl font-black text-white">{{ openJobs() }}</span> }
              </div>
            </div>
          </div>

          <div class="bento-card flex flex-col items-center justify-center text-center group">
            <div class="w-20 h-20 rounded-full bg-primary/10 flex items-center justify-center text-primary group-hover:bg-primary group-hover:text-white transition-all duration-500 mb-4 group-hover:scale-110 shadow-lg group-hover:shadow-glow-primary">
              <lucide-icon name="users" class="w-10 h-10"></lucide-icon>
            </div>
            <p class="text-sm font-semibold text-gray-500 mb-1 uppercase tracking-wide">Total Applicants</p>
            @if (applicantCountLoading()) {
              <div class="h-10 bg-gray-100 rounded w-16 animate-pulse mt-1"></div>
            } @else {
              <p class="text-5xl font-black text-gray-900 tracking-tighter">{{ totalApplicants() }}</p>
              <p class="text-xs text-gray-500 font-bold mt-2 flex items-center gap-1">
                <lucide-icon name="users" class="w-3 h-3"></lucide-icon> Across your postings
              </p>
            }
          </div>

          <div class="bento-card flex flex-col items-center justify-center text-center group">
            <div class="w-20 h-20 rounded-full bg-green-50 flex items-center justify-center text-green-600 group-hover:bg-green-500 group-hover:text-white transition-all duration-500 mb-4 group-hover:scale-110 shadow-lg group-hover:shadow-glow-accent">
              <lucide-icon name="send" class="w-10 h-10"></lucide-icon>
            </div>
            <p class="text-sm font-semibold text-gray-500 mb-1 uppercase tracking-wide">Total Applications</p>
            @if (loadingStats()) {
              <div class="h-10 bg-gray-100 rounded w-16 animate-pulse mt-1"></div>
            } @else {
              <p class="text-5xl font-black text-gray-900 tracking-tighter">{{ stats().applications || 0 }}</p>
              <p class="text-xs text-gray-500 font-bold mt-2 flex items-center gap-1">
                <lucide-icon name="bar-chart-3" class="w-3 h-3"></lucide-icon> From analytics service
              </p>
            }
          </div>

          <div class="bento-card flex flex-col items-center justify-center text-center group">
            <div class="w-20 h-20 rounded-full bg-amber-50 flex items-center justify-center text-amber-600 group-hover:bg-amber-500 group-hover:text-white transition-all duration-500 mb-4 group-hover:scale-110 shadow-lg">
              <lucide-icon name="file-text" class="w-10 h-10"></lucide-icon>
            </div>
            <p class="text-sm font-semibold text-gray-500 mb-1 uppercase tracking-wide">Resumes Uploaded</p>
            @if (loadingStats()) {
              <div class="h-10 bg-gray-100 rounded w-16 animate-pulse mt-1"></div>
            } @else {
              <p class="text-5xl font-black text-gray-900 tracking-tighter">{{ stats().resumeUploads || 0 }}</p>
              <p class="text-xs text-gray-500 font-bold mt-2 flex items-center gap-1">
                <lucide-icon name="file-text" class="w-3 h-3"></lucide-icon> Candidate documents
              </p>
            }
          </div>
        </div>

        <!-- Jobs Table Overview -->
        <div class="bg-white rounded-xl border border-gray-200 shadow-sm overflow-hidden">
          <div class="px-6 py-5 border-b border-gray-200 flex items-center justify-between">
            <h2 class="font-semibold text-gray-900">My Job Postings</h2>
            <span class="text-xs text-gray-400">{{ jobs().length }} total</span>
          </div>

          @if (loading()) {
            <div class="overflow-hidden">
              <table class="w-full text-left text-sm whitespace-nowrap">
                <thead class="bg-gray-50 border-b border-gray-100">
                  <tr>
                    @for (w of ['w-32','w-20','w-24','w-24','w-16']; track w) {
                      <th class="px-6 py-4"><div class="h-4 bg-gray-200 rounded {{ w }} animate-pulse"></div></th>
                    }
                  </tr>
                </thead>
                <tbody class="divide-y divide-gray-100">
                  @for (sk of [1,2,3]; track sk) {
                    <tr class="animate-pulse">
                      <td class="px-6 py-4 space-y-2">
                        <div class="h-4 bg-gray-200 rounded w-48"></div>
                        <div class="h-3 bg-gray-200 rounded w-24"></div>
                      </td>
                      <td class="px-6 py-4"><div class="h-6 bg-gray-200 rounded-full w-20"></div></td>
                      <td class="px-6 py-4"><div class="h-4 bg-gray-200 rounded w-16"></div></td>
                      <td class="px-6 py-4"><div class="h-4 bg-gray-200 rounded w-24"></div></td>
                      <td class="px-6 py-4 text-right"><div class="h-8 bg-gray-200 rounded-lg w-20 ml-auto"></div></td>
                    </tr>
                  }
                </tbody>
              </table>
            </div>
          } @else {
            <div class="overflow-x-auto">
              <table class="w-full text-left text-sm whitespace-nowrap">
                <thead class="bg-gray-50 text-gray-500 font-medium border-b border-gray-200">
                  <tr>
                    <th class="px-6 py-4">Job Title</th>
                    <th class="px-6 py-4">Status</th>
                    <th class="px-6 py-4">Applicants</th>
                    <th class="px-6 py-4">Date Posted</th>
                    <th class="px-6 py-4 text-right">Actions</th>
                  </tr>
                </thead>
                <tbody class="divide-y divide-gray-100">
                  @for (job of jobs(); track (job.jobId + '-' + $index); let i = $index) {
                    <tr class="hover:bg-primary/5 transition-colors group cursor-pointer"
                        [class.bg-gray-50/50]="i % 2 === 1">
                      <td class="px-6 py-4">
                        <p class="font-bold text-gray-900 group-hover:text-primary transition-colors tracking-tight">{{ job.title }}</p>
                        <p class="text-xs font-medium text-gray-500 mt-1 bg-gray-100 inline-flex items-center px-2 py-0.5 rounded-md">
                          {{ job.location }}
                        </p>
                      </td>
                      <td class="px-6 py-4">
                        @if (job.status === 'OPEN') {
                          <span class="badge badge-active">Active</span>
                        } @else {
                          <span class="badge badge-closed">Closed</span>
                        }
                      </td>
                      <td class="px-6 py-4">
                        <div class="flex items-center gap-2">
                          <div class="w-8 h-8 rounded-full bg-blue-50 flex items-center justify-center text-blue-500 border border-blue-100">
                             <lucide-icon name="users" class="w-4 h-4"></lucide-icon>
                          </div>
                          @if (applicantCountLoading()) {
                            <span class="inline-block h-4 bg-gray-200 rounded w-6 animate-pulse"></span>
                          } @else {
                            <span class="font-bold text-gray-900 text-base">{{ job.applicantCount }}</span>
                          }
                        </div>
                      </td>
                      <td class="px-6 py-4 text-gray-500 font-medium">{{ job.createdAt | date:'mediumDate' }}</td>
                      <td class="px-6 py-4 text-right space-x-2">
                        <a [routerLink]="['/recruiter/applicants', job.jobId]"
                          class="inline-flex items-center justify-center w-9 h-9 rounded-lg bg-primary-light text-primary hover:bg-primary hover:text-white transition-all shadow-sm hover:shadow"
                          title="View Applicants"
                          aria-label="View applicants">
                          <lucide-icon name="eye" class="w-4 h-4"></lucide-icon>
                        </a>
                        <a [routerLink]="['/recruiter/pipeline', job.jobId]"
                          class="inline-flex items-center justify-center w-9 h-9 rounded-lg bg-indigo-50 text-indigo-600 hover:bg-indigo-500 hover:text-white transition-all shadow-sm hover:shadow"
                          title="Kanban Pipeline"
                          aria-label="Open applicant pipeline">
                          <lucide-icon name="bar-chart-3" class="w-4 h-4"></lucide-icon>
                        </a>
                        @if (job.status === 'OPEN') {
                          <button (click)="closeJob(job.jobId)"
                            class="inline-flex items-center justify-center w-9 h-9 rounded-lg bg-red-50 text-red-500 hover:bg-red-500 hover:text-white transition-all shadow-sm hover:shadow"
                            title="Close Job"
                            aria-label="Close job">
                            <lucide-icon name="x" class="w-4 h-4"></lucide-icon>
                          </button>
                        } @else {
                          <button (click)="reopenJob(job.jobId)"
                            class="inline-flex items-center justify-center w-9 h-9 rounded-lg bg-green-50 text-green-600 hover:bg-green-500 hover:text-white transition-all shadow-sm hover:shadow"
                            title="Reopen Job"
                            aria-label="Reopen job">
                            <lucide-icon name="refresh-cw" class="w-4 h-4"></lucide-icon>
                          </button>
                        }
                      </td>
                    </tr>
                  }
                </tbody>
              </table>
            </div>

            @if (jobs().length === 0) {
              <div class="text-center py-16">
                <div class="w-16 h-16 bg-gray-50 rounded-full flex items-center justify-center mx-auto mb-4 border border-gray-100 text-gray-300">
                  <lucide-icon name="briefcase" class="w-8 h-8"></lucide-icon>
                </div>
                <h3 class="text-lg font-semibold text-gray-900 mb-1">No jobs posted yet</h3>
                <p class="text-gray-500 text-sm mb-6">Create your first job listing to get started.</p>
                <a routerLink="/recruiter/post-job" class="btn-primary py-2 px-6">Post a Job</a>
              </div>
            }
          }
        </div>
      </div>
    </div>
  `
})
export class RecruiterDashboardComponent implements OnInit {
  private api   = inject(ApiService);
  private auth  = inject(AuthService);
  private toast = inject(ToastService);

  private _jobs = signal<JobWithCount[]>([]);
  loading = signal(true);
  applicantCountLoading = signal(true);
  loadingStats = signal(true);
  stats = signal<any>({});

  // ── Computed metrics derived directly from loaded job data ─────────────
  jobs          = computed(() => this._jobs());
  totalJobs     = computed(() => this._jobs().length);
  openJobs      = computed(() => this._jobs().filter(j => j.status === 'OPEN').length);
  totalApplicants = computed(() => this._jobs().reduce((sum, j) => sum + j.applicantCount, 0));

  ngOnInit(): void {
    const user = this.auth.getCurrentUser();
    this.loading.set(true);
    this.applicantCountLoading.set(true);
    this.loadingStats.set(true);

    // Load analytics summary (applications count, resume uploads)
    this.api.getAnalyticsSummary().subscribe({
      next: (res: any) => {
        this.stats.set(res?.data ?? res ?? {});
        this.loadingStats.set(false);
      },
      error: () => this.loadingStats.set(false)
    });

    this.api.getJobs().subscribe({
      next: (res) => {
        const myJobs = (res.data ?? [])
          .filter((j: Job) => j.recruiterId === user?.userId)
          .map(j => ({ ...j, applicantCount: 0 }));

        this._jobs.set(myJobs);
        this.loading.set(false);

        if (myJobs.length === 0) {
          this.applicantCountLoading.set(false);
          return;
        }

        // Load applicant counts for all jobs in parallel
        const requests = myJobs.map(j =>
          this.api.getJobApplications(j.jobId).pipe(
            map(r => ({ jobId: j.jobId, count: (r.data ?? []).length })),
            catchError(() => of({ jobId: j.jobId, count: 0 }))
          )
        );

        forkJoin(requests).subscribe({
          next: (results) => {
            this._jobs.update(jobs =>
              jobs.map(j => {
                const found = results.find(r => r.jobId === j.jobId);
                return found ? { ...j, applicantCount: found.count } : j;
              })
            );
            this.applicantCountLoading.set(false);
          },
          error: () => {
            this.applicantCountLoading.set(false);
          }
        });
      },
      error: () => {
        this.toast.error('Failed to load jobs');
        this.loading.set(false);
        this.applicantCountLoading.set(false);
      }
    });
  }

  closeJob(jobId: string): void {
    this.api.closeJob(jobId).subscribe({
      next: () => {
        this._jobs.update(jobs =>
          jobs.map(j => j.jobId === jobId ? { ...j, status: 'CLOSED' as const } : j)
        );
        this.toast.success('Job closed successfully');
      },
      error: () => this.toast.error('Failed to close job')
    });
  }

  reopenJob(jobId: string): void {
    this.api.reopenJob(jobId).subscribe({
      next: () => {
        this._jobs.update(jobs =>
          jobs.map(j => j.jobId === jobId ? { ...j, status: 'OPEN' as const } : j)
        );
        this.toast.success('Job reopened — it is now visible to job seekers');
      },
      error: () => this.toast.error('Failed to reopen job')
    });
  }
}
