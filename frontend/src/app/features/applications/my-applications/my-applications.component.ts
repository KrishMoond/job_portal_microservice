import { Component, OnInit, inject, signal, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ApiService } from '../../../core/services/api.service';
import { AuthService } from '../../../core/services/auth.service';
import { ToastService } from '../../../core/services/toast.service';
import { Application, Interview } from '../../../shared/models/models';
import { LucideAngularModule } from 'lucide-angular';
import { RouterLink } from '@angular/router';
import { forkJoin, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';

interface EnrichedApplication extends Application { company: string; }

@Component({
  selector: 'app-my-applications',
  standalone: true,
  imports: [CommonModule, LucideAngularModule, RouterLink],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="min-h-screen bg-gray-50 flex flex-col pt-6">
      <div class="max-w-7xl mx-auto px-6 w-full flex-grow grid grid-cols-1 md:grid-cols-[240px_1fr] gap-8">

        <!-- Sidebar -->
        <aside class="space-y-1">
          <h3 class="text-xs font-bold text-gray-400 uppercase tracking-wider mb-3 px-3">Dashboard</h3>
          <a href="#" class="flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium bg-primary-light text-primary relative">
            <lucide-icon name="file-text" class="w-4 h-4"></lucide-icon>
            My Applications
            <span class="absolute left-0 top-1/2 -translate-y-1/2 w-1 h-5 bg-primary rounded-r-md"></span>
          </a>
          <a routerLink="/seeker/bookmarks" class="flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium text-gray-600 hover:bg-gray-50 hover:text-gray-900 transition-colors">
            <lucide-icon name="bookmark" class="w-4 h-4"></lucide-icon>
            Saved Jobs
          </a>
          <a routerLink="/seeker/profile" class="flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium text-gray-600 hover:bg-gray-50 hover:text-gray-900 transition-colors">
            <lucide-icon name="user" class="w-4 h-4"></lucide-icon>
            Resume / Profile
          </a>
        </aside>

        <!-- Main Content -->
        <main>
          <div class="flex items-center justify-between mb-8">
            <div>
              <h1 class="text-2xl font-bold text-gray-900">My Applications</h1>
              <p class="text-sm text-gray-500 mt-1">Track and manage your submitted job applications.</p>
            </div>
            <span class="text-sm text-gray-400">{{ applications().length }} total</span>
          </div>

          <!-- Interviews -->
          <div class="bg-white rounded-2xl border border-gray-200 overflow-hidden shadow-sm mb-6">
            <div class="px-6 py-4 border-b border-gray-200 flex items-center justify-between">
              <div>
                <h2 class="text-sm font-bold text-gray-900">My Interviews</h2>
                <p class="text-xs text-gray-500 mt-0.5">Upcoming interviews scheduled by recruiters.</p>
              </div>
              @if (interviewsLoading()) {
                <span class="text-xs text-gray-400">Loading...</span>
              } @else {
                <span class="text-xs text-gray-400">{{ interviews().length }} total</span>
              }
            </div>

            @if (interviewsLoading()) {
              <div class="p-6 space-y-3 animate-pulse">
                @for (sk of [1,2]; track sk) {
                  <div class="h-12 bg-gray-100 rounded-xl"></div>
                }
              </div>
            } @else if (interviews().length === 0) {
              <div class="p-6 text-sm text-gray-500">No interviews scheduled yet.</div>
            } @else {
              <div class="divide-y divide-gray-100">
                @for (iv of interviews(); track iv.id) {
                  <div class="px-6 py-4 flex flex-col sm:flex-row sm:items-center justify-between gap-3">
                    <div class="flex items-start gap-3">
                      <div class="w-10 h-10 rounded-xl bg-primary/10 text-primary flex items-center justify-center border border-primary/10">
                        <lucide-icon name="calendar" class="w-5 h-5"></lucide-icon>
                      </div>
                      <div>
                        <p class="text-sm font-semibold text-gray-900">{{ iv.status || 'SCHEDULED' }}</p>
                        <p class="text-xs text-gray-500 mt-1">
                          Scheduled for <span class="font-semibold text-gray-700">{{ iv.scheduledAt | date:'medium' }}</span>
                        </p>
                        @if (iv.meetingLink) {
                          <a [href]="iv.meetingLink" target="_blank" rel="noopener noreferrer" class="text-xs font-bold text-primary hover:underline mt-1 inline-block">
                            Join meeting
                          </a>
                        }
                      </div>
                    </div>
                    <div class="text-xs text-gray-500 sm:text-right">
                      {{ iv.status || 'SCHEDULED' }}
                    </div>
                  </div>
                }
              </div>
            }
          </div>

          <div class="bg-white rounded-2xl border border-gray-200 overflow-hidden shadow-sm">
            @if (loading()) {
              <div class="overflow-hidden">
                <table class="w-full text-left text-sm whitespace-nowrap">
                  <thead class="bg-gray-50 border-b border-gray-100">
                    <tr>
                      <th class="px-6 py-4"><div class="h-4 bg-gray-300 rounded w-20 animate-pulse"></div></th>
                      <th class="px-6 py-4"><div class="h-4 bg-gray-300 rounded w-24 animate-pulse"></div></th>
                      <th class="px-6 py-4"><div class="h-4 bg-gray-300 rounded w-24 animate-pulse"></div></th>
                      <th class="px-6 py-4"><div class="h-4 bg-gray-300 rounded w-16 animate-pulse"></div></th>
                      <th class="px-6 py-4 text-right"><div class="h-4 bg-gray-300 rounded w-10 ml-auto animate-pulse"></div></th>
                    </tr>
                  </thead>
                  <tbody class="divide-y divide-gray-100">
                    @for (sk of [1,2,3,4,5]; track sk) {
                      <tr class="animate-pulse">
                        <td class="px-6 py-4">
                          <div class="flex items-center gap-3">
                            <div class="w-10 h-10 rounded-lg bg-gray-200"></div>
                            <div class="h-4 bg-gray-200 rounded w-24"></div>
                          </div>
                        </td>
                        <td class="px-6 py-4"><div class="h-4 bg-gray-200 rounded w-32"></div></td>
                        <td class="px-6 py-4"><div class="h-4 bg-gray-200 rounded w-24"></div></td>
                        <td class="px-6 py-4"><div class="h-6 bg-gray-200 rounded-full w-20"></div></td>
                        <td class="px-6 py-4 text-right"><div class="h-4 bg-gray-200 rounded w-6 ml-auto"></div></td>
                      </tr>
                    }
                  </tbody>
                </table>
              </div>
            } @else if (applications().length === 0) {
              <div class="text-center py-20">
                <div class="w-16 h-16 bg-gray-50 rounded-full flex items-center justify-center mx-auto mb-4 border border-gray-100 text-gray-300">
                  <lucide-icon name="file-x-2" class="w-8 h-8"></lucide-icon>
                </div>
                <h3 class="text-lg font-semibold text-gray-900 mb-1">No applications yet</h3>
                <p class="text-gray-500 text-sm mb-6">You haven't applied to any jobs. Start exploring opportunities!</p>
                <a routerLink="/jobs" class="btn-primary py-2 px-6">Find Jobs</a>
              </div>
            } @else {
              <div class="overflow-x-auto">
                <table class="w-full text-left text-sm whitespace-nowrap">
                  <thead class="bg-gray-50 text-gray-500 font-medium border-b border-gray-200">
                    <tr>
                      <th class="px-6 py-4">Company</th>
                      <th class="px-6 py-4">Role</th>
                      <th class="px-6 py-4">Date Applied</th>
                      <th class="px-6 py-4">Status</th>
                      <th class="px-6 py-4 text-right">Action</th>
                    </tr>
                  </thead>
                  <tbody class="divide-y divide-gray-100">
                    @for (app of applications(); track app.id) {
                      <tr class="hover:bg-gray-50/50 transition-colors group">
                        <td class="px-6 py-4">
                          <div class="flex items-center gap-3">
                            <div class="w-10 h-10 rounded-lg border border-gray-100 bg-white shadow-sm flex items-center justify-center font-bold text-gray-400 text-sm">
                              {{ (app.company || app.jobTitle).substring(0,2).toUpperCase() }}
                            </div>
                            <span class="font-semibold text-gray-900">{{ app.company || '—' }}</span>
                          </div>
                        </td>
                        <td class="px-6 py-4">
                          <a [routerLink]="['/jobs', app.jobId]" class="font-medium text-gray-900 hover:text-primary transition-colors">{{ app.jobTitle }}</a>
                        </td>
                        <td class="px-6 py-4 text-gray-500">{{ app.appliedAt | date:'mediumDate' }}</td>
                        <td class="px-6 py-4">
                          <span class="badge" [ngClass]="statusClass(app.status)">{{ formatStatus(app.status) }}</span>
                        </td>
                        <td class="px-6 py-4 text-right">
                          <div class="flex justify-end items-center gap-2">
                            @if (app.status === 'HIRED') {
                              <button (click)="respondToOffer(app.id, true)" class="px-3 py-1.5 rounded-md text-xs font-semibold bg-green-50 text-green-700 border border-green-200 hover:bg-green-100 transition-colors">
                                Accept
                              </button>
                              <button (click)="respondToOffer(app.id, false)" class="px-3 py-1.5 rounded-md text-xs font-semibold bg-red-50 text-red-700 border border-red-200 hover:bg-red-100 transition-colors">
                                Reject
                              </button>
                            }
                            <a [routerLink]="['/jobs', app.jobId]"
                              class="p-2 text-gray-400 hover:text-primary hover:bg-primary-light rounded-md transition-colors opacity-0 group-hover:opacity-100 focus:opacity-100 outline-none inline-flex"
                              title="View Job">
                              <lucide-icon name="external-link" class="w-4 h-4"></lucide-icon>
                            </a>
                          </div>
                        </td>
                      </tr>
                    }
                  </tbody>
                </table>
              </div>
              <div class="px-6 py-4 border-t border-gray-200 text-sm text-gray-500">
                Showing {{ applications().length }} result{{ applications().length === 1 ? '' : 's' }}
              </div>
            }
          </div>
        </main>
      </div>
    </div>
  `
})
export class MyApplicationsComponent implements OnInit {
  private api   = inject(ApiService);
  private auth  = inject(AuthService);
  private toast = inject(ToastService);

  applications = signal<EnrichedApplication[]>([]);
  loading = signal(false);
  readonly interviews = signal<Interview[]>([]);
  readonly interviewsLoading = signal(false);

  ngOnInit(): void {
    const user = this.auth.getCurrentUser();
    if (!user) return;
    this.loading.set(true);

    this.loadInterviews();

    this.api.getMyApplications(user.userId).subscribe({
      next: (res) => {
        const apps = (res.data ?? []).map(a => this.normalizeApplication(a));
        this.applications.set(apps.map(a => ({ ...a, company: '' })));
        this.loading.set(false);

        if (apps.length === 0) return;
        const jobRequests = apps.map(app =>
          this.api.getJobById(app.jobId).pipe(
            map(r => ({ ...app, company: r.data?.company ?? '' } as EnrichedApplication)),
            catchError(() => of({ ...app, company: '' } as EnrichedApplication))
          )
        );

        forkJoin(jobRequests).subscribe({
          next: (enriched) => { this.applications.set(enriched); },
          error: () => { /* keep fallback list */ }
        });
      },
      error: () => {
        this.applications.set([]);
        this.loading.set(false);
        this.toast.error('Failed to load applications');
      }
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
      error: () => {
        this.interviews.set([]);
        this.interviewsLoading.set(false);
      }
    });
  }

  private normalizeApplication(app: Application): Application {
    const appliedAtAny = (app as any).appliedAt;
    if (Array.isArray(appliedAtAny) && appliedAtAny.length >= 3) {
      const [year, month, day, hour = 0, minute = 0, second = 0] = appliedAtAny;
      const normalized = new Date(year, month - 1, day, hour, minute, second).toISOString();
      return { ...app, appliedAt: normalized };
    }
    return app;
  }

  formatStatus(status: string): string { return status.replace(/_/g, ' '); }

  respondToOffer(applicationId: string, accepted: boolean): void {
    this.api.respondToOffer(applicationId, accepted).subscribe({
      next: () => {
        this.applications.update(apps => apps.map(app =>
          app.id === applicationId
            ? { ...app, status: accepted ? 'OFFER_ACCEPTED' : 'OFFER_REJECTED' }
            : app
        ));
        this.toast.success(accepted ? 'Offer accepted' : 'Offer rejected');
      },
      error: () => this.toast.error('Failed to submit offer response')
    });
  }

  statusClass(status: string): string {
    switch (status) {
      case 'APPLIED':
      case 'SHORTLISTED':
      case 'INTERVIEW_SCHEDULED': return 'badge-pending';
      case 'HIRED':               return 'badge-accepted';
      case 'OFFER_ACCEPTED':      return 'badge-accepted';
      case 'REJECTED':            return 'badge-rejected';
      case 'OFFER_REJECTED':      return 'badge-rejected';
      default:                    return 'bg-gray-100 text-gray-700';
    }
  }
}
