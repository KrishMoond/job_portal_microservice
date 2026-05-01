import { Component, OnInit, inject, signal, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { ApiService } from '../../../core/services/api.service';
import { ToastService } from '../../../core/services/toast.service';
import { Application } from '../../../shared/models/models';
import { LucideAngularModule } from 'lucide-angular';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-applicants',
  standalone: true,
  imports: [CommonModule, RouterLink, LucideAngularModule, FormsModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="min-h-screen bg-gray-50 flex flex-col pt-10 px-6 relative">
      <div class="max-w-4xl mx-auto">
        <a routerLink="/recruiter/dashboard" class="inline-flex items-center gap-1 text-sm font-medium text-gray-500 hover:text-primary transition-colors mb-6">
          <lucide-icon name="arrow-left" class="w-4 h-4"></lucide-icon> Back to Dashboard
        </a>
        
        <div class="mb-8">
          <h1 class="text-2xl font-bold text-gray-900 tracking-tight">Applicants</h1>
          <p class="text-gray-500 text-sm mt-1">Review and manage candidates who applied for this role.</p>
        </div>

        @if (loading()) {
          <div class="bg-white rounded-2xl border border-gray-200 shadow-sm p-6">
            <div class="flex items-center gap-3 text-gray-500 mb-6">
              <lucide-icon name="loader-2" class="w-5 h-5 animate-spin text-primary"></lucide-icon>
              <p class="text-sm font-semibold">Loading applicants...</p>
            </div>
            <div class="space-y-4 animate-pulse">
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
          </div>
        } @else if (loadError()) {
          <div class="bg-white rounded-2xl border border-red-100 shadow-sm text-center py-16">
            <div class="w-16 h-16 bg-red-50 rounded-full flex items-center justify-center mx-auto mb-4 border border-red-100">
              <lucide-icon name="alert-circle" class="w-8 h-8 text-red-400"></lucide-icon>
            </div>
            <h3 class="text-lg font-semibold text-gray-900 mb-1">Failed to load applicants</h3>
            <p class="text-gray-500 text-sm mb-6">The application service may be unavailable. Please try again.</p>
            <button (click)="loadApplicants()" class="btn-primary py-2 px-6">
              <lucide-icon name="loader-2" class="w-4 h-4"></lucide-icon> Retry
            </button>
          </div>
        } @else if (applications().length === 0) {
          <div class="bg-white rounded-2xl border border-gray-200 shadow-sm text-center py-20">
            <div class="w-16 h-16 bg-gray-50 rounded-full flex items-center justify-center mx-auto mb-4 border border-gray-100 text-gray-300">
              <lucide-icon name="users" class="w-8 h-8"></lucide-icon>
            </div>
            <h3 class="text-lg font-semibold text-gray-900 mb-1">No applicants yet</h3>
            <p class="text-gray-500 text-sm">Candidates who apply to your job will appear here.</p>
          </div>
        } @else {
          <div class="grid grid-cols-1 gap-4">
            @for (app of applications(); track app.id) {
              <div class="bg-white rounded-xl p-5 border border-gray-200 shadow-sm flex flex-col sm:flex-row sm:items-center justify-between gap-4 transition-shadow hover:shadow-md">
                <div class="flex items-center gap-4">
                  <div class="w-12 h-12 rounded-full bg-gray-100 flex items-center justify-center text-xl font-bold text-gray-500 flex-shrink-0">
                    {{ app.candidateEmail.charAt(0).toUpperCase() }}
                  </div>
                  <div>
                    <h3 class="font-semibold text-gray-900">{{ app.candidateEmail }}</h3>
                    <div class="flex items-center gap-3 text-xs text-gray-500 mt-2 font-medium">
                      <span class="flex items-center gap-1 bg-gray-50 px-2 py-1 rounded">
                        <lucide-icon name="clock" class="w-3.5 h-3.5 text-gray-400"></lucide-icon> Applied {{ app.appliedAt | date:'shortDate' }}
                      </span>
                      @if (app.resumeId) {
                        <button type="button" (click)="openResume(app.resumeId)" class="flex items-center gap-1 font-bold text-primary hover:text-primary-hover hover:underline transition-colors bg-primary/5 px-2 py-1 rounded border-0">
                          <lucide-icon name="paperclip" class="w-3 h-3"></lucide-icon> View Resume
                        </button>
                      } @else {
                        <span class="flex items-center gap-1 text-gray-400 italic">No resume</span>
                      }
                    </div>
                  </div>
                </div>
                
                <div class="flex items-center gap-3 w-full sm:w-auto">
                  <span class="badge" [ngClass]="statusClass(app.status)">
                    {{ formatStatus(app.status) }}
                  </span>
                  
                  <select (change)="updateStatus(app.id, $any($event.target).value)" [value]="app.status"
                    class="form-input text-sm py-1.5 px-3 min-w-[140px] bg-gray-50 border-gray-200">
                    <option value="APPLIED">Applied</option>
                    <option value="SHORTLISTED">Shortlist</option>
                    <option value="INTERVIEW_SCHEDULED">Interview</option>
                    <option value="HIRED">Hire</option>
                    <option value="REJECTED">Reject</option>
                  </select>
                </div>
              </div>
            }
          </div>
        }
      </div>

      <!-- Interview Scheduling Modal -->
      @if (showInterviewModal()) {
        <div class="fixed inset-0 bg-black/40 backdrop-blur-sm z-50 flex items-center justify-center animate-fade-in p-4">
          <div class="bg-white rounded-3xl w-full max-w-md shadow-[0_10px_40px_-10px_rgba(0,0,0,0.3)] border border-white/20 p-8 animate-scale-in-flex">
            <div class="flex items-center justify-between mb-6">
              <h2 class="text-xl font-bold text-gray-900 flex items-center gap-2">
                <lucide-icon name="calendar" class="w-5 h-5 text-primary"></lucide-icon> Schedule Interview
              </h2>
              <button (click)="closeModal()" class="text-gray-400 hover:text-gray-600 transition-colors focus:outline-none">
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
                <button (click)="confirmInterview()" class="flex-1 btn-primary py-2.5 px-4 !rounded-xl shadow-sm">Confirm & Notify</button>
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
  private api = inject(ApiService);
  private toast = inject(ToastService);

  applications = signal<Application[]>([]);
  loading = signal(false);
  loadError = signal(false);
  private jobId = '';

  // Modal State
  showInterviewModal = signal(false);
  selectedAppId: string | null = null;
  interviewData = {
    scheduledAt: '',
    meetingLink: '',
    applicationId: '',
    candidateId: ''
  };

  private normalizeAppliedAt(app: Application): Application {
    const appliedAtAny = (app as any).appliedAt;
    if (Array.isArray(appliedAtAny) && appliedAtAny.length >= 3) {
      const [year, month, day, hour = 0, minute = 0, second = 0] = appliedAtAny;
      const normalized = new Date(year, month - 1, day, hour, minute, second).toISOString();
      return { ...app, appliedAt: normalized };
    }
    return app;
  }

  ngOnInit(): void {
    this.jobId = this.route.snapshot.paramMap.get('jobId') || '';
    this.loadApplicants();
  }

  loadApplicants(): void {
    if (!this.jobId) {
      this.toast.error('Invalid job ID');
      return;
    }
    this.loading.set(true);
    this.loadError.set(false);

    this.api.getJobApplications(this.jobId).subscribe({
      next: (res) => {
        const raw = res.data ?? (Array.isArray(res as any) ? res as any : []);
        this.applications.set(raw.map((a: Application) => this.normalizeAppliedAt(a)));
        this.loading.set(false);
        this.loadError.set(false);
      },
      error: (err) => {
        console.error('Failed to load applicants:', err);
        this.loading.set(false);
        this.loadError.set(true);
        this.toast.error('Failed to load applicants');
      }
    });
  }

  formatStatus(status: string): string {
    return status.replace(/_/g, ' ');
  }

  updateStatus(appId: string, status: string): void {
    if (!status) return;

    if (status === 'INTERVIEW_SCHEDULED') {
      this.selectedAppId = appId;
      const app = this.applications().find(a => a.id === appId);
      this.interviewData.applicationId = appId;
      this.interviewData.candidateId = (app as any)?.candidateId || '';
      this.showInterviewModal.set(true);
      return;
    }

    this.executeStatusUpdate(appId, status);
  }

  closeModal(): void {
    this.showInterviewModal.set(false);
    this.selectedAppId = null;
    this.interviewData = { scheduledAt: '', meetingLink: '', applicationId: '', candidateId: '' };
  }

  confirmInterview(): void {
    if (!this.selectedAppId || !this.interviewData.scheduledAt) {
      this.toast.error("Please provide interview date and time");
      return;
    }
    if (!this.interviewData.candidateId) {
      this.toast.error("Missing candidate ID for this application");
      return;
    }

    if (this.interviewData.meetingLink && !this.interviewData.meetingLink.startsWith('http')) {
      this.toast.error("Meeting link must start with http:// or https://");
      return;
    }

    this.api.scheduleInterview(this.interviewData).subscribe({
      next: () => {
        this.executeStatusUpdate(this.selectedAppId!, 'INTERVIEW_SCHEDULED');
        this.toast.success("Interview scheduled successfully!");
        this.closeModal();
      },
      error: (err) => {
        console.error('Interview scheduling error:', err);
        this.toast.error(err?.error?.message || "Failed to schedule interview");
      }
    });
  }

  private executeStatusUpdate(appId: string, status: string): void {
    this.api.updateApplicationStatus(appId, status).subscribe({
      next: () => {
        this.applications.update(apps =>
          apps.map(a => a.id === appId ? { ...a, status: status as any } : a)
        );
        this.toast.success('Status updated successfully');
      },
      error: () => this.toast.error('Failed to update status')
    });
  }

  openResume(resumeId: string): void {
    this.api.downloadResume(resumeId).subscribe({
      next: (blob: Blob) => {
        const blobUrl = URL.createObjectURL(blob);
        window.open(blobUrl, '_blank', 'noopener,noreferrer');
        setTimeout(() => URL.revokeObjectURL(blobUrl), 60000);
      },
      error: () => this.toast.error('Unable to open resume')
    });
  }

  statusClass(status: string): string {
    switch(status) {
      case 'APPLIED':
      case 'SHORTLISTED':
      case 'INTERVIEW_SCHEDULED':
        return 'badge-pending';
      case 'HIRED':
        return 'badge-accepted';
      case 'REJECTED':
        return 'badge-rejected';
      default:
        return 'bg-gray-100 text-gray-700';
    }
  }
}
