import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { LucideAngularModule } from 'lucide-angular';
import { ApiService } from '../../../core/services/api.service';
import { AuthService } from '../../../core/services/auth.service';
import { Job } from '../../../shared/models/models';
import { ScrollRevealDirective } from '../../../shared/directives/scroll-reveal.directive';
import { CountUpDirective } from '../../../shared/directives/count-up.directive';
import { TiltDirective } from '../../../shared/directives/tilt.directive';
import { RippleDirective } from '../../../shared/directives/ripple.directive';

// Icon map — purely presentational, driven by category name from DB
const CATEGORY_ICONS: Record<string, string> = {
  'Engineering':  '💻',
  'Design':       '🎨',
  'Marketing':    '📈',
  'Business':     '💼',
  'Finance':      '🏦',
  'Support':      '📞',
  'Healthcare':   '🏥',
  'Content':      '📝',
  'Intern':       '🎓',
};

function iconFor(category: string): string {
  return CATEGORY_ICONS[category] ?? '🗂️';
}

@Component({
  selector: 'app-landing',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule, LucideAngularModule, ScrollRevealDirective, CountUpDirective, TiltDirective, RippleDirective],
  template: `
    <div class="bg-gray-50 min-h-screen">
      <!-- Hero Section with Animated Blobs -->
      <section class="bg-vibrant-gradient w-full py-24 text-center relative overflow-hidden">
        <div class="absolute inset-0 bg-[url('https://www.transparenttextures.com/patterns/cubes.png')] opacity-20 mix-blend-overlay"></div>
        <!-- Animated blobs -->
        <div class="absolute top-10 left-10 w-72 h-72 bg-purple-500/30 rounded-full blur-3xl animate-blob pointer-events-none"></div>
        <div class="absolute bottom-10 right-10 w-96 h-96 bg-blue-500/30 rounded-full blur-3xl animate-blob-delay pointer-events-none"></div>
        <div class="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-80 h-80 bg-pink-500/20 rounded-full blur-3xl animate-blob-slow pointer-events-none"></div>
        <div class="relative max-w-4xl mx-auto px-4 z-10">
          <h1 class="text-5xl md:text-7xl font-black text-white mb-6 tracking-tighter drop-shadow-lg leading-tight animate-fade-in">
            Find Your <br class="md:hidden" />
            <span class="text-gradient-animated inline-block">Dream Career</span>
          </h1>
          <p class="text-xl md:text-2xl text-white/90 mb-12 font-medium tracking-wide animate-slide-up" style="animation-delay:150ms">
            @if (loadingStats()) {
              <span class="animate-pulse">Searching opportunities...</span>
            } @else {
              Join the future. Browse {{ totalJobs() }}+ live roles.
            }
          </p>

          <div class="glass-dark p-3 rounded-[2rem] max-w-3xl mx-auto flex flex-col md:flex-row gap-2 transition-all hover:scale-[1.02] duration-500 animate-slide-up" style="animation-delay:300ms">
            <div class="flex-1 bg-white/10 rounded-2xl flex items-center px-5 py-3 border border-white/20 hover:bg-white/15 transition-colors">
              <lucide-icon name="search" class="w-5 h-5 text-white/70"></lucide-icon>
              <input type="text" [(ngModel)]="searchKeyword" placeholder="Job title, keywords..." class="w-full border-none px-4 py-2 text-base focus:outline-none focus:ring-0 text-white bg-transparent placeholder-white/60 font-medium" />
            </div>
            <div class="hidden md:flex flex-1 bg-white/10 rounded-2xl items-center px-5 py-3 border border-white/20 hover:bg-white/15 transition-colors">
              <lucide-icon name="map-pin" class="w-5 h-5 text-white/70"></lucide-icon>
              <input type="text" [(ngModel)]="searchLocation" placeholder="Location or Remote..." class="w-full border-none px-4 py-2 text-base focus:outline-none focus:ring-0 text-white bg-transparent placeholder-white/60 font-medium" />
            </div>
            <a [routerLink]="['/jobs']" [queryParams]="{ keyword: searchKeyword || null, location: searchLocation || null }"
              appRipple
              class="bg-white hover:bg-gray-100 text-primary px-8 py-3 rounded-2xl font-bold text-base transition-all hover:scale-105 whitespace-nowrap flex items-center justify-center shadow-[0_0_20px_rgba(255,255,255,0.3)] hover:shadow-[0_0_30px_rgba(255,255,255,0.5)]">
              Search Jobs
            </a>
          </div>

          <p class="inline-flex items-center justify-center bg-white/90 border border-white/70 text-slate-900 text-sm md:text-base mt-7 px-4 py-2 rounded-full font-bold shadow-sm animate-fade-in" style="animation-delay:450ms">
            @if (!loadingStats()) {
              {{ totalJobs() }} live jobs &nbsp;•&nbsp; {{ openJobs() }} actively hiring
            } @else {
              <span class="inline-block animate-pulse">Loading live stats...</span>
            }
          </p>
        </div>
      </section>

      <!-- Live Stats Bar with Count-Up Animation -->
      <section class="bg-white py-12 border-b border-gray-200 relative overflow-hidden">
        <!-- Subtle gradient overlay -->
        <div class="absolute inset-0 bg-gradient-to-r from-primary/5 via-transparent to-secondary/5 pointer-events-none"></div>
        <div class="max-w-5xl mx-auto flex flex-col md:flex-row justify-around gap-12 text-center px-4 relative z-10">
          <div class="flex flex-col gap-2" appScrollReveal>
            <span class="text-4xl font-extrabold text-gray-900">
              @if (loadingStats()) { <span class="animate-pulse text-gray-400">—</span> } @else { <span appCountUp [target]="totalJobs()" [suffix]="'+'"></span> }
            </span>
            <span class="text-sm text-gray-600 font-semibold uppercase tracking-wider">Total Jobs</span>
          </div>
          <div class="flex flex-col gap-2" appScrollReveal [delay]="100">
            <span class="text-4xl font-extrabold text-gray-900">
              @if (loadingStats()) { <span class="animate-pulse text-gray-400">—</span> } @else { <span appCountUp [target]="openJobs()" [duration]="2000"></span> }
            </span>
            <span class="text-sm text-gray-600 font-semibold uppercase tracking-wider">Open Positions</span>
          </div>
          <div class="flex flex-col gap-2" appScrollReveal [delay]="200">
            <span class="text-4xl font-extrabold text-gray-900">
              @if (loadingStats()) { <span class="animate-pulse text-gray-400">—</span> } @else { <span appCountUp [target]="categoryCount()" [duration]="2500"></span> }
            </span>
            <span class="text-sm text-gray-600 font-semibold uppercase tracking-wider">Job Categories</span>
          </div>
        </div>
      </section>

      <!-- Job Categories — live counts from search-service DB -->
      <section class="py-16 max-w-7xl mx-auto px-6">
        <h2 class="section-heading mb-8 text-center" appScrollReveal>Browse by Category</h2>
        @if (loadingCategories()) {
          <div class="grid grid-cols-2 md:grid-cols-4 gap-4">
            @for (sk of [1,2,3,4,5,6,7,8]; track sk) {
              <div class="bg-white border border-gray-100 rounded-xl p-6 text-center animate-pulse">
                <div class="w-12 h-12 rounded-full bg-gray-200 mx-auto mb-4"></div>
                <div class="h-4 bg-gray-200 rounded w-24 mx-auto mb-2"></div>
                <div class="h-3 bg-gray-100 rounded w-16 mx-auto"></div>
              </div>
            }
          </div>
        } @else if (categories().length === 0) {
          <p class="text-center text-gray-500 text-sm py-12">No categories available yet. Post some jobs to see them here.</p>
        } @else {
          <div class="grid grid-cols-2 md:grid-cols-4 gap-4">
            @for (cat of categories(); track cat.label; let i = $index) {
              <a routerLink="/jobs" [queryParams]="{ category: cat.label }"
                appScrollReveal [delay]="i < 8 ? i * 60 : 0" appTilt [maxTilt]="4"
                class="bg-white border border-gray-200 rounded-2xl p-6 flex flex-col items-center justify-center aspect-square text-center hover:shadow-xl hover:-translate-y-2 transition-all duration-300 group relative overflow-hidden">
                <div class="absolute inset-0 bg-gradient-to-br from-primary/5 to-transparent opacity-0 group-hover:opacity-100 transition-opacity duration-500 rounded-2xl"></div>
                <div class="w-16 h-16 rounded-3xl bg-gradient-to-br from-primary/10 to-secondary/10 text-primary flex items-center justify-center mx-auto mb-5 text-3xl group-hover:scale-110 group-hover:from-primary group-hover:to-secondary group-hover:text-white transition-all duration-300 shadow-sm group-hover:shadow-lg relative z-10">
                  {{ cat.icon }}
                </div>
                <h3 class="font-bold text-gray-900 text-lg group-hover:text-primary transition-colors tracking-tight relative z-10">{{ cat.label }}</h3>
                <p class="text-sm font-semibold text-gray-600 mt-2 bg-gray-50 px-3 py-1.5 rounded-full group-hover:bg-primary/10 group-hover:text-primary transition-colors border border-gray-200 group-hover:border-primary/20 relative z-10">{{ cat.count }} Live Roles</p>
              </a>
            }
          </div>
        }
      </section>

      <!-- Featured Jobs (real data, latest 3 OPEN) -->
      <section class="py-16 max-w-7xl mx-auto px-6 bg-gray-50">
        <div class="flex justify-between items-end mb-8 block md:flex text-center md:text-left">
          <h2 class="section-heading" appScrollReveal>Featured Jobs</h2>
          <a routerLink="/jobs" class="text-sm font-semibold text-primary hover:text-primary-hover mt-4 md:mt-0 animated-underline">View all jobs &rarr;</a>
        </div>

        @if (loadingStats()) {
          <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            @for (sk of [1,2,3]; track sk) {
              <div class="card animate-pulse">
                <div class="flex items-start justify-between mb-4">
                  <div class="w-10 h-10 bg-gray-200 rounded-full"></div>
                  <div class="h-6 bg-gray-200 rounded-full w-20"></div>
                </div>
                <div class="h-5 bg-gray-200 rounded w-3/4 mb-2"></div>
                <div class="h-3 bg-gray-100 rounded w-1/2 mb-4"></div>
                <div class="flex gap-2 mb-4">
                  <div class="h-6 bg-gray-100 rounded w-16"></div>
                  <div class="h-6 bg-gray-100 rounded w-16"></div>
                </div>
                <div class="border-t border-gray-100 pt-4 flex justify-between">
                  <div class="h-4 bg-gray-200 rounded w-28"></div>
                  <div class="h-8 bg-gray-200 rounded-lg w-16"></div>
                </div>
              </div>
            }
          </div>
        } @else {
          <div class="bento-grid">
            @for (job of featuredJobs(); track job.jobId; let i = $index) {
              <div appScrollReveal [delay]="i * 80" appTilt [maxTilt]="5"
                class="bg-white border border-gray-200 rounded-2xl p-6 flex flex-col justify-between group hover:shadow-2xl transition-all duration-300 hover:-translate-y-2 relative overflow-hidden">
                <div class="absolute -right-12 -top-12 w-32 h-32 bg-primary/5 rounded-full blur-3xl group-hover:bg-primary/10 transition-colors duration-700"></div>
                
                <div>
                  <div class="flex items-center justify-between mb-6 relative z-10">
                    <div class="w-14 h-14 bg-gradient-to-br from-gray-50 to-gray-100 border border-gray-200 shadow-sm rounded-2xl flex items-center justify-center font-black text-gray-600 text-base group-hover:border-primary/30 group-hover:scale-110 group-hover:shadow-md transition-all duration-300">
                      {{ job.company ? job.company.substring(0,2).toUpperCase() : 'CO' }}
                    </div>
                    <span class="px-3 py-1.5 bg-green-50 text-green-700 text-xs font-bold rounded-full border border-green-200 shadow-sm flex items-center gap-1">
                      <span class="w-1.5 h-1.5 bg-green-500 rounded-full animate-pulse"></span>
                      Actively Hiring
                    </span>
                  </div>
                  
                  <h3 class="text-xl font-bold text-gray-900 mb-2 group-hover:text-primary transition-colors tracking-tight relative z-10 line-clamp-2">{{ job.title }}</h3>
                  <p class="text-sm font-semibold text-gray-600 mb-6 flex items-center gap-2 relative z-10">
                   <lucide-icon name="building-2" class="w-4 h-4 text-gray-400"></lucide-icon> {{ job.company || 'Company' }}
                  </p>
                  
                  <div class="flex gap-2 flex-wrap mb-6 relative z-10">
                    <span class="bg-gray-50 border border-gray-200 px-3 py-1.5 rounded-lg text-xs text-gray-700 font-semibold flex items-center gap-1.5 hover:border-primary/30 hover:bg-primary/5 transition-colors">
                      <lucide-icon name="map-pin" class="w-3.5 h-3.5 text-gray-500"></lucide-icon> {{ job.location }}
                    </span>
                    @if (job.salary) {
                      <span class="bg-green-50 border border-green-200 px-3 py-1.5 rounded-lg text-xs text-green-700 font-bold flex items-center gap-1.5 hover:border-green-300 hover:bg-green-100 transition-colors">
                        <lucide-icon name="banknote" class="w-3.5 h-3.5 text-green-600"></lucide-icon> {{ job.salary }}
                      </span>
                    }
                  </div>
                </div>
                
                <div class="flex justify-between items-center mt-auto pt-6 border-t border-gray-200 relative z-10 group-hover:border-primary/20 transition-colors">
                  <span class="text-xs font-medium text-gray-500 flex items-center gap-1.5">
                    <lucide-icon name="clock" class="w-3.5 h-3.5"></lucide-icon> {{ job.createdAt | date:'mediumDate' }}
                  </span>
                  @if (isRecruiter()) {
                    <a [routerLink]="['/jobs', job.jobId]" appRipple
                      class="bg-gray-100 hover:bg-gray-900 text-gray-900 hover:text-white px-5 py-2.5 rounded-xl text-sm font-bold transition-all flex items-center gap-2 shadow-sm hover:shadow-md">
                      View Details <lucide-icon name="arrow-right" class="w-4 h-4"></lucide-icon>
                    </a>
                  } @else {
                    <a [routerLink]="['/jobs', job.jobId]" appRipple
                      class="bg-gradient-to-r from-primary to-secondary hover:from-primary-hover hover:to-primary text-white px-5 py-2.5 rounded-xl text-sm font-bold transition-all shadow-md hover:shadow-xl hover:-translate-y-0.5 flex items-center gap-2">
                      Apply Now <lucide-icon name="arrow-right" class="w-4 h-4"></lucide-icon>
                    </a>
                  }
                </div>
              </div>
            }
            @if (featuredJobs().length === 0) {
              <div class="col-span-3 text-center py-12 text-gray-400 text-sm">No open jobs available right now.</div>
            }
          </div>
        }
      </section>

      <!-- CTA Banner — role-aware with animated gradient -->
      <section class="max-w-5xl mx-auto px-6 mb-20 relative transition-transform hover:-translate-y-0.5" appScrollReveal>
        @if (isRecruiter()) {
          <div class="bg-animated-gradient text-white rounded-2xl mx-auto py-12 px-8 text-center shadow-md relative overflow-hidden group border border-transparent hover:border-primary-light noise-overlay">
            <div class="absolute -right-20 -top-20 w-64 h-64 bg-white/10 rounded-full blur-2xl group-hover:scale-110 transition-transform duration-700"></div>
            <h2 class="text-2xl font-bold mb-3 relative z-10 text-white tracking-tight">Ready to find your next hire?</h2>
            <p class="text-white/80 font-medium mb-6 relative z-10 text-sm">Post a new job and start reviewing applicants today.</p>
            <div class="flex items-center justify-center gap-4 relative z-10">
              <a routerLink="/recruiter/post-job" appRipple class="inline-block bg-white text-primary font-bold text-sm px-6 py-3 rounded-xl shadow-sm hover:bg-gray-50 hover:shadow-md transition-all border border-gray-100 hover-lift">Post a Job &rarr;</a>
              <a routerLink="/recruiter/dashboard" class="inline-block bg-white/10 text-white font-bold text-sm px-6 py-3 rounded-xl hover:bg-white/20 transition-all border border-white/20">Go to Dashboard</a>
            </div>
          </div>
        } @else if (isJobSeeker()) {
          <div class="bg-vibrant-gradient text-white rounded-2xl mx-auto py-12 px-8 text-center shadow-md relative overflow-hidden group noise-overlay">
            <div class="absolute -right-20 -top-20 w-64 h-64 bg-white/10 rounded-full blur-2xl group-hover:scale-110 transition-transform duration-700"></div>
            <h2 class="text-2xl font-bold mb-3 relative z-10 text-white tracking-tight">Your next opportunity is waiting</h2>
            <p class="text-white/80 font-medium mb-6 relative z-10 text-sm">Browse {{ openJobs() }} open positions and apply in seconds.</p>
            <a routerLink="/jobs" appRipple class="inline-block bg-white text-primary font-bold text-sm px-6 py-3 rounded-xl shadow-sm hover:bg-gray-50 hover:shadow-md transition-all z-10 relative border border-gray-100 hover-lift">Browse All Jobs &rarr;</a>
          </div>
        } @else {
          <div class="bg-animated-gradient text-white rounded-2xl mx-auto py-12 px-8 text-center shadow-md relative overflow-hidden group border border-transparent hover:border-primary-light noise-overlay">
            <div class="absolute -right-20 -top-20 w-64 h-64 bg-white/10 rounded-full blur-2xl group-hover:scale-110 transition-transform duration-700"></div>
            <h2 class="text-2xl font-bold mb-3 relative z-10 text-white tracking-tight">Are you a Recruiter?</h2>
            <p class="text-white/80 font-medium mb-6 relative z-10 text-sm">Post jobs and find the best talent in minutes.</p>
            <a routerLink="/register" appRipple class="inline-block bg-white text-primary font-bold text-sm px-6 py-3 rounded-xl shadow-sm hover:bg-gray-50 hover:shadow-md transition-all z-10 relative border border-gray-100 hover-lift">Get Started Free &rarr;</a>
          </div>
        }
      </section>
    </div>
  `
})
export class Landing implements OnInit {
  private api = inject(ApiService);
  private auth = inject(AuthService);

