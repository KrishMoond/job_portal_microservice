import { Component, OnInit, inject, signal, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink, ActivatedRoute, Router } from '@angular/router';
import { ApiService } from '../../../core/services/api.service';
import { AuthService } from '../../../core/services/auth.service';
import { ToastService } from '../../../core/services/toast.service';
import { Job } from '../../../shared/models/models';
import { LucideAngularModule } from 'lucide-angular';
import { Subject, takeUntil } from 'rxjs';
import { map } from 'rxjs/operators';
import { ScrollRevealDirective } from '../../../shared/directives/scroll-reveal.directive';
import { RippleDirective } from '../../../shared/directives/ripple.directive';

@Component({
  selector: 'app-job-list',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, LucideAngularModule, ScrollRevealDirective, RippleDirective],
  template: `
    <div class="min-h-screen bg-gray-50 flex flex-col">
      <!-- Top Search Bar - Full Width Hero -->
      <div class="w-full bg-animated-gradient relative overflow-hidden py-12 shadow-lg">
        <!-- Animated mesh gradient blobs -->
        <div class="absolute top-0 left-0 w-96 h-96 bg-white/10 rounded-full blur-3xl animate-blob"></div>
        <div class="absolute bottom-0 right-0 w-96 h-96 bg-purple-500/20 rounded-full blur-3xl animate-blob-delay"></div>
        <div class="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[500px] h-[500px] bg-blue-500/15 rounded-full blur-3xl animate-blob-slow"></div>
        
        <div class="max-w-7xl mx-auto px-6 relative z-10">
          <h1 class="text-4xl font-black text-white mb-3 tracking-tight">Find Your Dream Job</h1>
          <p class="text-white/80 font-medium mb-8 text-lg">Discover opportunities from top companies worldwide</p>
          
          <div class="flex flex-col md:flex-row gap-4">
            <div class="flex-1 relative">
              <lucide-icon name="search" class="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400"></lucide-icon>
              <input [(ngModel)]="keyword" (keyup.enter)="submitSearch()" placeholder="Keywords, job title, company..."
                class="w-full pl-12 pr-4 py-4 border-2 border-white/20 rounded-xl text-base focus:ring-2 focus:ring-white/50 focus:outline-none transition-all bg-white/95 backdrop-blur-sm shadow-lg font-medium" 
                style="min-height: 56px;" />
            </div>
            <div class="md:w-72 relative">
              <lucide-icon name="map-pin" class="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400"></lucide-icon>
              <input [(ngModel)]="location" (keyup.enter)="submitSearch()" placeholder="City, state, remote..."
                class="w-full pl-12 pr-4 py-4 border-2 border-white/20 rounded-xl text-base focus:ring-2 focus:ring-white/50 focus:outline-none transition-all bg-white/95 backdrop-blur-sm shadow-lg font-medium" 
                style="min-height: 56px;" />
            </div>
            <button (click)="submitSearch()" class="btn-primary py-4 px-8 whitespace-nowrap flex items-center justify-center gap-2 shadow-xl text-base font-bold" 
                    style="min-height: 56px;">
              <lucide-icon name="search" class="w-5 h-5"></lucide-icon> Search Jobs
            </button>
          </div>
          
          @if (recentSearches.length > 0) {
            <div class="mt-6 flex items-center gap-3">
              <span class="text-xs text-white/70 font-medium whitespace-nowrap">Recent:</span>
              <div class="flex flex-wrap gap-2">
                @for (term of recentSearches; track term) {
                  <button (click)="applyRecentSearch(term)" class="px-3 py-1.5 text-xs font-medium text-white/90 bg-white/10 border border-white/20 rounded-lg hover:bg-white/20 transition-all shadow-sm backdrop-blur-sm">
                    {{ term }}
                  </button>
                }
              </div>
              <button (click)="clearRecentSearches()" class="text-xs text-white/70 hover:text-white hover:underline ml-auto">Clear history</button>
            </div>
          }
        </div>
      </div>

      <!-- Main Content -->
      <div class="max-w-7xl mx-auto px-6 py-8 w-full flex-grow grid grid-cols-1 lg:grid-cols-[280px_1fr] gap-6">
        
        <!-- Mobile Filter Toggle -->
        <div class="lg:hidden flex items-center justify-between bg-white rounded-xl border border-gray-200 p-4 shadow-sm">
          <div>
            <p class="text-sm font-semibold text-gray-900">Filters</p>
            <p class="text-xs text-gray-500">{{ activeFilterCount() }} active</p>
          </div>
          <button type="button" (click)="showMobileFilters.set(true)"
            class="btn-secondary py-2 px-4 text-sm flex items-center gap-2">
            <lucide-icon name="filter" class="w-4 h-4"></lucide-icon>
            Refine
          </button>
        </div>

        <!-- Mobile Filter Drawer -->
        @if (showMobileFilters()) {
          <div class="fixed inset-0 z-[70] lg:hidden">
            <button type="button" class="absolute inset-0 bg-black/40" aria-label="Close filters" (click)="showMobileFilters.set(false)"></button>
            <section class="absolute inset-x-0 bottom-0 max-h-[85vh] overflow-y-auto bg-white rounded-t-2xl border-t border-gray-200 shadow-2xl p-5 animate-slide-up">
              <div class="flex items-center justify-between mb-5">
                <div>
                  <h3 class="font-bold text-gray-900">Filters</h3>
                  <p class="text-xs text-gray-500">{{ filteredJobs.length }} matching jobs</p>
                </div>
                <button type="button" (click)="showMobileFilters.set(false)"
                  class="w-9 h-9 rounded-lg border border-gray-200 flex items-center justify-center text-gray-500 hover:text-gray-900 hover:bg-gray-50"
                  aria-label="Close filters">
                  <lucide-icon name="x" class="w-4 h-4"></lucide-icon>
                </button>
              </div>

              <div class="space-y-6">
                <div>
                  <h4 class="text-sm font-medium text-gray-900 mb-3">Job Type</h4>
                  <div class="grid grid-cols-1 gap-2">
                    <label class="checkbox-pill" [class.active]="filters.fullTime">
                      <input type="checkbox" [(ngModel)]="filters.fullTime" (change)="applyLocalFilters()" class="sr-only" />
                      Full-time
                    </label>
                    <label class="checkbox-pill" [class.active]="filters.contract">
                      <input type="checkbox" [(ngModel)]="filters.contract" (change)="applyLocalFilters()" class="sr-only" />
                      Contract
                    </label>
                    <label class="checkbox-pill" [class.active]="filters.remote">
                      <input type="checkbox" [(ngModel)]="filters.remote" (change)="applyLocalFilters()" class="sr-only" />
                      Remote
                    </label>
                  </div>
                </div>

                <div>
                  <h4 class="text-sm font-medium text-gray-900 mb-3">Tech Stack</h4>
                  <div class="flex flex-wrap gap-2">
                    @for (tech of techOptions; track tech) {
                      <button type="button" (click)="toggleTechStack(tech)"
                        [class.active]="filters.techStack.has(tech)"
                        class="checkbox-pill">
                        {{ tech }}
                      </button>
                    }
                  </div>
                </div>

                <div>
                  <h4 class="text-sm font-medium text-gray-900 mb-3">Minimum Salary / Year ($)</h4>
                  <input type="range" [(ngModel)]="filters.minSalary" (input)="applyLocalFilters()" min="0" max="250" step="10" class="w-full accent-primary" />
                  <div class="flex justify-between text-xs text-gray-500 mt-1 font-medium">
                    <span>$0</span>
                    <span>$250k+</span>
                  </div>
                  @if (filters.minSalary > 0) {
                    <p class="text-xs text-primary font-bold text-center mt-2">&gt; \${{ filters.minSalary }}k</p>
                  }
                </div>
              </div>

              <div class="sticky bottom-0 bg-white pt-5 mt-6 border-t border-gray-100 flex gap-3">
                <button type="button" (click)="clearFilters(); showMobileFilters.set(false)" class="btn-secondary flex-1 py-3">Clear</button>
                <button type="button" (click)="showMobileFilters.set(false)" class="btn-primary flex-1 py-3">Show Jobs</button>
              </div>
            </section>
          </div>
        }

        <!-- Left: Filters Panel -->
        <aside class="bg-white rounded-2xl border border-gray-200 p-5 sticky top-20 h-fit hidden lg:block shadow-sm">
          <div class="flex items-center justify-between mb-4">
            <h3 class="font-semibold text-gray-900">Filters</h3>
            <button (click)="clearFilters()" class="text-xs text-primary hover:text-primary-hover font-medium">Clear All</button>
          </div>
          
          <div class="space-y-6">
            <!-- Job Type -->
            <div>
              <h4 class="text-sm font-medium text-gray-900 mb-3">Job Type</h4>
              <div class="space-y-2">
                <label class="flex items-center gap-2 cursor-pointer group">
                  <input type="checkbox" [(ngModel)]="filters.fullTime" (change)="applyLocalFilters()" class="rounded border-gray-300 text-primary focus:ring-primary" />
                  <span class="text-sm text-gray-600 group-hover:text-gray-900 transition-colors">Full-time</span>
                </label>
                <label class="flex items-center gap-2 cursor-pointer group">
                  <input type="checkbox" [(ngModel)]="filters.contract" (change)="applyLocalFilters()" class="rounded border-gray-300 text-primary focus:ring-primary" />
                  <span class="text-sm text-gray-600 group-hover:text-gray-900 transition-colors">Contract</span>
                </label>
                <label class="flex items-center gap-2 cursor-pointer group">
                  <input type="checkbox" [(ngModel)]="filters.remote" (change)="applyLocalFilters()" class="rounded border-gray-300 text-primary focus:ring-primary" />
                  <span class="text-sm text-gray-600 group-hover:text-gray-900 transition-colors">Remote</span>
                </label>
              </div>
            </div>

            <hr class="border-gray-100" />

            <!-- Experience -->
            <div>
              <h4 class="text-sm font-medium text-gray-900 mb-3">Tech Stack</h4>
              <div class="flex flex-wrap gap-2">
                @for (tech of techOptions; track tech) {
                  <button type="button" (click)="toggleTechStack(tech)"
                        [class.bg-primary-light]="filters.techStack.has(tech)"
                        [class.border-primary]="filters.techStack.has(tech)"
                        [class.text-primary]="filters.techStack.has(tech)"
                        [class.bg-gray-100]="!filters.techStack.has(tech)"
                        [class.border-gray-200]="!filters.techStack.has(tech)"
                        [class.text-gray-700]="!filters.techStack.has(tech)"
                        class="px-2.5 py-1 border rounded-md text-xs font-medium cursor-pointer hover:border-gray-300 transition-colors">
                    {{ tech }}
                  </button>
                }
              </div>
            </div>

            <hr class="border-gray-100" />

            <!-- Salary Range -->
            <div>
              <h4 class="text-sm font-medium text-gray-900 mb-3">Minimum Salary / Year ($)</h4>
              <input type="range" [(ngModel)]="filters.minSalary" (input)="applyLocalFilters()" min="0" max="250" step="10" class="w-full accent-primary" />
              <div class="flex justify-between text-xs text-gray-500 mt-1 font-medium">
                <span>$0</span>
                <span>$250k+</span>
              </div>
              @if (filters.minSalary > 0) {
                <p class="text-xs text-primary font-bold text-center mt-2">&gt; \${{ filters.minSalary }}k</p>
              }
            </div>
          </div>
        </aside>

        <!-- Right: Results -->
        <main>
          <!-- Active Filter Pills -->
          @if (keyword || location || category || activeFilterCount() > 0) {
          <div class="flex flex-wrap items-center gap-2 mb-4">
            <span class="text-xs text-gray-500 mr-1">Active Search:</span>
            @if (category) {
               <span class="inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-medium bg-indigo-50 text-indigo-700 border border-indigo-200">
                 Category: {{ category }}
                 <button (click)="clearFilters()" class="hover:text-indigo-900 focus:outline-none"><lucide-icon name="x" class="w-3 h-3"></lucide-icon></button>
               </span>
            }
            @if (keyword) {
               <span class="inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-medium bg-primary-light text-primary border border-primary/20">
                 Keyword: {{ keyword }} 
                 <button (click)="removeKeyword()" class="hover:text-primary-hover focus:outline-none"><lucide-icon name="x" class="w-3 h-3"></lucide-icon></button>
               </span>
            }
            @if (location) {
               <span class="inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-medium bg-primary-light text-primary border border-primary/20">
                 Location: {{ location }} 
                 <button (click)="removeLocation()" class="hover:text-primary-hover focus:outline-none"><lucide-icon name="x" class="w-3 h-3"></lucide-icon></button>
               </span>
            }
            @for (filter of activeFilterLabels(); track filter) {
               <span class="inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-medium bg-gray-100 text-gray-700 border border-gray-200">
                 {{ filter }}
               </span>
            }
            <button (click)="clearFilters()" class="text-xs text-gray-500 hover:text-gray-700 underline font-medium ml-2">Clear all</button>
          </div>
          }

          <!-- Recommendations Carousel (Backend Integrated) -->
          @if (isJobSeeker && recommendedJobs.length > 0) {
            <div class="mb-8 p-6 bg-gradient-to-r from-primary-light via-white to-primary-light rounded-3xl border border-primary/10 shadow-sm animate-fade-in relative overflow-hidden">
               <div class="absolute -top-10 -right-10 w-32 h-32 bg-primary/10 rounded-full blur-2xl"></div>
               <h3 class="font-bold text-gray-900 text-lg mb-4 flex items-center gap-2 relative z-10"><lucide-icon name="star" class="w-5 h-5 text-yellow-500 fill-yellow-500"></lucide-icon> Recommended for You</h3>
               
               <div class="flex gap-4 overflow-x-auto pb-4 snap-x relative z-10 scrollbar-hide">
                 @for (rec of recommendedJobs; track rec.jobId) {
                   <a [routerLink]="['/jobs', rec.jobId || rec['id']]" class="min-w-[280px] bg-white p-5 rounded-2xl border border-gray-100 shadow-[0_4px_20px_-4px_rgba(0,0,0,0.05)] hover:shadow-lg transition-all duration-300 hover:-translate-y-1 block snap-start group">
                     <div class="flex justify-between items-start mb-3">
                       <div class="w-10 h-10 rounded-xl bg-gray-50 border border-gray-100 flex items-center justify-center font-black text-gray-500 transition-colors group-hover:border-primary/20 group-hover:text-primary">
                         {{ rec.company ? rec.company.substring(0,2).toUpperCase() : 'CO' }}
                       </div>
                       <span class="bg-indigo-50 text-primary px-2 py-0.5 rounded text-[10px] font-bold uppercase tracking-wider">Top Match</span>
                     </div>
                     <h4 class="font-bold text-gray-900 line-clamp-1 group-hover:text-primary transition-colors">{{ rec.title }}</h4>
                     <p class="text-xs font-semibold text-gray-500 mt-1 mb-4 flex items-center gap-1"><lucide-icon name="building-2" class="w-3.5 h-3.5"></lucide-icon> {{ rec.company || 'Company' }}</p>
                     
                     <div class="flex gap-2">
                       <span class="bg-gray-50 px-2 py-1 rounded-md text-[11px] font-medium text-gray-600 border border-gray-100">{{ rec.location }}</span>
                       @if (rec.salary) {
                         <span class="bg-green-50 px-2 py-1 rounded-md text-[11px] font-bold text-green-700 border border-green-100">{{ rec.salary }}</span>
                       }
                     </div>
                   </a>
                 }
               </div>
            </div>
          }

          <div class="flex items-center justify-between mb-6 pb-2 border-b border-gray-200">
            <h2 class="text-gray-700 font-medium text-sm">Showing <span class="text-gray-900 font-bold">{{ filteredJobs.length }}</span> {{ filteredJobs.length === 1 ? 'result' : 'results' }}</h2>
            <div class="flex items-center gap-2">
              <span class="text-xs text-gray-500 hidden sm:inline">Sort by:</span>
              <select [(ngModel)]="sortBy" (change)="applyLocalFilters()" class="text-sm border-none bg-transparent font-medium text-gray-900 focus:ring-0 cursor-pointer">
                <option value="Latest">Latest</option>
                <option value="Most Relevant">Most Relevant</option>
                <option value="Highest Salary">Highest Salary</option>
              </select>
            </div>
          </div>

          @if (loading()) {
            <!-- Skeleton Loaders -->
            <div class="space-y-4">
              @for (item of [1,2,3,4]; track item) {
                <div class="card p-5 flex flex-col sm:flex-row gap-5 animate-pulse border border-gray-100 shadow-none">
                  <div class="flex-shrink-0 w-14 h-14 rounded-xl bg-gray-200"></div>
                  <div class="flex-1 space-y-4 py-1">
                    <div class="flex justify-between">
                      <div class="h-5 bg-gray-200 rounded w-1/3"></div>
                      <div class="h-5 bg-gray-100 rounded w-20"></div>
                    </div>
                    <div class="flex gap-4">
                      <div class="h-3 bg-gray-200 rounded w-24"></div>
                      <div class="h-3 bg-gray-200 rounded w-24"></div>
                    </div>
                    <div class="h-8 bg-gray-100 rounded w-full mt-2"></div>
                  </div>
                </div>
              }
            </div>
          } @else if (filteredJobs.length === 0) {
            <div class="text-center py-20 bg-white rounded-2xl border border-gray-200 shadow-sm">
              <lucide-icon name="search-x" class="w-16 h-16 text-gray-300 mx-auto mb-4"></lucide-icon>
              <h3 class="text-lg font-semibold text-gray-900 mb-1">No jobs found</h3>
              <p class="text-gray-500 text-sm">Try adjusting your filters or search terms.</p>
              <button (click)="clearFilters()" class="mt-4 text-primary font-medium hover:underline text-sm">Clear filters</button>
            </div>
          } @else {
            <div class="space-y-4">
              @for (job of paginatedJobs(); track job.jobId || job['id']; let i = $index) {
                <div appScrollReveal [delay]="i * 60"
                  class="card p-6 group flex flex-col sm:flex-row gap-5 cursor-pointer relative overflow-hidden hover-lift card-shine glow-card border-l-4"
                  [class.border-l-primary]="job.location && job.location.toLowerCase().includes('full')"
                  [class.border-l-accent]="job.location && job.location.toLowerCase().includes('remote')"
                  [class.border-l-amber-500]="job.location && !job.location.toLowerCase().includes('full') && !job.location.toLowerCase().includes('remote')"
                  style="transition: transform 0.25s cubic-bezier(0.4,0,0.2,1), box-shadow 0.25s ease">
                  
                  <!-- Logo -->
                  <div class="flex-shrink-0 w-16 h-16 rounded-xl border border-gray-100 flex items-center justify-center bg-white shadow-sm text-gray-400 font-bold text-2xl group-hover:scale-105 transition-transform duration-300 z-10">
                    {{ job.company ? job.company.substring(0,2).toUpperCase() : 'CO' }}
                  </div>
                  
                  <!-- Info -->
                  <div class="flex-1 z-10">
                    <div class="flex justify-between items-start mb-2">
                      <a [routerLink]="['/jobs', job.jobId || job['id']]" class="text-xl font-bold text-gray-900 group-hover:text-primary transition-colors">{{ job.title }}</a>
                      <div class="flex gap-2">
                        @if (job.status === 'CLOSED') {
                          <span class="badge badge-closed shrink-0">Closed</span>
                        } @else {
                          <span class="badge badge-active flex items-center gap-1 shrink-0">
                            <lucide-icon name="zap" class="w-3 h-3 fill-current"></lucide-icon> Actively Hiring
                          </span>
                        }
                      </div>
                    </div>
                    
                    <div class="flex flex-wrap items-center gap-x-4 gap-y-2 text-sm text-gray-500 mb-4 font-medium">
                      <span class="flex items-center gap-1.5 text-gray-700">
                        <lucide-icon name="building-2" class="w-4 h-4"></lucide-icon>
                        {{ job.company }}
                      </span>
                      <span class="flex items-center gap-1.5">
                        <lucide-icon name="map-pin" class="w-4 h-4"></lucide-icon>
                        {{ job.location }}
                      </span>
                      @if (job.salary) {
                        <span class="flex items-center gap-1.5 text-green-600 bg-green-50 px-2 py-0.5 rounded-md font-semibold">
                          <lucide-icon name="banknote" class="w-4 h-4"></lucide-icon>
                          {{ job.salary }}
                        </span>
                      }
                      <span class="flex items-center gap-1.5">
                        <lucide-icon name="clock" class="w-4 h-4"></lucide-icon>
                        {{ formatDaysAgo(job.createdAt) }}
                      </span>
                    </div>

                    <!-- Tags & Easy Apply -->
                    <div class="flex justify-between items-center flex-wrap gap-4 mt-auto border-t border-gray-100 pt-4">
                      <div class="flex gap-2 flex-wrap">
                        @for (badge of jobBadges(job); track badge) {
                          <span class="bg-gray-50 px-3 py-1.5 rounded-md text-xs text-gray-700 font-semibold border border-gray-200">{{ badge }}</span>
                        }
                      </div>
                      
                      <div class="flex items-center gap-2">
                        <button (click)="toggleBookmark(job.jobId || job['id'], $event)" class="w-10 h-10 rounded-lg border border-gray-300 flex items-center justify-center transition-all relative group/bmk focus:outline-none hover-lift" 
                            [class.text-primary]="isBookmarked(job.jobId || job['id'])"
                            [class.border-primary]="isBookmarked(job.jobId || job['id'])"
                            [class.bg-primary-light]="isBookmarked(job.jobId || job['id'])"
                            [class.text-gray-400]="!isBookmarked(job.jobId || job['id'])"
                            [class.hover:text-primary]="!isBookmarked(job.jobId || job['id'])"
                            [class.hover:bg-primary-light]="!isBookmarked(job.jobId || job['id'])"
                            title="Save Job">
                          <lucide-icon name="bookmark" class="w-4 h-4" 
                            [class.fill-primary]="isBookmarked(job.jobId || job['id'])">
                          </lucide-icon>
                        </button>
                        <a [routerLink]="['/jobs', job.jobId || job['id']]" appRipple class="btn-primary py-2 px-6 text-sm !rounded-lg shadow-sm">View details</a>
                      </div>
                    </div>
                  </div>
                </div>
              }
              
              <!-- Pagination Footer -->
              @if (totalPages().length > 1) {
                <div class="flex justify-center items-center gap-2 mt-8 py-4">
                  <button (click)="prevPage()" [disabled]="currentPage() === 1" 
                    class="w-8 h-8 rounded-md flex items-center justify-center border border-gray-200 text-gray-500 hover:bg-gray-50 disabled:opacity-50 transition-colors">
                    &laquo;
                  </button>
                  
                  @for (page of displayedPages(); track page) {
                    @if (page === -1) {
                      <span class="text-gray-400 px-1 font-medium">...</span>
                    } @else {
                      <button (click)="goToPage(page)" 
                        [class.bg-primary]="currentPage() === page" 
                        [class.text-white]="currentPage() === page" 
                        [class.shadow-sm]="currentPage() === page"
                        [class.border-primary]="currentPage() === page"
                        [class.border-gray-200]="currentPage() !== page"
                        [class.text-gray-600]="currentPage() !== page"
                        [class.hover:bg-gray-50]="currentPage() !== page"
                        class="w-8 h-8 rounded-md flex items-center justify-center border font-medium transition-colors cursor-pointer">
                        {{ page }}
                      </button>
                    }
                  }
                  
                  <button (click)="nextPage()" [disabled]="currentPage() === totalPages().length" 
                    class="w-8 h-8 rounded-md flex items-center justify-center border border-gray-200 text-gray-500 hover:bg-gray-50 disabled:opacity-50 transition-colors">
                    &raquo;
                  </button>
                </div>
              }
            </div>
          }
        </main>
      </div>
    </div>
  `
})
export class JobListComponent implements OnInit, OnDestroy {
  private api = inject(ApiService);
  private auth = inject(AuthService);
  private toast = inject(ToastService);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private destroy$ = new Subject<void>();

