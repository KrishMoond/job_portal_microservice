import { Component, OnInit, inject, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { LucideAngularModule } from 'lucide-angular';
import { AuthService } from '../../../core/services/auth.service';
import { ApiService } from '../../../core/services/api.service';
import { ToastService } from '../../../core/services/toast.service';

@Component({
  selector: 'app-profile',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [CommonModule, RouterLink, FormsModule, LucideAngularModule],
  template: `
    <div class="min-h-screen bg-gray-50 py-8 px-4 sm:px-6">
      <div class="max-w-5xl mx-auto">

        <a routerLink="/jobs" class="inline-flex items-center gap-1.5 text-sm font-medium text-gray-500 hover:text-primary transition-colors mb-6">
          <lucide-icon name="arrow-left" class="w-4 h-4"></lucide-icon> Back to jobs
        </a>

        <div class="grid grid-cols-1 lg:grid-cols-[260px_1fr] gap-6">

          <!-- ── Sidebar ── -->
          <aside class="space-y-4">
            <!-- Avatar + name card -->
            <div class="bg-white rounded-2xl border border-gray-200 shadow-sm p-6 flex flex-col items-center text-center">
              <div class="w-20 h-20 rounded-2xl flex items-center justify-center text-3xl font-black mb-3 shadow-sm"
                [ngClass]="avatarClass()">
                {{ initial }}
              </div>
              <h2 class="text-lg font-bold text-gray-900 leading-tight">{{ profile?.name || '—' }}</h2>
              <p class="text-sm text-gray-500 mt-0.5">{{ roleLabel() }}</p>
              @if (profile?.location) {
                <p class="text-xs text-gray-400 mt-1 flex items-center gap-1 justify-center">
                  <lucide-icon name="map-pin" class="w-3 h-3"></lucide-icon> {{ profile.location }}
                </p>
              }
            </div>

            <!-- Quick stats -->
            <div class="bg-white rounded-2xl border border-gray-200 shadow-sm p-4 space-y-3">
              <h3 class="text-xs font-bold text-gray-400 uppercase tracking-wider px-1">Overview</h3>
              <div class="profile-stat-card">
                <span class="profile-stat-value">{{ resumes.length }}</span>
                <span class="profile-stat-label">Resumes uploaded</span>
              </div>
              <div class="profile-stat-card">
                <span class="profile-stat-value">{{ profile?.experienceLevel || '—' }}</span>
                <span class="profile-stat-label">Experience level</span>
              </div>
            </div>

            <!-- Quick links -->
            <div class="bg-white rounded-2xl border border-gray-200 shadow-sm p-4 space-y-1">
              <h3 class="text-xs font-bold text-gray-400 uppercase tracking-wider px-1 mb-2">Quick links</h3>
              <a routerLink="/my-applications" class="flex items-center gap-2.5 px-3 py-2.5 rounded-xl text-sm font-medium text-gray-700 hover:bg-gray-50 hover:text-primary transition-colors">
                <lucide-icon name="file-text" class="w-4 h-4 text-gray-400"></lucide-icon> My Applications
              </a>
              <a routerLink="/bookmarks" class="flex items-center gap-2.5 px-3 py-2.5 rounded-xl text-sm font-medium text-gray-700 hover:bg-gray-50 hover:text-primary transition-colors">
                <lucide-icon name="bookmark" class="w-4 h-4 text-gray-400"></lucide-icon> Saved Jobs
              </a>
              <a routerLink="/notifications" class="flex items-center gap-2.5 px-3 py-2.5 rounded-xl text-sm font-medium text-gray-700 hover:bg-gray-50 hover:text-primary transition-colors">
                <lucide-icon name="bell" class="w-4 h-4 text-gray-400"></lucide-icon> Notifications
              </a>
            </div>
          </aside>

          <!-- ── Main content ── -->
          <div class="space-y-6">

            <!-- Profile details card -->
            <div class="bg-white rounded-2xl border border-gray-200 shadow-sm p-6">
              <div class="flex items-center justify-between mb-5">
                <h2 class="text-lg font-bold text-gray-900">Profile Details</h2>
                @if (!editMode) {
                  <button (click)="startEdit()" class="btn-secondary py-1.5 px-4 text-sm flex items-center gap-1.5">
                    <lucide-icon name="pencil" class="w-3.5 h-3.5"></lucide-icon> Edit
                  </button>
                }
              </div>

              @if (loading) {
                <div class="grid grid-cols-1 sm:grid-cols-2 gap-3 animate-pulse">
                  @for (i of [1,2,3,4,5,6]; track i) {
                    <div class="h-16 bg-gray-100 rounded-xl"></div>
                  }
                </div>
              } @else if (editMode) {
                <form (ngSubmit)="saveProfile()" class="space-y-4">
                  <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
                    <div>
                      <label class="form-label">Full Name</label>
                      <input type="text" [(ngModel)]="editName" name="name" required class="form-input" placeholder="Your name" />
                    </div>
                    <div>
                      <label class="form-label">Email Address</label>
                      <input type="email" [(ngModel)]="editEmail" name="email" required class="form-input" placeholder="you@example.com" />
                    </div>
                    <div>
                      <label class="form-label">Phone Number</label>
                      <input type="tel" [(ngModel)]="editPhone" name="phone" class="form-input" placeholder="+1 (555) 123-4567" />
                    </div>
                    <div>
                      <label class="form-label">Location</label>
                      <input type="text" [(ngModel)]="editLocation" name="location" class="form-input" placeholder="City, Country" />
                    </div>
                    <div>
                      <label class="form-label">Experience Level</label>
                      <select [(ngModel)]="editExperienceLevel" name="experienceLevel" class="form-input">
                        <option value="">Select level</option>
                        <option value="ENTRY">Entry Level</option>
                        <option value="JUNIOR">Junior</option>
                        <option value="MID">Mid Level</option>
                        <option value="SENIOR">Senior</option>
                        <option value="EXECUTIVE">Executive</option>
                      </select>
                    </div>
                    <div>
                      <label class="form-label">Preferred Job Types</label>
                      <input type="text" [(ngModel)]="editPreferredJobTypes" name="preferredJobTypes" class="form-input" placeholder="Full-time, Remote..." />
                    </div>
                  </div>
                  <div>
                    <label class="form-label">Education</label>
                    <textarea [(ngModel)]="editEducation" name="education" rows="2" class="form-input" placeholder="Your educational background"></textarea>
                  </div>
                  <div>
                    <label class="form-label">New Password <span class="text-gray-400 font-normal">(leave blank to keep current)</span></label>
                    <input type="password" [(ngModel)]="editPassword" name="password" class="form-input" placeholder="••••••••" />
                  </div>
                  <div class="flex gap-3 pt-1">
                    <button type="submit" [disabled]="savingProfile" class="btn-primary py-2 px-6 disabled:opacity-70">
                      @if (savingProfile) { <lucide-icon name="loader-2" class="w-4 h-4 animate-spin"></lucide-icon> } @else { Save Changes }
                    </button>
                    <button type="button" (click)="cancelEdit()" class="btn-secondary py-2 px-6">Cancel</button>
                  </div>
                </form>
              } @else {
                <div class="grid grid-cols-1 sm:grid-cols-2 gap-3">
                  <div class="profile-field">
                    <span class="profile-field-label">Name</span>
                    <span class="profile-field-value">{{ profile?.name || '—' }}</span>
                  </div>
                  <div class="profile-field">
                    <span class="profile-field-label">Email</span>
                    <span class="profile-field-value">{{ profile?.email || '—' }}</span>
                  </div>
                  <div class="profile-field">
                    <span class="profile-field-label">Phone</span>
                    <span class="profile-field-value">{{ profile?.phone || 'Not provided' }}</span>
                  </div>
                  <div class="profile-field">
                    <span class="profile-field-label">Location</span>
                    <span class="profile-field-value">{{ profile?.location || 'Not provided' }}</span>
                  </div>
                  <div class="profile-field">
                    <span class="profile-field-label">Experience Level</span>
                    <span class="profile-field-value">{{ profile?.experienceLevel || 'Not provided' }}</span>
                  </div>
                  <div class="profile-field">
                    <span class="profile-field-label">Preferred Job Types</span>
                    <span class="profile-field-value">{{ profile?.preferredJobTypes || 'Not provided' }}</span>
                  </div>
                  <div class="profile-field sm:col-span-2">
                    <span class="profile-field-label">Education</span>
                    <span class="profile-field-value">{{ profile?.education || 'Not provided' }}</span>
                  </div>
                  <div class="profile-field sm:col-span-2">
                    <span class="profile-field-label">User ID</span>
                    <span class="font-mono text-xs text-gray-500 break-all">{{ profile?.userId }}</span>
                  </div>
                </div>
              }
            </div>

            <!-- Resume card -->
            <div class="bg-white rounded-2xl border border-gray-200 shadow-sm p-6">
              <div class="flex items-center justify-between mb-5">
                <div>
                  <h2 class="text-lg font-bold text-gray-900">Resume / CV</h2>
                  <p class="text-sm text-gray-500 mt-0.5">PDF or image, max 10 MB.</p>
                </div>
              </div>

              <div class="flex flex-col sm:flex-row gap-3 mb-5">
                <input type="file" accept=".pdf,image/*" (change)="onResumeFileSelected($event)"
                  class="form-input bg-gray-50 border-gray-200 flex-1 text-sm" />
                <button (click)="uploadResume()" [disabled]="resumeUploading"
                  class="btn-primary py-2.5 px-6 whitespace-nowrap">
                  @if (resumeUploading) {
                    <lucide-icon name="loader-2" class="w-4 h-4 animate-spin"></lucide-icon>
                  } @else {
                    <lucide-icon name="upload" class="w-4 h-4"></lucide-icon>
                  }
                  {{ resumeUploading ? 'Uploading...' : 'Upload' }}
                </button>
              </div>

              @if (resumeLoading) {
                <div class="space-y-2 animate-pulse">
                  <div class="h-14 bg-gray-100 rounded-xl"></div>
                </div>
              } @else if (resumes.length === 0) {
                <div class="border-2 border-dashed border-gray-200 rounded-xl p-8 text-center">
                  <lucide-icon name="file-text" class="w-8 h-8 text-gray-300 mx-auto mb-2"></lucide-icon>
                  <p class="text-sm text-gray-500 font-medium">No resume uploaded yet</p>
                  <p class="text-xs text-gray-400 mt-1">Upload a PDF or image to apply to jobs faster</p>
                </div>
              } @else {
                <div class="space-y-2">
                  @for (r of resumes; track r.resumeId || r.id) {
                    <button type="button" (click)="openResume(r)"
                      class="w-full text-left border border-gray-200 rounded-xl px-4 py-3 hover:bg-gray-50 hover:border-primary/30 transition-all bg-transparent group">
                      <div class="flex items-center gap-3">
                        <div class="w-9 h-9 rounded-lg bg-red-50 border border-red-100 flex items-center justify-center flex-shrink-0">
                          <lucide-icon name="file-text" class="w-4 h-4 text-red-500"></lucide-icon>
                        </div>
                        <div class="flex-1 min-w-0">
                          <p class="text-sm font-semibold text-gray-900 group-hover:text-primary transition-colors">{{ r.fileName || 'Resume' }}</p>
                          <p class="text-xs text-gray-400 truncate">{{ r.uploadedAt | date:'mediumDate' }}</p>
                        </div>
                        <lucide-icon name="external-link" class="w-4 h-4 text-gray-400 group-hover:text-primary transition-colors flex-shrink-0"></lucide-icon>
                      </div>
                    </button>
                  }
                </div>
              }
            </div>

          </div>
        </div>
      </div>
    </div>
  `
})
export class ProfileComponent implements OnInit {
  private auth = inject(AuthService);
  private api = inject(ApiService);
  private toast = inject(ToastService);
  private cdr = inject(ChangeDetectorRef);

  profile: any = null;
  loading = true;
  initial = '?';

  editMode = false;
  editName = '';
  editEmail = '';
  editPassword = '';
  editPhone = '';
  editLocation = '';
  editExperienceLevel = '';
  editEducation = '';
  editPreferredJobTypes = '';
  savingProfile = false;

  selectedResumeFile: File | null = null;
  resumes: any[] = [];
  resumeLoading = false;
  resumeUploading = false;

  avatarClass(): string {
    const classes = ['avatar-a0','avatar-a1','avatar-a2','avatar-a3','avatar-a4','avatar-a5','avatar-a6','avatar-a7'];
    const code = (this.profile?.name || 'U').charCodeAt(0);
    return classes[code % classes.length];
  }

  roleLabel(): string {
    const r = this.profile?.role || '';
    if (r === 'JOB_SEEKER') return 'Job Seeker';
    if (r === 'RECRUITER') return 'Recruiter';
    if (r === 'ADMIN') return 'Admin';
    return r || 'Job Seeker';
  }

  startEdit(): void {
    this.editName = this.profile?.name || '';
    this.editEmail = this.profile?.email || '';
    this.editPassword = '';
    this.editPhone = this.profile?.phone || '';
    this.editLocation = this.profile?.location || '';
    this.editExperienceLevel = this.profile?.experienceLevel || '';
    this.editEducation = this.profile?.education || '';
    this.editPreferredJobTypes = this.profile?.preferredJobTypes || '';
    this.editMode = true;
  }

  cancelEdit(): void { this.editMode = false; }

  saveProfile(): void {
    if (!this.editName.trim() || !this.editEmail.trim()) {
      this.toast.error('Name and email are required');
      return;
    }
    const userId = this.profile?.userId || this.profile?.id;
    if (!userId) { this.toast.error('User ID missing'); return; }
    this.savingProfile = true;

    if (this.editPassword.trim()) {
      const payload: any = { name: this.editName.trim(), email: this.editEmail.trim(), password: this.editPassword.trim(), role: this.profile?.role || 'JOB_SEEKER' };
      this.api.updateUserProfile(userId, payload).subscribe({
        next: (res: any) => this.handleProfileUpdate(res),
        error: () => { this.toast.error('Failed to update profile'); this.savingProfile = false; this.cdr.markForCheck(); }
      });
    } else {
      const profilePayload: any = { name: this.editName.trim() };
      if (this.editPhone.trim()) profilePayload.phone = this.editPhone.trim();
      if (this.editLocation.trim()) profilePayload.location = this.editLocation.trim();
      if (this.editExperienceLevel) profilePayload.experienceLevel = this.editExperienceLevel;
      if (this.editEducation.trim()) profilePayload.education = this.editEducation.trim();
      if (this.editPreferredJobTypes.trim()) profilePayload.preferredJobTypes = this.editPreferredJobTypes.trim();
      this.api.updateUserProfileDetails(userId, profilePayload).subscribe({
        next: (res: any) => this.handleProfileUpdate(res),
        error: () => { this.toast.error('Failed to update profile'); this.savingProfile = false; this.cdr.markForCheck(); }
      });
    }
  }

  private handleProfileUpdate(res: any): void {
    const updated = res?.data ?? res;
    this.profile = { ...this.profile, ...updated };
    this.initial = (this.profile.name?.charAt(0) || 'U').toUpperCase();
    const stored = localStorage.getItem('current_user');
    if (stored) {
      try {
        const u = JSON.parse(stored);
        localStorage.setItem('current_user', JSON.stringify({ ...u, name: this.profile.name, email: this.profile.email }));
      } catch { /* ignore */ }
    }
    this.toast.success('Profile updated successfully');
    this.savingProfile = false;
    this.editMode = false;
    this.cdr.markForCheck();
  }

  ngOnInit(): void {
    const user = this.auth.getCurrentUser();
    if (!user?.userId) { this.loading = false; return; }
    this.profile = user;
    this.initial = (user.name?.charAt(0) || 'U').toUpperCase();
    this.api.getUserById(user.userId).subscribe({
      next: (res: any) => {
        const full = res?.data ?? res ?? user;
        this.profile = full;
        this.initial = (full.name?.charAt(0) || 'U').toUpperCase();
        this.loading = false;
        this.cdr.markForCheck();
      },
      error: () => { this.toast.warning('Profile details could not be refreshed'); this.loading = false; this.cdr.markForCheck(); }
    });
    this.loadResumes(user.userId);
  }

  private loadResumes(userId: string): void {
    this.resumeLoading = true;
    this.api.getMyResumes(userId).subscribe({
      next: (res: any) => {
        this.resumes = Array.isArray(res) ? res : (res?.data || []);
        this.resumeLoading = false;
        this.cdr.markForCheck();
      },
      error: () => { this.resumes = []; this.resumeLoading = false; this.cdr.markForCheck(); }
    });
  }

  uploadResume(): void {
    if (!this.selectedResumeFile) { this.toast.error('Please choose a resume file'); return; }
    this.resumeUploading = true;
    this.api.uploadResumeFile(this.selectedResumeFile).subscribe({
      next: () => {
        this.toast.success('Resume uploaded');
        this.selectedResumeFile = null;
        this.resumeUploading = false;
        this.cdr.markForCheck();
        const userId = this.auth.getUserId();
        if (userId) this.loadResumes(userId);
      },
      error: (err: any) => {
        this.resumeUploading = false;
        this.cdr.markForCheck();
        this.toast.error(err?.error?.message || 'Failed to upload resume');
      }
    });
  }

  onResumeFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.selectedResumeFile = input.files && input.files.length > 0 ? input.files[0] : null;
  }

  openResume(resume: any): void {
    const resumeId = this.resolveResumeId(resume);
    if (!resumeId) { this.toast.error('Resume file id is missing'); return; }
    this.api.downloadResume(resumeId).subscribe({
      next: (blob: Blob) => {
        const blobUrl = URL.createObjectURL(blob);
        window.open(blobUrl, '_blank', 'noopener,noreferrer');
        setTimeout(() => URL.revokeObjectURL(blobUrl), 60000);
      },
      error: () => this.toast.error('Unable to open resume')
    });
  }

  private resolveResumeId(resume: any): string | null {
    if (resume?.resumeId) return resume.resumeId;
    if (resume?.id) return resume.id;
    const url: string = resume?.fileUrl || '';
    const marker = '/api/resumes/download/';
    const idx = url.indexOf(marker);
    if (idx < 0) return null;
    return url.substring(idx + marker.length).split(/[?#]/)[0] || null;
  }
}
