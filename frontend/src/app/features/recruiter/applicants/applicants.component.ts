import { Component, OnInit, inject, signal, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { ApiService } from '../../../core/services/api.service';
import { ToastService } from '../../../core/services/toast.service';
import { Application } from '../../../shared/models/models';
import { LucideAngularModule } from 'lucide-angular';
import { FormsModule } from '@angular/forms';

interface CandidateProfile {
  userId: string;
  name: string;
  email: string;
  phone?: string;
  location?: string;
  experienceLevel?: string;
  education?: string;
  preferredJobTypes?: string;
}

@Component({
  selector: 'app-applicants',
  standalone: true,
  imports: [CommonModule, RouterLink, LucideAngularModule, FormsModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="min-h-screen bg-gray-50 flex flex-col pt-10 px-6 relative">
      <div class="max-w-4xl mx-auto w-full">
        <a routerLink="/recruiter/dashboard" class="inline-flex items-center gap-1 text-sm font-medium text-gray-500 hover:text-primary transition-colors mb-6">
          <lucide-icon name="arrow-left" class="w-4 h-4"></lucide-icon> Back to Dashboard
        </a>

        <div class="mb-8">
          <h1 class="text-2xl font-bold text-gray-900 tracking-tight">Applicants</h1>
          <p class="text-gray-500 text-sm mt-1">Review and manage candidates who applied for this role.</p>
        </div>

        @if (loading()) {
          <div class="bg-white rounded-2xl border border-gray-200 shadow-sm p-6 space-y-4 animate-pulse">
            @for (sk of [1,2,3]; track sk) {
              <div class="border border-gray-200 rounded-xl p-5 flex items-center justify-between gap-4">
                <div class="flex items-center gap-4">
                  <div class="w-12 h-12 rounded-full bg-gray-100"></div>
                  <div class="space-y-2">
                    <div class="h-4 bg-gray-100 rounded w-52"></div>
                    <div class="h-3 bg-gray-100 rounded w-40"></div>
                  </div>
                </div>
                <div class="h-8 bg-gray-100 rounded w-32"></div>
              </div>
            }
          </div>
        } @else if (loadError()) {
          <div class="bg-white rounded-2xl border border-red-100 shadow-sm text-center py-16">
            <lucide-icon name="alert-circle" class="w-10 h-10 text-red-400 mx-auto mb-3"></lucide-icon>
            <h3 class="text-lg font-semibold text-gray-900 mb-1">Failed to load applicants</h3>
            <p class="text-gray-500 text-sm mb-6">The application service may be unavailable.</p>
            <button (click)="loadApplicants()" class="btn-primary py-2 px-6">Retry</button>
          </div>
        } @else if (applications().length === 0) {
          <div class="bg-white rounded-2xl border border-gray-200 shadow-sm text-center py-20">
            <lucide-icon name="users" class="w-10 h-10 text-gray-300 mx-auto mb-3"></lucide-icon>
            <h3 class="text-lg font-semibold text-gray-900 mb-1">No applicants yet</h3>
            <p class="text-gray-500 text-sm">Candidates who apply to your job will appear here.</p>
          </div>
        } @else {
          <div class="grid grid-cols-1 gap-4">
            @for (app of applications(); track app.id) {
              <div class="bg-white rounded-xl p-5 border border-gray-200 shadow-sm flex flex-col sm:flex-row sm:items-center justify-between gap-4 transition-shadow hover:shadow-md">
                <div class="flex items-center gap-4">
                  <div class="w-12 h-12 rounded-full bg-indigo-50 flex items-center justify-center text-xl font-bold text-indigo-400 flex-shrink-0">
                    {{ app.candidateEmail.charAt(0).toUpperCase() }}
                  </div>
                  <div>
                    <h3 class="font-semibold text-gray-900">{{ app.candidateEmail }}</h3>
                    <div class="flex flex-wrap items-center gap-2 text-xs text-gray-500 mt-2">
                      <span class="flex items-center gap-1 bg-gray-50 px-2 py-1 rounded">
                        <lucide-icon name="clock" class="w-3.5 h-3.5 text-gray-400"></lucide-icon>
                        Applied {{ app.appliedAt | date:'shortDate' }}
                      </span>
                      @if (app.profileViewedAt) {
                        <span class="flex items-center gap-1 bg-indigo-50 px-2 py-1 rounded font-semibold text-indigo-600">
                          <lucide-icon name="eye" class="w-3.5 h-3.5"></lucide-icon>
                          Viewed {{ app.profileViewedAt | date:'shortDate' }}
                        </span>
                      } @else {
                        <span class="flex items-center gap-1 bg-gray-50 px-2 py-1 rounded text-gray-400">
                          <lucide-icon name="eye-off" class="w-3.5 h-3.5"></lucide-icon>
                          Not viewed
                        </span>
                      }
                      <!-- View Profile button -->
                      <button type="button" (click)="openProfile(app)"
                        class="flex items-center gap-1 font-bold text-indigo-600 hover:text-indigo-700 bg-indigo-50 hover:bg-indigo-100 px-2 py-1 rounded transition-colors border-0">
                        <lucide-icon name="user" class="w-3 h-3"></lucide-icon> View Profile
                      </button>
                      @if (app.resumeId) {
                        <button type="button" (click)="openResume(app.resumeId, app.id)"
                          class="flex items-center gap-1 font-bold text-primary hover:text-primary-hover bg-primary/5 hover:bg-primary/10 px-2 py-1 rounded transition-colors border-0">
                          <lucide-icon name="paperclip" class="w-3 h-3"></lucide-icon> Resume
                        </button>
                      } @else {
                        <span class="text-gray-400 italic">No resume</span>
                      }
                    </div>
                  </div>
                </div>

                <div class="flex items-center gap-3 w-full sm:w-auto">
                  <span class="badge" [ngClass]="statusClass(app.status)">{{ formatStatus(app.status) }}</span>
                  <select (change)="updateStatus(app.id, $any($event.target).value, $any($event.target))" [value]="app.status"
                    class="form-input text-sm py-1.5 px-3 min-w-[140px] bg-gray-50 border-gray-200">
                    <option value="" disabled [selected]="true">Move to…</option>
                    @for (s of allowedStatuses(app.status); track s.value) {
                      <option [value]="s.value">{{ s.label }}</option>
                    }
                  </select>
                </div>
              </div>
            }
          </div>
        }
      </div>

      <!-- Candidate Profile Slide-over -->
      @if (profilePanelOpen()) {
        <!-- Backdrop -->
        <div class="fixed inset-0 bg-black/30 backdrop-blur-sm z-40" (click)="closeProfile()"></div>

        <!-- Panel -->
        <div class="fixed top-0 right-0 h-full w-full max-w-md bg-white shadow-2xl z-50 flex flex-col overflow-y-auto">

          <!-- Panel Header -->
          <div class="flex items-center justify-between px-6 py-5 border-b border-gray-100 sticky top-0 bg-white z-10">
            <h2 class="text-lg font-bold text-gray-900 flex items-center gap-2">
              <lucide-icon name="user" class="w-5 h-5 text-indigo-500"></lucide-icon>
              Candidate Profile
            </h2>
            <button (click)="closeProfile()" class="text-gray-400 hover:text-gray-600 transition-colors">
              <lucide-icon name="x" class="w-5 h-5"></lucide-icon>
            </button>
          </div>

          @if (profileLoading()) {
            <div class="flex-1 flex items-center justify-center">
              <lucide-icon name="loader-2" class="w-8 h-8 text-indigo-400 animate-spin"></lucide-icon>
            </div>
          } @else if (selectedProfile()) {
            <div class="flex-1 px-6 py-6 space-y-6">

              <!-- Avatar + Name -->
              <div class="flex items-center gap-4">
                <div class="w-16 h-16 rounded-2xl bg-indigo-100 flex items-center justify-center text-2xl font-bold text-indigo-500 flex-shrink-0">
                  {{ selectedProfile()!.name.charAt(0).toUpperCase() }}
                </div>
                <div>
                  <h3 class="text-xl font-bold text-gray-900">{{ selectedProfile()!.name }}</h3>
                  <p class="text-sm text-gray-500">{{ selectedProfile()!.email }}</p>
                </div>
              </div>

              <!-- Info Grid -->
              <div class="grid grid-cols-2 gap-4">
                @if (selectedProfile()!.phone) {
                  <div class="bg-gray-50 rounded-xl p-4">
                    <p class="text-xs font-semibold text-gray-400 uppercase tracking-wide mb-1">Phone</p>
                    <p class="text-sm font-semibold text-gray-800">{{ selectedProfile()!.phone }}</p>
                  </div>
                }
                @if (selectedProfile()!.location) {
                  <div class="bg-gray-50 rounded-xl p-4">
                    <p class="text-xs font-semibold text-gray-400 uppercase tracking-wide mb-1">Location</p>
                    <p class="text-sm font-semibold text-gray-800">{{ selectedProfile()!.location }}</p>
                  </div>
                }
                @if (selectedProfile()!.experienceLevel) {
                  <div class="bg-gray-50 rounded-xl p-4">
                    <p class="text-xs font-semibold text-gray-400 uppercase tracking-wide mb-1">Experience</p>
                    <p class="text-sm font-semibold text-gray-800">{{ selectedProfile()!.experienceLevel }}</p>
                  </div>
                }
                @if (selectedProfile()!.preferredJobTypes) {
                  <div class="bg-gray-50 rounded-xl p-4">
                    <p class="text-xs font-semibold text-gray-400 uppercase tracking-wide mb-1">Job Type</p>
                    <p class="text-sm font-semibold text-gray-800">{{ selectedProfile()!.preferredJobTypes }}</p>
                  </div>
                }
              </div>

              @if (selectedProfile()!.education) {
                <div class="bg-gray-50 rounded-xl p-4">
                  <p class="text-xs font-semibold text-gray-400 uppercase tracking-wide mb-1">Education</p>
                  <p class="text-sm text-gray-800">{{ selectedProfile()!.education }}</p>
                </div>
              }

              <!-- No extra info fallback -->
              @if (!selectedProfile()!.phone && !selectedProfile()!.location && !selectedProfile()!.experienceLevel && !selectedProfile()!.education) {
                <div class="text-center py-6 text-sm text-gray-400">
                  <lucide-icon name="info" class="w-6 h-6 mx-auto mb-2 text-gray-300"></lucide-icon>
                  Candidate hasn't filled in profile details yet.
                </div>
              }

              <!-- Resume button -->
              @if (selectedAppForProfile()?.resumeId) {
                <button type="button" (click)="openResume(selectedAppForProfile()!.resumeId, selectedAppForProfile()!.id)"
                  class="w-full flex items-center justify-center gap-2 py-3 rounded-xl bg-primary text-white font-semibold hover:bg-primary-hover transition-colors">
                  <lucide-icon name="download" class="w-4 h-4"></lucide-icon>
                  Download Resume
                </button>
              }

              <!-- Quick status update -->
              <div>
                <p class="text-xs font-semibold text-gray-400 uppercase tracking-wide mb-2">Update Status</p>
                @if (allowedStatuses(selectedAppForProfile()!.status).length === 0) {
                  <p class="text-xs text-gray-400 italic">No further actions available.</p>
                } @else {
                  <div class="flex flex-wrap gap-2">
                    @for (s of allowedStatuses(selectedAppForProfile()!.status); track s.value) {
                      <button type="button"
                        (click)="updateStatus(selectedAppForProfile()!.id, s.value)"
                        [class]="'px-3 py-1.5 rounded-lg text-xs font-semibold border transition-colors ' + s.cls">
                        {{ s.label }}
                      </button>
                    }
                  </div>
                }
              </div>

            </div>
          }
        </div>
      }

      <!-- Interview Scheduling Modal -->
      @if (showInterviewModal()) {
        <div class="fixed inset-0 bg-black/40 backdrop-blur-sm z-50 flex items-center justify-center animate-fade-in p-4">
          <div class="bg-white rounded-3xl w-full max-w-md shadow-[0_10px_40px_-10px_rgba(0,0,0,0.3)] p-8">
            <div class="flex items-center justify-between mb-6">
              <h2 class="text-xl font-bold text-gray-900 flex items-center gap-2">
                <lucide-icon name="calendar" class="w-5 h-5 text-primary"></lucide-icon> Schedule Interview
              </h2>
              <button (click)="closeModal()" class="text-gray-400 hover:text-gray-600 transition-colors">
                <lucide-icon name="x" class="w-5 h-5"></lucide-icon>
              </button>
            </div>
            <div class="space-y-4">
              <div>
                <label class="block text-sm font-semibold text-gray-700 mb-1">Interview Date & Time</label>
                <input type="datetime-local" [(ngModel)]="interviewData.scheduledAt" class="form-input bg-gray-50 border-gray-200" />
              </div>
              <div>
                <label class="block text-sm font-semibold text-gray-700 mb-1">Meeting Link (Zoom / Meet)</label>
                <input type="url" [(ngModel)]="interviewData.meetingLink" placeholder="https://" class="form-input bg-gray-50 border-gray-200" />
              </div>
              <div class="pt-4 flex gap-3">
                <button (click)="closeModal()" class="flex-1 px-4 py-2.5 rounded-xl border border-gray-200 text-gray-600 font-semibold hover:bg-gray-50 transition-colors">Cancel</button>
                <button (click)="confirmInterview()" class="flex-1 btn-primary py-2.5 px-4 !rounded-xl">Confirm & Notify</button>
              </div>
            </div>
          </div>
        </div>
      }
    </div>
  `
})
export class ApplicantsComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private api   = inject(ApiService);
  private toast = inject(ToastService);

  applications        = signal<Application[]>([]);
  loading             = signal(false);
  loadError           = signal(false);
  profilePanelOpen    = signal(false);
  profileLoading      = signal(false);
  selectedProfile     = signal<CandidateProfile | null>(null);
  selectedAppForProfile = signal<Application | null>(null);
  showInterviewModal  = signal(false);

  private jobId = '';
  selectedAppId: string | null = null;
  interviewData = { scheduledAt: '', meetingLink: '', applicationId: '', candidateId: '' };

  private static readonly TRANSITIONS: Record<string, { value: string; label: string; cls: string }[]> = {
    APPLIED:              [
      { value: 'SHORTLISTED', label: 'Shortlist', cls: 'bg-yellow-50 text-yellow-700 border-yellow-200 hover:bg-yellow-100' },
      { value: 'REJECTED',    label: 'Reject',    cls: 'bg-red-50 text-red-700 border-red-200 hover:bg-red-100' },
    ],
    SHORTLISTED:          [
      { value: 'INTERVIEW_SCHEDULED', label: 'Schedule Interview', cls: 'bg-blue-50 text-blue-700 border-blue-200 hover:bg-blue-100' },
      { value: 'REJECTED',            label: 'Reject',             cls: 'bg-red-50 text-red-700 border-red-200 hover:bg-red-100' },
    ],
    INTERVIEW_SCHEDULED:  [
      { value: 'HIRED',        label: 'Hire',            cls: 'bg-green-50 text-green-700 border-green-200 hover:bg-green-100' },
      { value: 'REJECTED',     label: 'Reject',          cls: 'bg-red-50 text-red-700 border-red-200 hover:bg-red-100' },
      { value: 'SHORTLISTED',  label: 'Cancel Interview', cls: 'bg-yellow-50 text-yellow-700 border-yellow-200 hover:bg-yellow-100' },
    ],
  };

  allowedStatuses(currentStatus: string): { value: string; label: string; cls: string }[] {
    return ApplicantsComponent.TRANSITIONS[currentStatus] ?? [];
  }

  ngOnInit(): void {
    this.jobId = this.route.snapshot.paramMap.get('jobId') || '';
    this.loadApplicants();
  }

  loadApplicants(): void {
    if (!this.jobId) { this.toast.error('Invalid job ID'); return; }
    this.loading.set(true);
    this.loadError.set(false);
    this.api.getJobApplications(this.jobId).subscribe({
      next: (res) => {
        const raw = res.data ?? (Array.isArray(res as any) ? res as any : []);
        this.applications.set(raw.map((a: Application) => this.normalizeAppliedAt(a)));
        this.loading.set(false);
      },
      error: () => { this.loading.set(false); this.loadError.set(true); this.toast.error('Failed to load applicants'); }
    });
  }

  openProfile(app: Application): void {
    this.selectedAppForProfile.set(app);
    this.profilePanelOpen.set(true);
    this.profileLoading.set(true);
    this.selectedProfile.set(null);

    // Mark profile viewed + fetch user profile in parallel
    this.api.markProfileViewed(app.id).subscribe({
      next: (res: any) => this.mergeUpdatedApplication(res?.data ?? res),
      error: () => {}
    });
    this.api.getUserById((app as any).candidateId).subscribe({
      next: (res: any) => {
        const data = res?.data ?? res;
        this.selectedProfile.set(data as CandidateProfile);
        this.profileLoading.set(false);
      },
      error: () => {
        this.selectedProfile.set({ userId: (app as any).candidateId, name: app.candidateEmail.split('@')[0], email: app.candidateEmail });
        this.profileLoading.set(false);
      }
    });
  }

  closeProfile(): void {
    this.profilePanelOpen.set(false);
    this.selectedProfile.set(null);
    this.selectedAppForProfile.set(null);
  }

  updateStatus(appId: string, status: string, selectEl?: HTMLSelectElement): void {
    if (!status) return;
    if (status === 'INTERVIEW_SCHEDULED') {
      this.selectedAppId = appId;
      const app = this.applications().find(a => a.id === appId);
      this.interviewData.applicationId = appId;
      this.interviewData.candidateId = (app as any)?.candidateId || '';
      this.showInterviewModal.set(true);
      if (selectEl) selectEl.value = app?.status ?? '';
      return;
    }
    this.executeStatusUpdate(appId, status, selectEl);
  }

  closeModal(): void {
    this.showInterviewModal.set(false);
    this.selectedAppId = null;
    this.interviewData = { scheduledAt: '', meetingLink: '', applicationId: '', candidateId: '' };
  }

  confirmInterview(): void {
    if (!this.selectedAppId || !this.interviewData.scheduledAt) { this.toast.error('Please provide interview date and time'); return; }
    if (!this.interviewData.candidateId) { this.toast.error('Missing candidate ID'); return; }
    if (this.interviewData.meetingLink && !this.interviewData.meetingLink.startsWith('http')) { this.toast.error('Meeting link must start with http://'); return; }
    this.api.scheduleInterview(this.interviewData).subscribe({
      next: () => { this.executeStatusUpdate(this.selectedAppId!, 'INTERVIEW_SCHEDULED'); this.toast.success('Interview scheduled!'); this.closeModal(); },
      error: (err) => this.toast.error(err?.error?.message || 'Failed to schedule interview')
    });
  }

  private executeStatusUpdate(appId: string, status: string, selectEl?: HTMLSelectElement): void {
    this.api.updateApplicationStatus(appId, status).subscribe({
      next: () => {
        this.applications.update(apps => apps.map(a => a.id === appId ? { ...a, status: status as any } : a));
        if (this.selectedAppForProfile()?.id === appId)
          this.selectedAppForProfile.update(a => a ? { ...a, status: status as any } : a);
        this.toast.success('Status updated');
      },
      error: (err) => {
        if (selectEl) selectEl.value = this.applications().find(a => a.id === appId)?.status ?? '';
        this.toast.error(err?.error?.message || 'Failed to update status');
      }
    });
  }

  openResume(resumeId: string, appId: string): void {
    this.api.markProfileViewed(appId).subscribe({
      next: (res: any) => this.mergeUpdatedApplication(res?.data ?? res),
      error: () => {}
    });
    this.api.downloadResume(resumeId).subscribe({
      next: (blob: Blob) => {
        const url = URL.createObjectURL(blob);
        window.open(url, '_blank', 'noopener,noreferrer');
        setTimeout(() => URL.revokeObjectURL(url), 60000);
      },
      error: () => this.toast.error('Unable to open resume')
    });
  }

  private mergeUpdatedApplication(app: Application): void {
    const updated = this.normalizeApplicationDates(app);
    if (!updated?.id) return;
    this.applications.update(apps => apps.map(a => a.id === updated.id ? { ...a, ...updated } : a));
    if (this.selectedAppForProfile()?.id === updated.id) {
      this.selectedAppForProfile.update(current => current ? { ...current, ...updated } : current);
    }
  }

  private normalizeAppliedAt(app: Application): Application {
    return this.normalizeApplicationDates(app);
  }

  private normalizeApplicationDates(app: Application): Application {
    const normalizeDate = (v: any): any => {
      if (!v) return v;
      if (Array.isArray(v) && v.length >= 3) {
        const [y, m, d, h = 0, min = 0, s = 0] = v;
        return new Date(y, m - 1, d, h, min, s).toISOString();
      }
      return v;
    };
    return {
      ...app,
      appliedAt: normalizeDate((app as any).appliedAt),
      updatedAt: normalizeDate((app as any).updatedAt),
      profileViewedAt: normalizeDate((app as any).profileViewedAt)
    };
  }

  formatStatus(status: string): string { return status.replace(/_/g, ' '); }

  statusClass(status: string): string {
    switch (status) {
      case 'APPLIED': case 'SHORTLISTED': case 'INTERVIEW_SCHEDULED': return 'badge-pending';
      case 'HIRED': return 'badge-accepted';
      case 'REJECTED': return 'badge-rejected';
      default: return 'bg-gray-100 text-gray-700';
    }
  }
}