  searchKeyword = '';
  searchLocation = '';
  private allJobs = signal<Job[]>([]);
  loadingStats = signal(true);

  // Categories fetched from DB via search-service
  categories = signal<{ label: string; icon: string; count: number }[]>([]);
  loadingCategories = signal(true);

  // Role helpers
  isRecruiter = () => this.auth.isRecruiter();
  isJobSeeker = () => this.auth.isJobSeeker();

  // ── Computed from real API data ──────────────────────────────────────────
  totalJobs   = computed(() => this.allJobs().length);
  openJobs    = computed(() => this.allJobs().filter(j => j.status === 'OPEN').length);
  categoryCount = computed(() => this.categories().filter(c => c.count > 0).length);
  featuredJobs  = computed(() =>
    this.allJobs()
      .filter(j => j.status === 'OPEN')
      .sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime())
      .slice(0, 3)
  );

  ngOnInit(): void {
    // Single call — getJobs() returns category on every job from the jobs-service DB.
    // We derive category counts from that data, so no separate search-service call needed.
    this.api.getJobs().subscribe({
      next: (res) => {
        const jobs = res.data ?? [];
        this.allJobs.set(jobs);
        this.loadingStats.set(false);

        // Build category counts from the jobs data — fully DB-driven, zero hardcoding.
        // job.category is set by JobService.resolveCategory() and stored in the jobs DB.
        const openJobs = jobs.filter((j: Job) => j.status === 'OPEN');
        const countMap = new Map<string, number>();
        for (const job of openJobs) {
          const cat = this.resolveCategory(job);
          countMap.set(cat, (countMap.get(cat) ?? 0) + 1);
        }
        const cats = Array.from(countMap.entries())
          .map(([label, count]) => ({ label, icon: iconFor(label), count }))
          .sort((a, b) => b.count - a.count);
        this.categories.set(cats);
        this.loadingCategories.set(false);
      },
      error: () => {
        this.loadingStats.set(false);
        this.loadingCategories.set(false);
      }
    });
  }

  /**
   * Resolves a job's category.
   * Priority: job.category from DB → keyword fallback on title+description.
   * The keyword fallback mirrors JobService.resolveCategory() on the backend,
   * so it only fires for legacy jobs posted before the category column existed.
   */
  private resolveCategory(job: Job): string {
    if (job.category?.trim()) return job.category.trim();
    const text = `${job.title ?? ''} ${job.description ?? ''}`.toLowerCase();
    if (/intern|internship|trainee|graduate/.test(text))                          return 'Intern';
    if (/design|ui|ux|graphic|figma|visual/.test(text))                           return 'Design';
    if (/marketing|seo|brand|campaign|social media|advertising/.test(text))       return 'Marketing';
    if (/finance|accounting|banking|auditor|tax|fintech/.test(text))              return 'Finance';
    if (/health|medical|nurse|doctor|pharma|clinical|biotech/.test(text))         return 'Healthcare';
    if (/support|helpdesk|customer success|service desk|call center/.test(text))  return 'Support';
    if (/content|writer|copywriter|editor|journalist/.test(text))                 return 'Content';
    if (/business|operations|analyst|manager|strategy|recruiter/.test(text))      return 'Business';
    return 'Engineering';
  }
}
