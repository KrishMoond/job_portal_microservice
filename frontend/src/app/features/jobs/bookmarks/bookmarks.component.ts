import { Component, OnInit, inject, signal, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { ApiService } from '../../../core/services/api.service';
import { ToastService } from '../../../core/services/toast.service';
import { Job } from '../../../shared/models/models';
import { LucideAngularModule } from 'lucide-angular';
import { forkJoin, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';

@Component({
  selector: 'app-bookmarks',
  standalone: true,
  imports: [CommonModule, RouterLink, LucideAngularModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="min-h-screen bg-gray-50 py-10 px-6">
      <div class="max-w-4xl mx-auto">
        <div class="mb-8">
          <h1 class="text-2xl font-bold text-gray-900 tracking-tight">Saved Jobs</h1>
          <p class="text-gray-500 text-sm mt-1">Review your bookmarked opportunities.</p>
        </div>

        @if (loading()) {
          <div class="flex justify-center flex-col items-center py-20 text-gray-400">
            <lucide-icon name="loader-2" class="w-10 h-10 animate-spin text-primary mb-4"></lucide-icon>
            <p class="text-sm font-medium">Loading bookmarks...</p>
          </div>
        } @else if (bookmarkedJobs().length === 0) {
          <div class="bg-white rounded-2xl border border-gray-200 shadow-sm text-center py-20">
            <div class="w-16 h-16 bg-gray-50 rounded-full flex items-center justify-center mx-auto mb-4 border border-gray-100 text-gray-300">
              <lucide-icon name="bookmark" class="w-8 h-8"></lucide-icon>
            </div>
            <h3 class="text-lg font-semibold text-gray-900 mb-1">No saved jobs</h3>
            <p class="text-gray-500 text-sm mb-6">You haven't bookmarked any jobs yet.</p>
            <a routerLink="/jobs" class="btn-primary py-2 px-6">Browse Jobs</a>
          </div>
        } @else {
          <div class="grid grid-cols-1 gap-4">
            @for (job of bookmarkedJobs(); track job.jobId || job['id']) {
              <div class="bg-white rounded-xl p-5 border border-gray-200 shadow-sm flex flex-col sm:flex-row sm:items-center justify-between gap-4 transition-shadow hover:shadow-md cursor-pointer block relative">
                 <a [routerLink]="['/jobs', job.jobId || job['id']]" class="absolute inset-0 z-0"></a>
                 
                 <div class="flex items-center gap-4 z-10">
                  <div class="w-12 h-12 rounded-xl bg-gray-100 flex items-center justify-center text-xl font-bold text-gray-400 flex-shrink-0">
                    {{ job.company ? job.company.substring(0,2).toUpperCase() : 'CO' }}
                  </div>
                  <div>
                    <h3 class="font-bold text-gray-900 group-hover:text-primary transition-colors">{{ job.title }}</h3>
                    <div class="flex items-center gap-3 text-xs text-gray-500 mt-1 font-medium">
                      <span class="flex items-center gap-1"><lucide-icon name="building-2" class="w-3 h-3"></lucide-icon> {{ job.company }}</span>
                      <span class="flex items-center gap-1"><lucide-icon name="map-pin" class="w-3 h-3"></lucide-icon> {{ job.location }}</span>
                    </div>
                  </div>
                </div>
                
                <div class="flex items-center gap-3 w-full sm:w-auto z-10">
                  <span class="text-green-600 bg-green-50 px-2 py-1 rounded text-xs font-semibold mr-2">
                    {{ job.salary || 'Competitive' }}
                  </span>
                  <button (click)="removeBookmark(job.jobId || job['id'], $event)" class="w-9 h-9 flex items-center justify-center text-primary bg-primary-light border border-primary hover:bg-red-50 hover:text-red-500 hover:border-red-200 rounded-md transition-colors" title="Remove Bookmark">
                    <lucide-icon name="bookmark-minus" class="w-4 h-4"></lucide-icon>
                  </button>
                </div>
              </div>
            }
          </div>
        }
      </div>
    </div>
  `
})
export class BookmarksComponent implements OnInit {
  private api = inject(ApiService);
  private toast = inject(ToastService);

  bookmarkedJobs = signal<Job[]>([]);
  loading = signal(false);

  ngOnInit(): void {
    this.loadBookmarks();
  }

  loadBookmarks(): void {
    this.loading.set(true);
    this.api.getBookmarks().subscribe({
      next: (res: any) => { 
        const bookmarks = Array.isArray(res) ? res : (res.data || []);
        if (bookmarks.length === 0) {
           this.bookmarkedJobs.set([]);
           this.loading.set(false);
           return;
        }

        const jobRequests = bookmarks.map((b: any) => {
          const id = b.jobId || b.id;
          return this.api.getJobById(id).pipe(
            map(r => r.data as Job),
            catchError(() => of(null))
          );
        });

        forkJoin(jobRequests).subscribe({
          next: (jobs: unknown) => {
            const resolved = (jobs as Array<Job | null>).filter((j): j is Job => !!j);
            this.bookmarkedJobs.set(resolved);
            this.loading.set(false);
          },
          error: () => {
            this.bookmarkedJobs.set([]);
            this.toast.error('Failed to resolve bookmark details');
            this.loading.set(false);
          }
        });
      },
      error: () => { 
        this.bookmarkedJobs.set([]);
        this.toast.error('Failed to load saved jobs'); 
        this.loading.set(false); 
      }
    });
  }

  removeBookmark(jobId: string, event: Event): void {
    event.stopPropagation();
    event.preventDefault();
    this.api.removeBookmark(jobId).subscribe({
      next: () => {
        this.bookmarkedJobs.update(jobs => jobs.filter(j => j.jobId !== jobId));
        this.toast.success('Removed from saved jobs');
      },
      error: () => this.toast.error('Failed to remove bookmark')
    });
  }
}
