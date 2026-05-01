import { Component, OnInit, inject } from '@angular/core';
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
  imports: [CommonModule, RouterLink, FormsModule, LucideAngularModule],
  template: `
    <div class="min-h-screen bg-gray-50 py-10 px-6">
      <div class="max-w-3xl mx-auto">
        <a routerLink="/jobs" class="inline-flex items-center gap-1 text-sm font-medium text-gray-500 hover:text-primary transition-colors mb-6">
          <lucide-icon name="arrow-left" class="w-4 h-4"></lucide-icon> Back to jobs
        </a>

        <div class="bg-white rounded-2xl border border-gray-200 shadow-sm p-8 mb-6">
          <div class="flex items-center justify-between gap-4 mb-8">
            <div class="flex items-center gap-4">
              <div class="w-16 h-16 rounded-full bg-primary-light flex items-center justify-center border border-primary/20">
                <span class="text-primary text-2xl font-bold">{{ initial }}</span>
              </div>
              <div>
                <h1 class="text-2xl font-bold text-gray-900">My Profile</h1>
                <p class="text-sm text-gray-500">Your account details and resume</p>
              </div>
            </div>
            @if (!editMode) {
              <button (click)="startEdit()" class="btn-secondary py-1.5 px-4 text-sm flex items-center gap-1.5">
                <lucide-icon name="pencil" class="w-3.5 h-3.5"></lucide-icon> Edit
              </button>
            }
          </div>

          @if (loading) {
            <div class="space-y-4 animate-pulse">
              <div class="h-14 bg-gray-100 rounded-lg"></div>
              <div class="h-14 bg-gray-100 rounded-lg"></div>
              <div class="h-14 bg-gray-100 rounded-lg"></div>
            </div>
          } @else if (editMode) {
            <!-- Edit Form -->
            <form (ngSubmit)="saveProfile()" class="space-y-4">
              <div>
                <label class="form-label">Full Name</label>
                <input type="text" [(ngModel)]="editName" name="name" required class="form-input" placeholder="Your name" />
              </div>
              <div>
                <label class="form-label">Email Address</label>
                <input type="email" [(ngModel)]="editEmail" name="email" required class="form-input" placeholder="you@example.com" />
              </div>
              <div>
                <label class="form-label">New Password <span class="text-gray-400 font-normal">(leave blank to keep current)</span></label>
                <input type="password" [(ngModel)]="editPassword" name="password" class="form-input" placeholder="••••••••" />
              </div>
              <div>
                <label class="form-label">Phone Number</label>
                <input type="tel" [(ngModel)]="editPhone" name="phone" class="form-input" placeholder="+1 (555) 123-4567" />
              </div>
              <div>
                <label class="form-label">Location</label>
                <input type="text" [(ngModel)]="editLocation" name="location" class="form-input" placeholder="City, State/Country" />
              </div>
              <div>
                <label class="form-label">Experience Level</label>
                <select [(ngModel)]="editExperienceLevel" name="experienceLevel" class="form-input">
                  <option value="">Select experience level</option>
                  <option value="ENTRY">Entry Level</option>
                  <option value="JUNIOR">Junior</option>
                  <option value="MID">Mid Level</option>
                  <option value="SENIOR">Senior</option>
                  <option value="EXECUTIVE">Executive</option>
                </select>
              </div>
              <div>
                <label class="form-label">Education</label>
                <textarea [(ngModel)]="editEducation" name="education" rows="3" class="form-input" placeholder="Your educational background"></textarea>
              </div>
              <div>
                <label class="form-label">Preferred Job Types</label>
                <input type="text" [(ngModel)]="editPreferredJobTypes" name="preferredJobTypes" class="form-input" placeholder="Full-time, Remote, Contract (comma-separated)" />
              </div>
              <div class="flex gap-3 pt-2">
                <button type="submit" [disabled]="savingProfile" class="btn-primary py-2 px-6 disabled:opacity-70">
                  @if (savingProfile) { <lucide-icon name="loader-2" class="w-4 h-4 animate-spin"></lucide-icon> } @else { Save Changes }
                </button>
                <button type="button" (click)="cancelEdit()" class="btn-secondary py-2 px-6">Cancel</button>
              </div>
            </form>
          } @else {
            <div class="grid grid-cols-1 gap-4">
              <div class="border border-gray-200 rounded-lg p-4">
                <p class="text-xs text-gray-500 mb-1">Name</p>
                <p class="font-semibold text-gray-900">{{ profile?.name || 'Not available' }}</p>
              </div>
              <div class="border border-gray-200 rounded-lg p-4">
                <p class="text-xs text-gray-500 mb-1">Email</p>
                <p class="font-semibold text-gray-900">{{ profile?.email || 'Not available' }}</p>
              </div>
              <div class="border border-gray-200 rounded-lg p-4">
                <p class="text-xs text-gray-500 mb-1">Role</p>
                <p class="font-semibold text-gray-900">{{ profile?.role || 'JOB_SEEKER' }}</p>
              </div>
              <div class="border border-gray-200 rounded-lg p-4">
                <p class="text-xs text-gray-500 mb-1">Phone</p>
                <p class="font-semibold text-gray-900">{{ profile?.phone || 'Not provided' }}</p>
              </div>
              <div class="border border-gray-200 rounded-lg p-4">
                <p class="text-xs text-gray-500 mb-1">Location</p>
                <p class="font-semibold text-gray-900">{{ profile?.location || 'Not provided' }}</p>
              </div>
              <div class="border border-gray-200 rounded-lg p-4">
                <p class="text-xs text-gray-500 mb-1">Experience Level</p>
                <p class="font-semibold text-gray-900">{{ profile?.experienceLevel || 'Not provided' }}</p>
              </div>
              <div class="border border-gray-200 rounded-lg p-4">
                <p class="text-xs text-gray-500 mb-1">Education</p>
                <p class="font-semibold text-gray-900">{{ profile?.education || 'Not provided' }}</p>
              </div>
              <div class="border border-gray-200 rounded-lg p-4">
                <p class="text-xs text-gray-500 mb-1">Preferred Job Types</p>
                <p class="font-semibold text-gray-900">{{ profile?.preferredJobTypes || 'Not provided' }}</p>
              </div>
              <div class="border border-gray-200 rounded-lg p-4">
                <p class="text-xs text-gray-500 mb-1">User ID</p>
                <p class="font-mono text-xs text-gray-700 break-all">{{ profile?.userId }}</p>
              </div>
            </div>
          }
        </div>

        <!-- Resume Section -->
        <div class="bg-white rounded-2xl border border-gray-200 shadow-sm p-8">
          <div class="flex items-center justify-between gap-4 mb-6">
            <div>
              <h2 class="text-lg font-bold text-gray-900">Resume / CV</h2>
              <p class="text-sm text-gray-500">Upload your resume file (PDF or image) to apply to jobs.</p>
            </div>
          </div>

          <div class="mb-4">
            <label class="block text-xs font-semibold text-gray-600 mb-1">Resume file</label>
            <input type="file" accept=".pdf,image/*" (change)="onResumeFileSelected($event)"
                   class="form-input bg-gray-50 border-gray-200" />
            <p class="text-xs text-gray-400 mt-2">Allowed formats: PDF, JPG, PNG, WEBP</p>
          </div>

          <button (click)="uploadResume()" [disabled]="resumeUploading"
                  class="btn-primary py-2.5 px-6 shadow-sm">
            {{ resumeUploading ? 'Uploading...' : 'Upload Resume' }}
          </button>

          <div class="mt-6">
            @if (resumeLoading) {
              <p class="text-sm text-gray-400">Loading resumes...</p>
            } @else if (resumes.length === 0) {
              <p class="text-sm text-gray-500">No resume uploaded yet.</p>
            } @else {
              <div class="space-y-2">
                @for (r of resumes; track r.resumeId || r.id) {
                  <button type="button" (click)="openResume(r)" class="block w-full text-left border border-gray-200 rounded-lg px-4 py-3 hover:bg-gray-50 transition bg-transparent">
                    <div class="flex items-center justify-between gap-4">
                      <div>
                        <p class="text-sm font-semibold text-gray-900">{{ r.fileName || 'Resume' }}</p>
                        <p class="text-xs text-gray-500 truncate max-w-[520px]">{{ r.fileUrl }}</p>
                      </div>
                      <span class="text-xs text-gray-400">Open</span>
                    </div>
                  </button>
                }
              </div>
            }
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

  profile: any = null;
  loading = true;
  initial = '?';

  // Edit mode state
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

  cancelEdit(): void {
    this.editMode = false;
  }

  saveProfile(): void {
    if (!this.editName.trim() || !this.editEmail.trim()) {
      this.toast.error('Name and email are required');
      return;
    }
    const userId = this.profile?.userId || this.profile?.id;
    if (!userId) { this.toast.error('User ID missing'); return; }

    this.savingProfile = true;

    // If password is being changed, use the full update endpoint
    if (this.editPassword.trim()) {
      const payload: any = { 
        name: this.editName.trim(), 
        email: this.editEmail.trim(), 
        password: this.editPassword.trim(),
        role: this.profile?.role || 'JOB_SEEKER' 
      };
      this.api.updateUserProfile(userId, payload).subscribe({
        next: (res: any) => {
          this.handleProfileUpdate(res);
        },
        error: (err) => {
          this.toast.error('Failed to update profile');
          this.savingProfile = false;
        }
      });
    } else {
      // Update profile details
      const profilePayload: any = {
        name: this.editName.trim()
      };
      if (this.editPhone.trim()) profilePayload.phone = this.editPhone.trim();
      if (this.editLocation.trim()) profilePayload.location = this.editLocation.trim();
      if (this.editExperienceLevel) profilePayload.experienceLevel = this.editExperienceLevel;
      if (this.editEducation.trim()) profilePayload.education = this.editEducation.trim();
      if (this.editPreferredJobTypes.trim()) profilePayload.preferredJobTypes = this.editPreferredJobTypes.trim();
      
      this.api.updateUserProfileDetails(userId, profilePayload).subscribe({
        next: (res: any) => {
          this.handleProfileUpdate(res);
        },
        error: (err) => {
          this.toast.error('Failed to update profile');
          this.savingProfile = false;
        }
      });
    }
  }

  private handleProfileUpdate(res: any): void {
    const updated = res?.data ?? res;
    this.profile = { ...this.profile, ...updated };
    this.initial = (this.profile.name?.charAt(0) || 'U').toUpperCase();
    // Update stored user so navbar reflects new name
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
  }

  selectedResumeFile: File | null = null;
  resumes: any[] = [];
  resumeLoading = false;
  resumeUploading = false;

  ngOnInit(): void {
    const user = this.auth.getCurrentUser();
    if (!user?.userId) {
      this.loading = false;
      return;
    }

    this.profile = user;
    this.initial = (user.name?.charAt(0) || 'U').toUpperCase();

    this.api.getUserById(user.userId).subscribe({
      next: (res: any) => {
        const full = res?.data ?? res ?? user;
        this.profile = full;
        this.initial = (full.name?.charAt(0) || 'U').toUpperCase();
        this.loading = false;
      },
      error: () => {
        this.toast.warning('Profile details could not be refreshed');
        this.loading = false;
      }
    });

    this.loadResumes(user.userId);
  }

  private loadResumes(userId: string): void {
    this.resumeLoading = true;
    this.api.getMyResumes(userId).subscribe({
      next: (res: any) => {
        const list = Array.isArray(res) ? res : (res?.data || []);
        this.resumes = list;
        this.resumeLoading = false;
      },
      error: () => {
        this.resumes = [];
        this.resumeLoading = false;
      }
    });
  }

  uploadResume(): void {
    if (!this.selectedResumeFile) {
      this.toast.error('Please choose a resume file');
      return;
    }
    this.resumeUploading = true;
    this.api.uploadResumeFile(this.selectedResumeFile).subscribe({
      next: () => {
        this.toast.success('Resume uploaded');
        this.selectedResumeFile = null;
        this.resumeUploading = false;
        const userId = this.auth.getUserId();
        if (userId) this.loadResumes(userId);
      },
      error: (err: any) => {
        this.resumeUploading = false;
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
    if (!resumeId) {
      this.toast.error('Resume file id is missing');
      return;
    }
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
    const idPart = url.substring(idx + marker.length).split(/[?#]/)[0];
    return idPart || null;
  }
}