  jobs: Job[] = [];
  filteredJobs: Job[] = [];
  recommendedJobs: Job[] = [];
  
  // Search parameters
  keyword = '';
  location = '';
  category = '';   // set from ?category= query param (landing page category click)
  recentSearches: string[] = [];
  
  // Advanced filter models
  filters = {
    fullTime: false,
    contract: false,
    remote: false,
    minSalary: 0,
    techStack: new Set<string>()
  };
  sortBy = 'Latest';
  readonly techOptions = ['React', 'Angular', 'Java', 'Spring', 'Python'];
  readonly showMobileFilters = signal(false);

  // Pagination Logic
  currentPage = signal(1);
  pageSize = 6;
  
  paginatedJobs(): Job[] {
    const start = (this.currentPage() - 1) * this.pageSize;
    return this.filteredJobs.slice(start, start + this.pageSize);
  }
  
  totalPages(): number[] {
    const pages = Math.max(1, Math.ceil(this.filteredJobs.length / this.pageSize));
    return Array.from({length: pages}, (_, i) => i + 1);
  }
  
  displayedPages(): number[] {
    const current = this.currentPage();
    const total = this.totalPages().length;
    if (total <= 5) return this.totalPages();
    if (current <= 3) return [1, 2, 3, 4, -1, total];
    if (current >= total - 2) return [1, -1, total - 3, total - 2, total - 1, total];
    return [1, -1, current - 1, current, current + 1, -1, total];
  }

