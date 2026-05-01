import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { ApiService } from '../../../core/services/api.service';
import { AuthService } from '../../../core/services/auth.service';
import { ToastService } from '../../../core/services/toast.service';
import { Job, Company } from '../../../shared/models/models';
import { LucideAngularModule } from 'lucide-angular';
import { catchError, of } from 'rxjs';
import { ScrollRevealDirective } from '../../../shared/directives/scroll-reveal.directive';
import { RippleDirective } from '../../../shared/directives/ripple.directive';

@Component({
  selector: 'app-job-detail',
  standalone: true,
  imports: [CommonModule, RouterLink, LucideAngularModule, ScrollRevealDirective, RippleDirective],
  template: `
    <div class="min-h-screen bg-gray-50 flex flex-col">
      <!-- Top banner for company branding - Full Width Mesh Gradient -->
      @if (job() && !loading()) {
        <div class="h-48 md:h-60 w-full bg-animated-gradient relative rounded-b-[2.5rem] shadow-lg overflow-hidden">
          <div class="absolute inset-0 bg-[url('https://www.transparenttextures.com/patterns/cubes.png')] opacity-20 mix-blend-overlay rounded-b-[2.5rem]"></div>
          <!-- Animated mesh gradient blobs -->
          <div class="absolute top-10 left-10 w-80 h-80 bg-purple-500/20 rounded-full blur-3xl animate-blob pointer-events-none"></div>
          <div class="absolute bottom-10 right-10 w-96 h-96 bg-blue-500/20 rounded-full blur-3xl animate-blob-delay pointer-events-none"></div>
          <div class="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[500px] h-[500px] bg-pink-500/15 rounded-full blur-3xl animate-blob-slow pointer-events-none"></div>
        </div>
      }

      <div class="max-w-7xl mx-auto px-6 pb-12 pt-6 w-full flex-grow relative" [class.-mt-20]="job() && !loading()">
        <!-- Back Link -->
        <a routerLink="/jobs" class="inline-flex items-center gap-1 text-sm font-medium transition-colors mb-6"
           [class.text-white]="job() && !loading()" [class.hover:text-white/80]="job() && !loading()"
           [class.text-gray-500]="loading()" [class.hover:text-primary]="loading()">
          <lucide-icon name="arrow-left" class="w-4 h-4"></lucide-icon> Back to jobs
        </a>

        @if (loading()) {
          <!-- Skeleton Layout -->
          <div class="grid grid-cols-1 lg:grid-cols-[1fr_360px] gap-8 animate-pulse">
            <div class="space-y-6">
              <div class="bg-white rounded-2xl border border-gray-100 p-8 h-48 shadow-sm"></div>
              <div class="bg-white rounded-2xl border border-gray-100 p-8 h-96 shadow-sm"></div>
            </div>
            <aside class="space-y-6">
              <div class="bg-white rounded-2xl border border-gray-100 h-64 shadow-sm"></div>
            </aside>
          </div>
        }

        @if (job() && !loading()) {
          <div class="grid grid-cols-1 lg:grid-cols-[1fr_360px] gap-8">
            
            <!-- Left: Job Info Main -->
            <main>
              <!-- Job Header -->
              <div appScrollReveal class="bento-card border border-gray-100 p-8 mb-6 relative hover-lift glow-card card-shine shadow-sm z-10">
                <div class="flex items-start gap-6">
                  <!-- Company Logo - 64px rounded-square with border -->
                  <div class="w-20 h-20 bg-white rounded-2xl flex-shrink-0 border-4 border-white shadow-[0_8px_30px_-6px_rgba(0,0,0,0.15)] flex items-center justify-center font-bold text-4xl text-gray-300 -mt-20 relative overflow-hidden transition-all duration-300 hover:scale-110 hover:shadow-[0_12px_40px_-8px_rgba(0,0,0,0.2)]">
                    @if (companyDetails()?.logoUrl) {
                      <img [src]="companyDetails()?.logoUrl" alt="Company Logo" class="w-full h-full object-cover rounded-xl">
                    } @else {
                      {{ companyDetails()?.name ? companyDetails()!.name.substring(0,2).toUpperCase() : job()?.company ? job()!.company.substring(0,2).toUpperCase() : 'CO' }}
                    }
                  </div>
                  <div class="flex-1 mt-2 md:-mt-4">
                    <div class="flex justify-between items-start mb-3">
                      <div class="flex items-center gap-3 flex-wrap">
                        <h1 class="text-3xl font-black text-gray-900 tracking-tight">{{ job()!.title }}</h1>
                      </div>

                      @if (job()!.status === 'CLOSED') {
                        <span class="badge badge-closed ml-2 animate-pulse">Closed</span>
                      } @else {
                        <span class="badge badge-active ml-2 animate-pulse-glow">Actively Hiring</span>
                      }
                    </div>
                    <div class="flex flex-wrap items-center gap-2 text-xs font-semibold text-gray-500 mb-8">
                      <span class="bg-gray-100 px-3 py-1.5 rounded-lg flex items-center gap-1.5 text-gray-700 border border-gray-200"><lucide-icon name="building-2" class="w-3.5 h-3.5"></lucide-icon> {{ companyDetails()?.name || job()!.company }}</span>
                      <span class="bg-gray-100 px-3 py-1.5 rounded-lg flex items-center gap-1.5 text-gray-700 border border-gray-200"><lucide-icon name="map-pin" class="w-3.5 h-3.5"></lucide-icon> {{ job()!.location }}</span>
                      <span class="bg-gray-100 px-3 py-1.5 rounded-lg flex items-center gap-1.5 text-gray-700 border border-gray-200"><lucide-icon name="clock" class="w-3.5 h-3.5"></lucide-icon> {{ job()!.createdAt | date:'shortDate' }}</span>
                    </div>

                    <!-- Header Actions -->
                    <div class="flex flex-wrap gap-3">
                      @if (isJobSeeker() && job()!.status === 'OPEN') {
                        <button (click)="apply()" [disabled]="applied() || applying()" appRipple 
                                class="btn-primary py-3 px-10 shadow-lg text-base hover-lift relative overflow-hidden group/apply"
                                [class.opacity-60]="applied() || applying()" 
                                [class.cursor-not-allowed]="applied() || applying()"
                                [class.bg-green-500]="applied()"
                                [class.hover:bg-green-600]="applied()">
                          <span class="flex items-center gap-2 transition-all duration-300" 
                                [class.translate-x-0]="!applied()"
                                [class.-translate-x-2]="applied()">
                            @if (applied()) {
                              <lucide-icon name="check" class="w-5 h-5 animate-bounce-in"></lucide-icon>
                            } @else if (applying()) {
                              <lucide-icon name="loader-2" class="w-5 h-5 animate-spin"></lucide-icon>
                            }
                            {{ applied() ? 'Applied Successfully' : applying() ? 'Applying...' : 'Apply Now' }}
                          </span>
                        </button>
                      }
                      <button (click)="toggleBookmark()" appRipple class="btn-outline shadow-sm py-3 px-6 transition-colors hover-lift"
                              [class.text-primary]="isBookmarked()"
                              [class.border-primary]="isBookmarked()"
                              [class.bg-primary-light]="isBookmarked()"
                              [class.text-gray-700]="!isBookmarked()"
                              [class.bg-white]="!isBookmarked()"
                              [class.border-gray-300]="!isBookmarked()"
                              [class.hover:bg-gray-50]="!isBookmarked()">
                        <lucide-icon name="bookmark" class="w-4 h-4" [class.fill-primary]="isBookmarked()"></lucide-icon> 
                        {{ isBookmarked() ? 'Saved' : 'Save Job' }}
                      </button>
                      <button type="button" (click)="shareJob()" appRipple title="Share job" aria-label="Share job"
                        class="w-12 h-12 rounded-lg border border-gray-300 flex items-center justify-center text-gray-700 hover:bg-gray-50 transition-colors shadow-sm bg-white hover-lift">
                        <lucide-icon name="share-2" class="w-4 h-4"></lucide-icon>
                      </button>
                    </div>
                  </div>
                </div>
              </div>

              <!-- Content Body -->
              <div appScrollReveal [delay]="100" class="bento-card border border-gray-100 p-8 mb-6 hover-lift card-shine z-0">
                <h3 class="text-xl font-bold tracking-tight mb-5">About the Role</h3>
                <div class="prose prose-sm text-gray-600 max-w-none whitespace-pre-line leading-loose text-base font-medium">
                  {{ job()!.description || 'No detailed description provided.' }}
                </div>
              </div>
            </main>

            <!-- Right: Sidebar Cards -->
            <aside class="space-y-6 lg:sticky lg:top-20 h-fit">
              <!-- Job Overview -->
              <div appScrollReveal [delay]="150" class="card p-0 overflow-hidden hover-lift card-shine">
                <div class="bg-gray-50 border-b border-gray-100 px-6 py-4">
                  <h3 class="font-semibold text-gray-900 text-sm">Job Overview</h3>
                </div>
                <div class="p-6 space-y-5">
                  <div class="flex items-start gap-3">
                    <lucide-icon name="calendar" class="w-5 h-5 text-gray-400 mt-0.5"></lucide-icon>
                    <div>
                      <p class="text-xs text-gray-500 font-medium">Posted</p>
                      <p class="text-sm font-medium text-gray-900">{{ job()!.createdAt | date:'longDate' }}</p>
                    </div>
                  </div>
                  <div class="flex items-start gap-3">
                    <lucide-icon name="banknote" class="w-5 h-5 text-gray-400 mt-0.5"></lucide-icon>
                    <div>
                      <p class="text-xs text-gray-500 font-medium">Salary</p>
                      <p class="text-sm font-medium text-green-600">{{ job()!.salary || 'Not specified' }}</p>
                    </div>
                  </div>
                  <div class="flex items-start gap-3">
                    <lucide-icon name="map" class="w-5 h-5 text-gray-400 mt-0.5"></lucide-icon>
                    <div>
                      <p class="text-xs text-gray-500 font-medium">Location</p>
                      <p class="text-sm font-medium text-gray-900">{{ job()!.location }}</p>
                    </div>
                  </div>
                </div>
              </div>

              <!-- About Company Card -->
              @if (companyDetails()) {
                <div appScrollReveal [delay]="200" class="card p-0 overflow-hidden hover-lift card-shine">
                  <div class="bg-gray-900 px-6 py-4 border-b border-gray-800">
                    <h3 class="font-semibold text-white text-sm">About {{ companyDetails()!.name }}</h3>
                  </div>
                  <div class="p-6">
                    <div class="flex items-center gap-3 mb-5">
                      <p class="text-sm text-gray-600 leading-relaxed">{{ companyDetails()!.description || 'No company details available.' }}</p>
                    </div>
                    @if (companyDetails()!.website) {
                      <a [href]="companyDetails()!.website.startsWith('http') ? companyDetails()!.website : 'https://' + companyDetails()!.website" target="_blank" rel="noopener noreferrer"
                        class="w-full inline-flex justify-center items-center gap-1 bg-gray-50 border border-gray-200 text-sm font-semibold text-gray-700 py-2 rounded-lg hover:bg-gray-100 transition-colors">
                        <lucide-icon name="external-link" class="w-4 h-4"></lucide-icon>
                        View full profile
                      </a>
                    }
                  </div>
                </div>
              }

              @if (isRecruiter() && isOwnRecruiterJob()) {
                @if (job()!.status === 'OPEN') {
                  <button (click)="closeJob()" appRipple
                    class="w-full bg-red-50 text-red-600 border border-red-200 font-semibold py-3 rounded-xl hover:bg-red-100 transition shadow-sm flex items-center justify-center gap-2 hover-lift">
                    <lucide-icon name="x" class="w-4 h-4"></lucide-icon>
                    Close Job Posting
                  </button>
                } @else {
                  <button (click)="reopenJob()" appRipple
                    class="w-full bg-green-50 text-green-700 border border-green-200 font-semibold py-3 rounded-xl hover:bg-green-100 transition shadow-sm flex items-center justify-center gap-2 hover-lift">
                    <lucide-icon name="refresh-cw" class="w-4 h-4"></lucide-icon>
                    Reopen Job Posting
                  </button>
                }
              }

            </aside>
          </div>
        }
      </div>
    </div>
  `
})
export class JobDetailComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private api = inject(ApiService);
  private auth = inject(AuthService);
  private toast = inject(ToastService);

  readonly job = signal<Job | null>(null);
  readonly companyDetails = signal<Company | null>(null);
  readonly loading = signal(true);
  readonly applied = signal(false);
  readonly applying = signal(false);
  readonly isBookmarked = signal(false);
  private jobId = '';

  readonly isJobSeeker = computed(() => this.auth.isJobSeeker());
  readonly isRecruiter = computed(() => this.auth.isRecruiter());
  readonly isOwnRecruiterJob = computed(() => {
    const job = this.job();
    const userId = this.auth.getUserId();
    return !!job && !!userId && job.recruiterId === userId;
  });

  ngOnInit(): void {
    this.jobId = this.route.snapshot.paramMap.get('id') || '';
    this.applied.set(false);
    this.isBookmarked.set(false);
    this.loading.set(true);
    this.job.set(null);
    this.companyDetails.set(null);
    this.load();
  }

  private load(): void {
    const id = this.jobId;
    if (!id) {
      this.loading.set(false);
      this.toast.error('Invalid job ID');
      return;
    }

    this.api.getJobById(id).subscribe({
      next: (res) => {
        const job = res.data;
        this.job.set(job);
        this.loading.set(false);

        if (job?.companyId) {
          this.api.getCompanyById(job.companyId).pipe(
            catchError(() => of(null))
          ).subscribe((companyRes: any) => {
            const company = companyRes?.data ?? companyRes;
            if (company && company.name) this.companyDetails.set(company);
          });
        }

        this.loadBookmarkState(id);
        this.loadAppliedState(id);
      },
      error: () => {
        this.toast.error('Unable to load job details');
        this.loading.set(false);
      }
    });
  }

  apply(): void {
    const user = this.auth.getCurrentUser();
    const job = this.job();
    if (!job) return;
    if (!this.isJobSeeker()) {
      this.toast.error('Only job seekers can apply for jobs');
      return;
    }
    const userId = this.auth.getUserId();
    if (!userId) {
      this.toast.error('Please login again to apply');
      return;
    }
    const jobId = (job as any).jobId || (job as any).id || (job as any)['id'];
    this.applying.set(true);
    
    this.api.getMyResumes().subscribe({
      next: (res: any) => {
        const resumes = Array.isArray(res) ? res : (res?.data || []);
        const latest = resumes?.[0];
        const resumeId = latest?.resumeId || latest?.id;
        if (!resumeId) {
          this.applying.set(false);
          this.toast.error('Please upload your resume before applying');
          return;
        }
        this.api.applyForJob(jobId, userId, resumeId).subscribe({
          next: () => {
            this.applied.set(true);
            this.applying.set(false);
            this.toast.success('Application submitted!');
          },
          error: (err: any) => {
            this.applying.set(false);
            this.toast.error(err.error?.message || 'Failed to apply');
          }
        });
      },
      error: () => {
        this.applying.set(false);
        this.toast.error('Unable to verify resume. Please try again.');
      }
    });
  }

  closeJob(): void {
    const job = this.job();
    if (!job) return;
    const jobId = (job as any).jobId || (job as any).id || (job as any)['id'];
    this.api.closeJob(jobId).subscribe({
      next: (res) => { this.job.set(res.data); this.toast.success('Job closed'); },
      error: () => this.toast.error('Failed to close job')
    });
  }

  reopenJob(): void {
    const job = this.job();
    if (!job) return;
    const jobId = (job as any).jobId || (job as any).id || (job as any)['id'];
    this.api.reopenJob(jobId).subscribe({
      next: (res) => {
        this.job.set(res.data);
        this.toast.success('Job reopened — it is now visible to job seekers');
      },
      error: () => this.toast.error('Failed to reopen job')
    });
  }

  // --- Bookmarking ---
  loadBookmarkState(jobId: string): void {
    if (!this.isJobSeeker()) return;
    this.api.getBookmarks().subscribe({
      next: (res: any) => {
        const bmks = Array.isArray(res) ? res : (res.data || []);
        this.isBookmarked.set(bmks.some((b: any) => (b.jobId || b.id) === jobId));
      },
      error: () => {
        this.isBookmarked.set(false);
      }
    });
  }

  toggleBookmark(): void {
    const job = this.job();
    if (!job || !this.isJobSeeker()) { 
      this.toast.info('Please log in as a Job Seeker to save jobs'); 
      return; 
    }
    const jobId = (job as any).jobId || (job as any).id || (job as any)['id'];

    if (this.isBookmarked()) {
      this.api.removeBookmark(jobId).subscribe({
        next: () => { 
          this.isBookmarked.set(false);
          this.toast.success('Removed from saved jobs'); 
        },
        error: () => this.toast.error('Failed to remove bookmark')
      });
    } else {
      this.api.bookmarkJob(jobId).subscribe({
        next: () => { 
          this.isBookmarked.set(true);
          this.toast.success('Job saved successfully'); 
        },
        error: () => this.toast.error('Failed to save job')
      });
    }
  }

  private loadAppliedState(jobId: string): void {
    if (!this.isJobSeeker()) return;
    const userId = this.auth.getUserId();
    if (!userId) return;

    this.api.getMyApplications(userId).subscribe({
      next: (res) => {
        const applications = res.data ?? [];
        this.applied.set(applications.some(app => app.jobId === jobId));
      },
      error: () => {
        this.applied.set(false);
      }
    });
  }

  shareJob(): void {
    const job = this.job();
    if (!job) return;

    const url = window.location.href;
    if (navigator.share) {
      navigator.share({ title: job.title, text: `${job.title} at ${job.company}`, url })
        .catch(() => {
          // User cancelled the native share sheet.
        });
      return;
    }

    if (!navigator.clipboard) {
      this.toast.error('Clipboard is unavailable in this browser');
      return;
    }

    navigator.clipboard.writeText(url)
      .then(() => this.toast.success('Job link copied'))
      .catch(() => this.toast.error('Unable to copy job link'));
  }
}
