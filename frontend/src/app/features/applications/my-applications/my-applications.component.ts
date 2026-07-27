import { Component, OnInit, inject, signal, computed, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ApiService } from '../../../core/services/api.service';
import { AuthService } from '../../../core/services/auth.service';
import { ToastService } from '../../../core/services/toast.service';
import { Application, Interview, Job } from '../../../shared/models/models';
import { LucideAngularModule } from 'lucide-angular';
import { RouterLink } from '@angular/router';
import { forkJoin, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';

interface EnrichedApplication extends Application { company: string; }
type Tab = 'saved' | 'applied' | 'interviews' | 'archived';

const ARCHIVED_STATUSES = new Set(['REJECTED', 'OFFER_REJECTED', 'OFFER_ACCEPTED']);

@Component({
  selector: 'app-my-applications',
  standalone: true,
  imports: [CommonModule, LucideAngularModule, RouterLink],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="min-h-screen bg-gray-50">
      <div class="max-w-5xl mx-auto px-4 sm:px-6 py-8">

        <!-- Page Header -->
        <div class="mb-6">
          <h1 class="text-2xl font-bold text-gray-900">My Jobs</h1>
          <p class="text-sm text-gray-500 mt-1">Track your saved jobs, applications, interviews and history.</p>
        </div>

        <!-- Tab Bar -->
        <div class="flex items-center gap-1 bg-white border border-gray-200 rounded-2xl p-1.5 shadow-sm mb-6 w-fit">
          @for (tab of tabs; track tab.key) {
            <button
              (click)="setTab(tab.key)"
              class="flex items-center gap-2 px-4 py-2 rounded-xl text-sm font-semibold transition-all"
              [class]="activeTab() === tab.key
                ? 'bg-primary text-white shadow-sm'
                : 'text-gray-500 hover:text-gray-800 hover:bg-gray-50'">
              <lucide-icon [name]="tab.icon" class="w-4 h-4"></lucide-icon>
              {{ tab.label }}
              <span class="text-xs px-1.5 py-0.5 rounded-full font-bold"
                [class]="activeTab() === tab.key ? 'bg-white/20 text-white' : 'bg-gray-100 text-gray-500'">
                {{ tabCount(tab.key) }}
              </span>
            </button>
          }
        </div>

        <!-- SAVED TAB -->
        @if (activeTab() === 'saved') {
          @if (savedLoading()) {
            <ng-container *ngTemplateOutlet="skeletonList"></ng-container>
          } @else if (savedJobs().length === 0) {
            <ng-container *ngTemplateOutlet="emptyState; context: { icon: 'bookmark', title: 'No saved jobs', desc: 'Bookmark jobs you like to revisit them here.', link: '/jobs', linkLabel: 'Browse Jobs' }"></ng-container>
          } @else {
            <div class="space-y-3">
              @for (job of savedJobs(); track job.jobId) {
                <div class="bg-white rounded-xl border border-gray-200 shadow-sm p-5 flex flex-col sm:flex-row sm:items-center justify-between gap-4 hover:shadow-md transition-shadow">
                  <div class="flex items-center gap-4">
                    <div class="w-11 h-11 rounded-xl bg-gray-100 flex items-center justify-center font-bold text-gray-400 text-sm flex-shrink-0">
                      {{ (job.company || job.title).substring(0,2).toUpperCase() }}
                    </div>
                    <div>
                      <a [routerLink]="['/jobs', job.jobId]" class="font-semibold text-gray-900 hover:text-primary transition-colors">{{ job.title }}</a>
                      <p class="text-xs text-gray-500 mt-0.5 flex items-center gap-2">
                        <span>{{ job.company }}</span>
                        <span>·</span>
                        <span>{{ job.location }}</span>
                        @if (job.salary) { <span>·</span> <span class="text-green-600 font-semibold">{{ job.salary }}</span> }
                      </p>
                    </div>
                  </div>
                  <div class="flex items-center gap-2">
                    <span class="text-xs px-2 py-1 rounded-full font-semibold"
                      [class]="job.status === 'OPEN' ? 'bg-green-50 text-green-700' : 'bg-gray-100 text-gray-500'">
                      {{ job.status }}
                    </span>
                    <a [routerLink]="['/jobs', job.jobId]"
                      class="px-3 py-1.5 rounded-lg text-xs font-semibold bg-primary/5 text-primary hover:bg-primary/10 transition-colors">
                      Apply
                    </a>
                    <button (click)="removeBookmark(job.jobId)"
                      class="p-1.5 rounded-lg text-gray-400 hover:text-red-500 hover:bg-red-50 transition-colors" title="Remove">
                      <lucide-icon name="bookmark-minus" class="w-4 h-4"></lucide-icon>
                    </button>
                  </div>
                </div>
              }
            </div>
          }
        }

        <!-- APPLIED TAB -->
        @if (activeTab() === 'applied') {
          @if (appsLoading()) {
            <ng-container *ngTemplateOutlet="skeletonList"></ng-container>
          } @else if (activeApplications().length === 0) {
            <ng-container *ngTemplateOutlet="emptyState; context: { icon: 'file-text', title: 'No active applications', desc: 'Jobs you apply to will appear here.', link: '/jobs', linkLabel: 'Find Jobs' }"></ng-container>
          } @else {
            <div class="space-y-3">
              @for (app of activeApplications(); track app.id) {
                <div class="bg-white rounded-xl border border-gray-200 shadow-sm overflow-hidden hover:shadow-md transition-shadow">
                  <div class="p-5 flex flex-col sm:flex-row sm:items-center justify-between gap-4">
                    <div class="flex items-center gap-4">
                      <div class="w-11 h-11 rounded-xl border border-gray-100 bg-white shadow-sm flex items-center justify-center font-bold text-gray-400 text-sm flex-shrink-0">
                        {{ (app.company || app.jobTitle).substring(0,2).toUpperCase() }}
                      </div>
                      <div>
                        <a [routerLink]="['/jobs', app.jobId]" class="font-semibold text-gray-900 hover:text-primary transition-colors">{{ app.jobTitle }}</a>
                        <p class="text-xs text-gray-500 mt-0.5">{{ app.company || '—' }} · Applied {{ app.appliedAt | date:'mediumDate' }}</p>
                      </div>
                    </div>
                    <div class="flex items-center gap-2 flex-wrap">
                      <span class="badge" [ngClass]="statusClass(app.status)">{{ formatStatus(app.status) }}</span>
                      @if (app.status === 'HIRED') {
                        <button (click)="respondToOffer(app.id, true)" class="px-3 py-1.5 rounded-lg text-xs font-semibold bg-green-50 text-green-700 border border-green-200 hover:bg-green-100 transition-colors">Accept Offer</button>
                        <button (click)="respondToOffer(app.id, false)" class="px-3 py-1.5 rounded-lg text-xs font-semibold bg-red-50 text-red-700 border border-red-200 hover:bg-red-100 transition-colors">Decline</button>
                      }
                    </div>
                  </div>
                  <!-- Activity strip -->
                  <div class="px-5 pb-4 pt-0 flex items-center gap-6 border-t border-gray-50">
                    <div class="flex items-center gap-1.5 text-xs text-gray-400">
                      <lucide-icon name="send" class="w-3.5 h-3.5 text-primary"></lucide-icon>
                      <span>Submitted {{ app.appliedAt | date:'shortDate' }}</span>
                    </div>
                    @if (app.profileViewedAt) {
                      <div class="flex items-center gap-1.5 text-xs text-indigo-600 font-semibold">
                        <lucide-icon name="eye" class="w-3.5 h-3.5"></lucide-icon>
                        <span>Profile viewed {{ app.profileViewedAt | date:'shortDate' }}</span>
                      </div>
                    } @else {
                      <div class="flex items-center gap-1.5 text-xs text-gray-300">
                        <lucide-icon name="eye-off" class="w-3.5 h-3.5"></lucide-icon>
                        <span>Not yet viewed</span>
                      </div>
                    }
                    @if (app.status !== 'APPLIED') {
                      <div class="flex items-center gap-1.5 text-xs font-semibold" [class]="latestActionTextClass(app.status)">
                        <lucide-icon [name]="latestActionIcon(app.status)" class="w-3.5 h-3.5"></lucide-icon>
                        <span>{{ latestActionLabel(app.status) }}</span>
                      </div>
                    }
                  </div>
                </div>
              }
            </div>
          }
        }

        <!-- INTERVIEWS TAB -->
        @if (activeTab() === 'interviews') {
          @if (interviewsLoading()) {
            <ng-container *ngTemplateOutlet="skeletonList"></ng-container>
          } @else if (interviews().length === 0) {
            <ng-container *ngTemplateOutlet="emptyState; context: { icon: 'calendar', title: 'No interviews yet', desc: 'Interviews scheduled by recruiters will appear here.', link: null, linkLabel: null }"></ng-container>
          } @else {
            <div class="space-y-3">
              @for (iv of interviews(); track iv.id) {
                <div class="bg-white rounded-xl border border-gray-200 shadow-sm p-5 flex flex-col sm:flex-row sm:items-center justify-between gap-4 hover:shadow-md transition-shadow">
                  <div class="flex items-center gap-4">
                    <div class="w-11 h-11 rounded-xl flex items-center justify-center flex-shrink-0"
                      [class]="iv.status === 'CANCELED' ? 'bg-red-50 text-red-400' : iv.status === 'COMPLETED' ? 'bg-green-50 text-green-500' : 'bg-blue-50 text-blue-500'">
                      <lucide-icon name="calendar" class="w-5 h-5"></lucide-icon>
                    </div>
                    <div>
                      <p class="font-semibold text-gray-900">{{ iv.scheduledAt | date:'EEEE, MMMM d, y' }}</p>
                      <p class="text-xs text-gray-500 mt-0.5">{{ iv.scheduledAt | date:'h:mm a' }}</p>
                      @if (iv.meetingLink) {
                        <a [href]="iv.meetingLink" target="_blank" rel="noopener noreferrer"
                          class="text-xs font-bold text-primary hover:underline mt-1 inline-flex items-center gap-1">
                          <lucide-icon name="video" class="w-3 h-3"></lucide-icon> Join Meeting
                        </a>
                      }
                    </div>
                  </div>
                  <span class="text-xs px-3 py-1.5 rounded-full font-semibold self-start sm:self-auto"
                    [class]="iv.status === 'CANCELED' ? 'bg-red-50 text-red-600' : iv.status === 'COMPLETED' ? 'bg-green-50 text-green-700' : 'bg-blue-50 text-blue-700'">
                    {{ iv.status || 'SCHEDULED' }}
                  </span>
                </div>
              }
            </div>
          }
        }

        <!-- ARCHIVED TAB -->
        @if (activeTab() === 'archived') {
          @if (appsLoading()) {
            <ng-container *ngTemplateOutlet="skeletonList"></ng-container>
          } @else if (archivedApplications().length === 0) {
            <ng-container *ngTemplateOutlet="emptyState; context: { icon: 'archive', title: 'Nothing archived yet', desc: 'Closed applications (rejected, accepted offers) will appear here.', link: null, linkLabel: null }"></ng-container>
          } @else {
            <div class="space-y-3">
              @for (app of archivedApplications(); track app.id) {
                <div class="bg-white rounded-xl border border-gray-200 shadow-sm p-5 flex flex-col sm:flex-row sm:items-center justify-between gap-4 opacity-75 hover:opacity-100 transition-opacity">
                  <div class="flex items-center gap-4">
                    <div class="w-11 h-11 rounded-xl border border-gray-100 bg-gray-50 flex items-center justify-center font-bold text-gray-300 text-sm flex-shrink-0">
                      {{ (app.company || app.jobTitle).substring(0,2).toUpperCase() }}
                    </div>
                    <div>
                      <a [routerLink]="['/jobs', app.jobId]" class="font-semibold text-gray-700 hover:text-primary transition-colors">{{ app.jobTitle }}</a>
                      <p class="text-xs text-gray-400 mt-0.5">{{ app.company || '—' }} · Applied {{ app.appliedAt | date:'mediumDate' }}</p>
                    </div>
                  </div>
                  <span class="badge" [ngClass]="statusClass(app.status)">{{ formatStatus(app.status) }}</span>
                </div>
              }
            </div>
          }
        }

      </div>
    </div>

    <!-- Skeleton template -->
    <ng-template #skeletonList>
      <div class="space-y-3 animate-pulse">
        @for (sk of [1,2,3]; track sk) {
          <div class="bg-white rounded-xl border border-gray-200 p-5 flex items-center gap-4">
            <div class="w-11 h-11 rounded-xl bg-gray-100 flex-shrink-0"></div>
            <div class="flex-1 space-y-2">
              <div class="h-4 bg-gray-100 rounded w-48"></div>
              <div class="h-3 bg-gray-100 rounded w-32"></div>
            </div>
            <div class="h-6 bg-gray-100 rounded-full w-20"></div>
          </div>
        }
      </div>
    </ng-template>

    <!-- Empty state template -->
    <ng-template #emptyState let-icon="icon" let-title="title" let-desc="desc" let-link="link" let-linkLabel="linkLabel">
      <div class="bg-white rounded-2xl border border-gray-200 shadow-sm text-center py-20">
        <div class="w-14 h-14 bg-gray-50 rounded-full flex items-center justify-center mx-auto mb-4 border border-gray-100 text-gray-300">
          <lucide-icon [name]="icon" class="w-7 h-7"></lucide-icon>
        </div>
        <h3 class="text-base font-semibold text-gray-900 mb-1">{{ title }}</h3>
        <p class="text-gray-500 text-sm mb-6">{{ desc }}</p>
        @if (link) {
          <a [routerLink]="link" class="btn-primary py-2 px-6 text-sm">{{ linkLabel }}</a>
        }
      </div>
    </ng-template>
  `
})
export class MyApplicationsComponent implements OnInit {
  private api   = inject(ApiService);
  private auth  = inject(AuthService);
  private toast = inject(ToastService);

  activeTab       = signal<Tab>('applied');
  appsLoading     = signal(false);
  savedLoading    = signal(false);
  interviewsLoading = signal(false);

  allApplications = signal<EnrichedApplication[]>([]);
  savedJobs       = signal<Job[]>([]);
  interviews      = signal<Interview[]>([]);

  readonly activeApplications = computed(() =>
    this.allApplications().filter(a => !ARCHIVED_STATUSES.has(a.status))
  );
  readonly archivedApplications = computed(() =>
    this.allApplications().filter(a => ARCHIVED_STATUSES.has(a.status))
  );

  readonly tabs = [
    { key: 'saved'      as Tab, label: 'Saved',      icon: 'bookmark'   },
    { key: 'applied'    as Tab, label: 'Applied',     icon: 'file-text'  },
    { key: 'interviews' as Tab, label: 'Interviews',  icon: 'calendar'   },
    { key: 'archived'   as Tab, label: 'Archived',    icon: 'archive'    },
  ];

  tabCount(tab: Tab): number {
    switch (tab) {
      case 'saved':       return this.savedJobs().length;
      case 'applied':     return this.activeApplications().length;
      case 'interviews':  return this.interviews().length;
      case 'archived':    return this.archivedApplications().length;
    }
  }

  ngOnInit(): void {
    const user = this.auth.getCurrentUser();
    if (!user) return;
    this.loadApplications(user.userId);
    this.loadSaved();
    this.loadInterviews();
  }

  setTab(tab: Tab): void { this.activeTab.set(tab); }

  private loadApplications(userId: string): void {
    this.appsLoading.set(true);
    this.api.getMyApplications(userId).subscribe({
      next: (res) => {
        const apps = (res.data ?? []).map(a => this.normalizeApp(a));
        this.allApplications.set(apps.map(a => ({ ...a, company: '' })));
        this.appsLoading.set(false);
        if (!apps.length) return;
        forkJoin(apps.map(app =>
          this.api.getJobById(app.jobId).pipe(
            map(r => ({ ...app, company: r.data?.company ?? '' } as EnrichedApplication)),
            catchError(() => of({ ...app, company: '' } as EnrichedApplication))
          )
        )).subscribe({ next: enriched => this.allApplications.set(enriched) });
      },
      error: () => { this.appsLoading.set(false); this.toast.error('Failed to load applications'); }
    });
  }

  private loadSaved(): void {
    this.savedLoading.set(true);
    this.api.getBookmarks().subscribe({
      next: (res: any) => {
        const bookmarks = Array.isArray(res) ? res : (res.data || []);
        if (!bookmarks.length) { this.savedJobs.set([]); this.savedLoading.set(false); return; }
        forkJoin(bookmarks.map((b: any) =>
          this.api.getJobById(b.jobId || b.id).pipe(map(r => r.data as Job), catchError(() => of(null)))
        )).subscribe({
          next: (jobs: any) => { this.savedJobs.set(jobs.filter(Boolean)); this.savedLoading.set(false); },
          error: () => { this.savedJobs.set([]); this.savedLoading.set(false); }
        });
      },
      error: () => { this.savedJobs.set([]); this.savedLoading.set(false); }
    });
  }

  private loadInterviews(): void {
    this.interviewsLoading.set(true);
    this.api.getMyInterviews().subscribe({
      next: (res: any) => {
        const list = Array.isArray(res) ? res : (res?.data || []);
        this.interviews.set(list as Interview[]);
        this.interviewsLoading.set(false);
      },
      error: () => { this.interviews.set([]); this.interviewsLoading.set(false); }
    });
  }

  removeBookmark(jobId: string): void {
    this.api.removeBookmark(jobId).subscribe({
      next: () => { this.savedJobs.update(jobs => jobs.filter(j => (j.jobId || (j as any).id) !== jobId)); this.toast.success('Removed from saved jobs'); },
      error: () => this.toast.error('Failed to remove bookmark')
    });
  }

  respondToOffer(applicationId: string, accepted: boolean): void {
    this.api.respondToOffer(applicationId, accepted).subscribe({
      next: () => {
        this.allApplications.update(apps => apps.map(a =>
          a.id === applicationId ? { ...a, status: accepted ? 'OFFER_ACCEPTED' : 'OFFER_REJECTED' } : a
        ));
        this.toast.success(accepted ? 'Offer accepted!' : 'Offer declined');
      },
      error: () => this.toast.error('Failed to submit response')
    });
  }

  private normalizeApp(app: Application): Application {
    const norm = (v: any): any => {
      if (!v) return v;
      if (Array.isArray(v) && v.length >= 3) {
        const [y, m, d, h = 0, min = 0, s = 0] = v;
        return new Date(y, m - 1, d, h, min, s).toISOString();
      }
      return v;
    };
    return { ...app, appliedAt: norm((app as any).appliedAt), updatedAt: norm((app as any).updatedAt), profileViewedAt: norm((app as any).profileViewedAt) };
  }

  formatStatus(s: string): string { return s.replace(/_/g, ' '); }

  statusClass(status: string): string {
    switch (status) {
      case 'APPLIED': case 'SHORTLISTED': case 'INTERVIEW_SCHEDULED': return 'badge-pending';
      case 'HIRED': case 'OFFER_ACCEPTED': return 'badge-accepted';
      case 'REJECTED': case 'OFFER_REJECTED': return 'badge-rejected';
      default: return 'bg-gray-100 text-gray-700';
    }
  }

  latestActionIcon(status: string): string {
    switch (status) {
      case 'SHORTLISTED': return 'star';
      case 'INTERVIEW_SCHEDULED': return 'calendar';
      case 'HIRED': case 'OFFER_ACCEPTED': return 'check-circle';
      case 'REJECTED': case 'OFFER_REJECTED': return 'x-circle';
      default: return 'clock';
    }
  }

  latestActionTextClass(status: string): string {
    switch (status) {
      case 'SHORTLISTED': return 'text-yellow-600';
      case 'INTERVIEW_SCHEDULED': return 'text-blue-600';
      case 'HIRED': case 'OFFER_ACCEPTED': return 'text-green-600';
      case 'REJECTED': case 'OFFER_REJECTED': return 'text-red-500';
      default: return 'text-gray-400';
    }
  }

  latestActionLabel(status: string): string {
    switch (status) {
      case 'SHORTLISTED': return 'Shortlisted';
      case 'INTERVIEW_SCHEDULED': return 'Interview Scheduled';
      case 'HIRED': return 'Offer Extended';
      case 'OFFER_ACCEPTED': return 'Offer Accepted';
      case 'OFFER_REJECTED': return 'Offer Declined';
      case 'REJECTED': return 'Not Selected';
      default: return status.replace(/_/g, ' ');
    }
  }
}