  loading = signal(false); // signal avoids NG0100
  bookmarkedJobIds = signal<Set<string>>(new Set());

  get isJobSeeker(): boolean { return this.auth.isJobSeeker(); }

  ngOnInit(): void { 
    this.loadRecentSearches();
    this.loadBookmarks();

    this.route.queryParams.pipe(takeUntil(this.destroy$)).subscribe(params => {
      this.keyword  = params['keyword']  ?? '';
      this.location = params['location'] ?? '';
      this.category = params['category'] ?? '';
      this.loadJobs();
    });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  loadJobs(): void {
    if (this.loading()) return;
    this.loading.set(true);

    // Category filter: load all jobs, filter by job.category client-side.
    // Keyword/location: use search-service for server-side text search.
    // No params: load all jobs.
    const source$ = (this.keyword.trim() || this.location.trim())
      ? this.api.searchJobs(this.keyword, this.location).pipe(
          map(res => ({ ...res, data: (res.data ?? []).map((j: any) => ({ ...j, jobId: j.jobId ?? j.id }) as Job) }))
        )
      : this.api.getJobs();

    source$.pipe(takeUntil(this.destroy$)).subscribe({
      next: (res) => {
        this.jobs = res.data ?? [];
        this.applyLocalFilters();
        this.loading.set(false);
        this.loadRecommendations();
      },
      error: () => {
        this.toast.error('Failed to load jobs');
        this.loading.set(false);
        this.jobs = [];
        this.filteredJobs = [];
      }
    });
  }

  loadRecommendations(): void {
    if (!this.isJobSeeker) return;
    this.api.getRecommendations().pipe(takeUntil(this.destroy$)).subscribe({
      next: (res: any) => {
         const recommendations = Array.isArray(res) ? res : (res?.data || []);
         const recIds = recommendations.map((r: any) => r.jobId || r.id);
         this.recommendedJobs = this.jobs.filter(j => 
            recIds.includes((j as any).jobId) || recIds.includes((j as any).id)
         ).slice(0, 5);
      },
      error: () => {
        this.recommendedJobs = [];
      }
    });
  }

  submitSearch(): void {
    if (this.keyword.trim()) {
      this.saveRecentSearch(this.keyword.trim());
    }
    // Navigate to update URL params, which triggers ngOnInit queryParams subscription → loadJobs()
    this.router.navigate(['/jobs'], {
      queryParams: { keyword: this.keyword.trim() || null, location: this.location.trim() || null }
    });
  }

  toggleTechStack(tech: string): void {
    if (this.filters.techStack.has(tech)) {
      this.filters.techStack.delete(tech);
    } else {
      this.filters.techStack.add(tech);
    }
    this.applyLocalFilters();
  }

  activeFilterCount(): number {
    return Number(this.filters.fullTime)
      + Number(this.filters.contract)
      + Number(this.filters.remote)
      + (this.filters.minSalary > 0 ? 1 : 0)
      + this.filters.techStack.size;
  }

  activeFilterLabels(): string[] {
    const labels: string[] = [];
    if (this.filters.fullTime) labels.push('Full-time');
    if (this.filters.contract) labels.push('Contract');
    if (this.filters.remote) labels.push('Remote');
    if (this.filters.minSalary > 0) labels.push(`$${this.filters.minSalary}k+`);
    labels.push(...Array.from(this.filters.techStack));
    return labels;
  }

  jobBadges(job: Job): string[] {
    const badges: string[] = [];
    if (job.category?.trim()) badges.push(job.category.trim());
    if (this.isRemoteJob(job)) badges.push('Remote');
    if (this.isContractJob(job)) badges.push('Contract');
    else if (this.isFullTimeJob(job)) badges.push('Full-time');
    return Array.from(new Set(badges)).slice(0, 4);
  }

  applyLocalFilters(): void {
    const safeLower = (v: unknown) => (typeof v === 'string' ? v.toLowerCase() : '');
    const isOpen = (j: any) => safeLower(j?.status) === 'open';
    let result = this.jobs.filter(j => isOpen(j));

    // 1. Category filter (from landing page category click → ?category=Engineering)
    if (this.category.trim()) {
      result = result.filter(j => {
        const jobCat = (j as any)?.category?.trim() ?? '';
        return jobCat.toLowerCase() === this.category.toLowerCase();
      });
    }

    // 2. Advanced Job Type Filters
    if (this.filters.fullTime) {
      result = result.filter(j => this.isFullTimeJob(j));
    }
    if (this.filters.contract) {
      result = result.filter(j => this.isContractJob(j));
    }
    if (this.filters.remote) {
      result = result.filter(j => this.isRemoteJob(j));
    }

    // 3. Tech Stack Filters
    if (this.filters.techStack.size > 0) {
      result = result.filter(j => {
        return Array.from(this.filters.techStack).some(tech => this.matchesTech(j, tech));
      });
    }

    // 4. Salary Filter (approximation extraction)
    if (this.filters.minSalary > 0) {
      result = result.filter(j => {
        return this.extractSalary(j.salary) >= this.filters.minSalary;
      });
    }

    // 5. Sorting
    if (this.sortBy === 'Lowest Salary') { // optional extra sort
      result.sort((a,b) => this.extractSalary(a.salary) - this.extractSalary(b.salary));
    } else if (this.sortBy === 'Highest Salary') {
      result.sort((a,b) => this.extractSalary(b.salary) - this.extractSalary(a.salary));
    } else { // Latest or Most Relevant
      result.sort((a,b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime());
    }

    this.filteredJobs = result;
    this.currentPage.set(1); // reset to page 1 on new filter
  }

  private extractSalary(salaryStr: string | undefined): number {
    if (!salaryStr) return 0;
    const normalized = salaryStr.toLowerCase().replace(/,/g, '');
    const matches = normalized.match(/\d+(?:\.\d+)?/g);
    if (!matches?.length) return 0;

    const values = matches.map(raw => {
      const numeric = Number(raw);
      if (!Number.isFinite(numeric)) return 0;
      if (normalized.includes('lpa') || normalized.includes('lakh')) return numeric * 1.2;
      if (normalized.includes('k')) return numeric;
      return numeric > 1000 ? numeric / 1000 : numeric;
    });

    return Math.max(...values);
  }

  private jobSearchText(job: Job): string {
    return [
      job.title,
      job.company,
      job.location,
      job.description,
      job.category
    ].filter(Boolean).join(' ').toLowerCase();
  }

  private isContractJob(job: Job): boolean {
    return /\b(contract|contractor|consultant|consulting|freelance|temporary|temp)\b/.test(this.jobSearchText(job));
  }

  private isRemoteJob(job: Job): boolean {
    return /\b(remote|hybrid|work from home|wfh|anywhere)\b/.test(this.jobSearchText(job));
  }

  private isFullTimeJob(job: Job): boolean {
    const text = this.jobSearchText(job);
    if (/\b(full[-\s]?time|permanent)\b/.test(text)) return true;
    if (this.isContractJob(job)) return false;
    if (/\b(part[-\s]?time|intern|internship|trainee)\b/.test(text)) return false;
    return true;
  }

  private matchesTech(job: Job, tech: string): boolean {
    const text = this.jobSearchText(job);
    const matchers: Record<string, RegExp> = {
      React: /\breact(?:\.js|js)?\b/,
      Angular: /\bangular\b/,
      Java: /\bjava\b|\bj2ee\b/,
      Spring: /\bspring(?:\s+boot)?\b/,
      Python: /\bpython\b|\bdjango\b|\bflask\b/
    };
    return (matchers[tech] ?? new RegExp(`\\b${tech.toLowerCase()}\\b`)).test(text);
  }

  clearFilters(): void {
    this.keyword = '';
    this.location = '';
    this.category = '';
    this.filters.fullTime = false;
    this.filters.contract = false;
    this.filters.remote = false;
    this.filters.minSalary = 0;
    this.filters.techStack.clear();
    this.sortBy = 'Latest';
    this.router.navigate(['/jobs'], { queryParams: {} });
  }

  // --- Pagination Handlers ---
  goToPage(p: number) { 
    if (p >= 1 && p <= this.totalPages().length) this.currentPage.set(p); 
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }
  prevPage() { this.goToPage(this.currentPage() - 1); }
  nextPage() { this.goToPage(this.currentPage() + 1); }

  // --- Utility ---
  formatDaysAgo(dateStr: string): string {
    const diffTime = Math.abs(new Date().getTime() - new Date(dateStr).getTime());
    const diffDays = Math.floor(diffTime / (1000 * 60 * 60 * 24));
    return diffDays === 0 ? 'Today' : diffDays === 1 ? '1 day ago' : `${diffDays} days ago`;
  }

  // --- Bookmarks Logic ---
  loadBookmarks(): void {
    if (!this.isJobSeeker) return;
    this.api.getBookmarks().pipe(takeUntil(this.destroy$)).subscribe({
      next: (res: any) => {
        const bmks = Array.isArray(res) ? res : (res.data || []);
        const newSet = new Set(this.bookmarkedJobIds());
        bmks.forEach((b: any) => {
           newSet.add(b.jobId || b.id);
        });
        this.bookmarkedJobIds.set(newSet);
      },
      error: () => {
        this.bookmarkedJobIds.set(new Set());
      }
    });
  }

  isBookmarked(jobId: string): boolean {
    return this.bookmarkedJobIds().has(jobId);
  }

  toggleBookmark(jobId: string, event: Event): void {
    event.stopPropagation();
    event.preventDefault();
    if (!this.isJobSeeker) { 
      this.toast.info('Please log in as a Job Seeker to save jobs'); 
      return; 
    }

    if (this.bookmarkedJobIds().has(jobId)) {
      this.api.removeBookmark(jobId).subscribe({
        next: () => { 
          this.bookmarkedJobIds.update(set => {
            const nextSet = new Set(set);
            nextSet.delete(jobId);
            return nextSet;
          });
          this.toast.success('Removed from saved jobs'); 
        },
        error: () => this.toast.error('Failed to remove bookmark')
      });
    } else {
      this.api.bookmarkJob(jobId).subscribe({
        next: () => { 
          this.bookmarkedJobIds.update(set => {
            const nextSet = new Set(set);
            nextSet.add(jobId);
            return nextSet;
          });
          this.toast.success('Job saved successfully'); 
        },
        error: () => this.toast.error('Failed to save job')
      });
    }
  }

  // --- LocalStorage History ---
  loadRecentSearches(): void {
    try {
      const stored = localStorage.getItem('hirehub_recent_searches');
      if (stored) this.recentSearches = JSON.parse(stored);
    } catch(e) {}
  }

  saveRecentSearch(term: string): void {
    if (!term) return;
    this.recentSearches = this.recentSearches.filter(t => t.toLowerCase() !== term.toLowerCase());
    this.recentSearches.unshift(term);
    if (this.recentSearches.length > 5) this.recentSearches = this.recentSearches.slice(0, 5);
    localStorage.setItem('hirehub_recent_searches', JSON.stringify(this.recentSearches));
  }

  applyRecentSearch(term: string): void {
    this.keyword = term;
    this.submitSearch();
  }

  clearRecentSearches(): void {
    this.recentSearches = [];
    localStorage.removeItem('hirehub_recent_searches');
  }

  removeKeyword(): void {
    this.keyword = '';
    this.router.navigate(['/jobs'], { queryParams: { location: this.location.trim() || null } });
  }

  removeLocation(): void {
    this.location = '';
    this.router.navigate(['/jobs'], { queryParams: { keyword: this.keyword.trim() || null } });
  }
}
