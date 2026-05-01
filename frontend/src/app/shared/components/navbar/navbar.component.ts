import { Component, DestroyRef, ElementRef, HostListener, afterNextRender, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { CommonModule } from '@angular/common';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { ApiService } from '../../../core/services/api.service';
import { User } from '../../models/models';
import { LucideAngularModule } from 'lucide-angular';
import { interval } from 'rxjs';
import { filter } from 'rxjs/operators';
import { RippleDirective } from '../../directives/ripple.directive';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive, LucideAngularModule, RippleDirective],
  template: `
    <nav class="bg-white border-b border-gray-200 sticky top-0 z-50 h-16 w-full shadow-sm backdrop-blur-md bg-opacity-95">
      <div class="max-w-7xl mx-auto px-4 h-full flex items-center justify-between">
        <!-- Logo -->
        <a routerLink="/" class="flex items-center gap-2 group">
          <lucide-icon name="briefcase" class="w-6 h-6 text-primary group-hover:scale-110 transition-transform animate-float-slow"></lucide-icon>
          <span class="text-xl font-bold text-gray-900 tracking-tight text-gradient-animated">HireHub</span>
        </a>

        <!-- Desktop Links -->
        <div class="hidden md:flex items-center gap-8">
          <a routerLink="/jobs" routerLinkActive="text-primary font-semibold"
            class="text-sm text-gray-700 hover:text-primary font-medium transition-colors animated-underline">Jobs</a>
          <a routerLink="/companies" routerLinkActive="text-primary font-semibold"
            class="text-sm text-gray-700 hover:text-primary font-medium transition-colors animated-underline">Companies</a>
          @if (isJobSeeker()) {
            <a routerLink="/seeker/applications" routerLinkActive="text-primary font-semibold"
              class="text-sm text-gray-700 hover:text-primary font-medium transition-colors animated-underline">Dashboard</a>
          } @else if (isRecruiter()) {
            <a routerLink="/recruiter/dashboard" routerLinkActive="text-primary font-semibold"
              class="text-sm text-gray-700 hover:text-primary font-medium transition-colors animated-underline">Dashboard</a>
          }
        </div>

        <!-- Right Side: Auth / Profile -->
        <div class="flex items-center gap-3 md:gap-5">
          @if (!user()) {
            <a routerLink="/login" class="text-sm font-medium text-gray-700 hover:text-primary transition-colors hidden sm:block animated-underline">Log In</a>
            <a routerLink="/register" appRipple class="btn-primary py-1.5 px-4 text-sm hidden sm:flex hover-lift">Sign Up</a>
          } @else {
            <!-- Bookmarks Icon (desktop) -->
            @if (isJobSeeker()) {
              <a routerLink="/seeker/bookmarks" class="cursor-pointer group hidden md:flex items-center justify-center relative pt-1" title="Saved Jobs">
                <lucide-icon name="bookmark" class="w-5 h-5 text-gray-700 group-hover:text-primary transition-colors"></lucide-icon>
              </a>
            }

            <!-- Notifications -->
            <div class="relative pt-1">
              <button type="button" (click)="toggleNotifications()"
                class="relative group flex items-center justify-center bg-transparent border-0 p-0 cursor-pointer"
                aria-label="Notifications"
                [attr.aria-expanded]="showNotifications()">
                <lucide-icon name="bell" class="w-5 h-5 text-gray-700 group-hover:text-primary transition-colors group-hover:scale-110"></lucide-icon>
                @if (unreadCount() > 0) {
                  <span class="absolute -top-2 -right-2 w-4 h-4 bg-red-500 text-white text-[10px] font-bold rounded-full flex items-center justify-center animate-pulse-glow">
                    {{ unreadCount() > 9 ? '9+' : unreadCount() }}
                  </span>
                }
              </button>
              <!-- Notification Dropdown -->
              @if (showNotifications()) {
                <div class="absolute right-0 top-10 w-96 bg-white border border-gray-200 rounded-2xl shadow-2xl z-50 max-h-[32rem] overflow-hidden animate-fade-in">
                  <!-- Header -->
                  <div class="flex items-center justify-between px-5 py-4 border-b border-gray-200 bg-gradient-to-r from-primary/5 to-secondary/5">
                    <div class="flex items-center gap-2">
                      <lucide-icon name="bell" class="w-5 h-5 text-primary"></lucide-icon>
                      <span class="text-gray-900 font-bold text-base">Notifications</span>
                      @if (unreadCount() > 0) {
                        <span class="bg-primary text-white text-xs font-bold px-2 py-0.5 rounded-full">{{ unreadCount() }}</span>
                      }
                    </div>
                    <div class="flex items-center gap-2">
                      <button (click)="markAllRead(); $event.stopPropagation()" 
                        class="text-primary text-xs font-semibold hover:underline bg-transparent border-none cursor-pointer px-2 py-1 rounded-md hover:bg-primary/10 transition-colors">
                        Mark all read
                      </button>
                      <a routerLink="/notifications" (click)="showNotifications.set(false)" 
                        class="text-xs text-gray-600 hover:text-primary font-medium px-2 py-1 rounded-md hover:bg-gray-100 transition-colors">
                        See all
                      </a>
                    </div>
                  </div>
                  
                  <!-- Notifications List -->
                  <div class="overflow-y-auto max-h-[28rem] scrollbar-thin scrollbar-thumb-gray-300 scrollbar-track-gray-100">
                    @for (n of notifications().slice(0, 5); track n.id) {
                      <div class="px-5 py-4 border-b border-gray-100 hover:bg-gray-50 transition-all cursor-pointer group relative"
                        [class.bg-primary/5]="!n.read"
                        [class.border-l-4]="!n.read"
                        [class.border-l-primary]="!n.read">
                        <div class="flex gap-3">
                          <!-- Icon/Avatar -->
                          <div class="flex-shrink-0 mt-0.5">
                            <div class="w-10 h-10 rounded-full flex items-center justify-center"
                              [class.bg-primary/10]="!n.read"
                              [class.bg-gray-100]="n.read">
                              @if (n.message.toLowerCase().includes('offer')) {
                                <lucide-icon name="gift" class="w-5 h-5" [class.text-primary]="!n.read" [class.text-gray-500]="n.read"></lucide-icon>
                              } @else if (n.message.toLowerCase().includes('shortlist')) {
                                <lucide-icon name="star" class="w-5 h-5" [class.text-primary]="!n.read" [class.text-gray-500]="n.read"></lucide-icon>
                              } @else if (n.message.toLowerCase().includes('reject')) {
                                <lucide-icon name="x-circle" class="w-5 h-5" [class.text-red-500]="!n.read" [class.text-gray-500]="n.read"></lucide-icon>
                              } @else {
                                <lucide-icon name="bell" class="w-5 h-5" [class.text-primary]="!n.read" [class.text-gray-500]="n.read"></lucide-icon>
                              }
                            </div>
                          </div>
                          
                          <!-- Content -->
                          <div class="flex-1 min-w-0">
                            <p class="text-sm leading-relaxed"
                              [class.text-gray-900]="!n.read"
                              [class.font-semibold]="!n.read"
                              [class.text-gray-700]="n.read"
                              [class.font-medium]="n.read">
                              {{ n.message }}
                            </p>
                            <div class="flex items-center gap-2 mt-2">
                              <lucide-icon name="clock" class="w-3 h-3 text-gray-400"></lucide-icon>
                              <p class="text-xs text-gray-500 font-medium">{{ n.createdAt | date:'short' }}</p>
                            </div>
                          </div>
                          
                          <!-- Unread Indicator -->
                          @if (!n.read) {
                            <div class="flex-shrink-0">
                              <div class="w-2 h-2 rounded-full bg-primary animate-pulse"></div>
                            </div>
                          }
                        </div>
                      </div>
                    }
                    
                    <!-- Empty State -->
                    @if (notifications().length === 0) {
                      <div class="text-center py-12">
                        <div class="w-16 h-16 bg-gray-100 rounded-full flex items-center justify-center mx-auto mb-4">
                          <lucide-icon name="bell-off" class="w-8 h-8 text-gray-400"></lucide-icon>
                        </div>
                        <p class="text-gray-600 font-medium mb-1">No new notifications</p>
                        <p class="text-gray-400 text-sm">You're all caught up!</p>
                      </div>
                    }
                  </div>
                  
                  <!-- Footer -->
                  @if (notifications().length > 5) {
                    <div class="border-t border-gray-200 bg-gray-50">
                      <a routerLink="/notifications" (click)="showNotifications.set(false)"
                        class="block text-center text-sm text-primary font-bold py-3 hover:bg-gray-100 transition-colors">
                        View all {{ notifications().length }} notifications →
                      </a>
                    </div>
                  }
                </div>
              }
            </div>

            <!-- Profile Dropdown (desktop) -->
            <div class="relative hidden md:block">
              <button type="button" (click)="toggleMenu()"
                class="flex items-center gap-2 cursor-pointer bg-transparent border-0 p-0 group"
                aria-label="Open account menu"
                [attr.aria-expanded]="showMenu()">
                <div class="w-8 h-8 rounded-full bg-primary-light flex items-center justify-center border border-primary/20 group-hover:scale-110 transition-transform">
                  <span class="text-primary text-sm font-bold">{{ initial() }}</span>
                </div>
                <lucide-icon name="chevron-down" class="w-4 h-4 text-gray-700 group-hover:rotate-180 transition-transform"></lucide-icon>
              </button>

              @if (showMenu()) {
                <div class="absolute right-0 top-12 w-72 rounded-2xl shadow-2xl z-50 py-3 animate-fade-in overflow-hidden border border-white/30"
                     style="background: rgba(255, 255, 255, 0.85); backdrop-filter: blur(20px) saturate(180%); -webkit-backdrop-filter: blur(20px) saturate(180%);">
                  <!-- User Info Header - Light & Translucent -->
                  <div class="px-5 py-4 mb-2 border-b border-white/40"
                       style="background: linear-gradient(135deg, rgba(249, 250, 251, 0.5), rgba(255, 255, 255, 0.3));">
                    <div class="flex items-center gap-3">
                      <div class="w-14 h-14 rounded-full bg-gradient-to-br from-primary to-secondary flex items-center justify-center shadow-lg">
                        <span class="text-white text-xl font-black">{{ initial() }}</span>
                      </div>
                      <div class="flex-1 min-w-0">
                        <p class="text-gray-900 text-base font-bold truncate">{{ user()!.name || 'User' }}</p>
                        <p class="text-gray-600 text-xs truncate font-medium">{{ user()!.email || user()!.userId }}</p>
                      </div>
                    </div>
                  </div>
                  
                  <!-- Menu Items - Translucent -->
                  <div class="px-2 py-1 space-y-1">
                    @if (isJobSeeker()) {
                      <a routerLink="/seeker/profile" 
                        class="flex items-center gap-3 px-4 py-3 text-sm text-gray-800 hover:text-primary transition-all font-semibold group rounded-xl hover:bg-white/40">
                        <div class="w-8 h-8 rounded-lg bg-primary/10 flex items-center justify-center group-hover:bg-primary/20 transition-colors">
                          <lucide-icon name="user" class="w-4 h-4 text-primary"></lucide-icon>
                        </div>
                        <span>My Profile</span>
                        <lucide-icon name="chevron-right" class="w-4 h-4 ml-auto text-gray-400 group-hover:text-primary group-hover:translate-x-1 transition-all"></lucide-icon>
                      </a>
                      <a routerLink="/seeker/bookmarks" 
                        class="flex items-center gap-3 px-4 py-3 text-sm text-gray-800 hover:text-primary transition-all font-semibold group rounded-xl hover:bg-white/40">
                        <div class="w-8 h-8 rounded-lg bg-accent/10 flex items-center justify-center group-hover:bg-accent/20 transition-colors">
                          <lucide-icon name="bookmark" class="w-4 h-4 text-accent"></lucide-icon>
                        </div>
                        <span>Bookmarks</span>
                        <lucide-icon name="chevron-right" class="w-4 h-4 ml-auto text-gray-400 group-hover:text-primary group-hover:translate-x-1 transition-all"></lucide-icon>
                      </a>
                      <a routerLink="/notifications" 
                        class="flex items-center gap-3 px-4 py-3 text-sm text-gray-800 hover:text-primary transition-all font-semibold group rounded-xl hover:bg-white/40">
                        <div class="w-8 h-8 rounded-lg bg-secondary/10 flex items-center justify-center group-hover:bg-secondary/20 transition-colors relative">
                          <lucide-icon name="bell" class="w-4 h-4 text-secondary"></lucide-icon>
                          @if (unreadCount() > 0) {
                            <span class="absolute -top-1 -right-1 w-4 h-4 bg-red-500 text-white text-[9px] font-bold rounded-full flex items-center justify-center animate-pulse">{{ unreadCount() }}</span>
                          }
                        </div>
                        <span>Notifications</span>
                        <lucide-icon name="chevron-right" class="w-4 h-4 ml-auto text-gray-400 group-hover:text-primary group-hover:translate-x-1 transition-all"></lucide-icon>
                      </a>
                    }
                  </div>
                  
                  <!-- Logout - Translucent -->
                  <div class="px-2 mt-2 pt-2 border-t border-white/40">
                    <button (click)="logout()" 
                      class="w-full flex items-center gap-3 px-4 py-3 text-sm text-red-600 transition-all font-bold group rounded-xl hover:bg-red-50/70">
                      <div class="w-8 h-8 rounded-lg bg-red-50 flex items-center justify-center group-hover:bg-red-100 transition-colors">
                        <lucide-icon name="log-out" class="w-4 h-4 text-red-600"></lucide-icon>
                      </div>
                      <span>Log out</span>
                      <lucide-icon name="chevron-right" class="w-4 h-4 ml-auto text-red-400 group-hover:translate-x-1 transition-all"></lucide-icon>
                    </button>
                  </div>
                </div>
              }
            </div>
          }

          <!-- Mobile Hamburger -->
          <button type="button" (click)="toggleMobileMenu()" appRipple class="md:hidden flex items-center justify-center w-9 h-9 rounded-lg hover:bg-gray-100 transition-colors hover-lift"
            aria-label="Toggle navigation menu"
            [attr.aria-expanded]="showMobileMenu()">
            @if (showMobileMenu()) {
              <lucide-icon name="x" class="w-5 h-5 text-gray-900"></lucide-icon>
            } @else {
              <lucide-icon name="menu" class="w-5 h-5 text-gray-900"></lucide-icon>
            }
          </button>
        </div>
      </div>

      <!-- Mobile Menu Drawer -->
      @if (showMobileMenu()) {
        <div class="md:hidden absolute top-16 left-0 right-0 bg-white border-b border-gray-200 shadow-lg z-40 animate-slide-down backdrop-blur-md">
          <div class="px-4 py-3 space-y-1">
            <a routerLink="/jobs" (click)="showMobileMenu.set(false)"
              class="flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium text-gray-700 hover:bg-gray-100 hover:text-primary transition-colors">
              <lucide-icon name="briefcase" class="w-4 h-4"></lucide-icon> Jobs
            </a>
            <a routerLink="/companies" (click)="showMobileMenu.set(false)"
              class="flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium text-gray-700 hover:bg-gray-100 hover:text-primary transition-colors">
              <lucide-icon name="building-2" class="w-4 h-4"></lucide-icon> Companies
            </a>
            @if (user()) {
              @if (isJobSeeker()) {
                <a routerLink="/seeker/applications" (click)="showMobileMenu.set(false)"
                  class="flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium text-gray-700 hover:bg-gray-100 hover:text-primary transition-colors">
                  <lucide-icon name="file-text" class="w-4 h-4"></lucide-icon> My Applications
                </a>
                <a routerLink="/seeker/bookmarks" (click)="showMobileMenu.set(false)"
                  class="flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium text-gray-700 hover:bg-gray-100 hover:text-primary transition-colors">
                  <lucide-icon name="bookmark" class="w-4 h-4"></lucide-icon> Saved Jobs
                </a>
                <a routerLink="/seeker/profile" (click)="showMobileMenu.set(false)"
                  class="flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium text-gray-700 hover:bg-gray-100 hover:text-primary transition-colors">
                  <lucide-icon name="user" class="w-4 h-4"></lucide-icon> My Profile
                </a>
              } @else if (isRecruiter()) {
                <a routerLink="/recruiter/dashboard" (click)="showMobileMenu.set(false)"
                  class="flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium text-gray-700 hover:bg-gray-100 hover:text-primary transition-colors">
                  <lucide-icon name="bar-chart-3" class="w-4 h-4"></lucide-icon> Dashboard
                </a>
                <a routerLink="/recruiter/post-job" (click)="showMobileMenu.set(false)"
                  class="flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium text-gray-700 hover:bg-gray-100 hover:text-primary transition-colors">
                  <lucide-icon name="plus" class="w-4 h-4"></lucide-icon> Post a Job
                </a>
              }
              <a routerLink="/notifications" (click)="showMobileMenu.set(false)"
                class="flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium text-gray-700 hover:bg-gray-100 hover:text-primary transition-colors">
                <lucide-icon name="bell" class="w-4 h-4"></lucide-icon> Notifications
                @if (unreadCount() > 0) {
                  <span class="ml-auto bg-red-500 text-white text-[10px] font-bold px-1.5 py-0.5 rounded-full">{{ unreadCount() }}</span>
                }
              </a>
              <div class="border-t border-gray-200 mt-2 pt-2">
                <div class="px-3 py-2">
                  <p class="text-sm font-medium text-gray-900">{{ user()!.name }}</p>
                  <p class="text-xs text-gray-600">{{ user()!.email }}</p>
                </div>
                <button (click)="logout(); showMobileMenu.set(false)"
                  class="w-full flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium text-red-500 hover:bg-red-500 hover:bg-opacity-10 transition-colors">
                  <lucide-icon name="log-out" class="w-4 h-4"></lucide-icon> Log out
                </button>
              </div>
            } @else {
              <div class="border-t border-surface-border mt-2 pt-2 flex flex-col gap-2">
                <a routerLink="/login" (click)="showMobileMenu.set(false)" class="btn-secondary py-2 text-center text-sm">Log In</a>
                <a routerLink="/register" (click)="showMobileMenu.set(false)" class="btn-primary py-2 text-center text-sm">Sign Up</a>
              </div>
            }
          </div>
        </div>
      }
    </nav>
  `
})
export class NavbarComponent {
  private auth = inject(AuthService);
  private api = inject(ApiService);
  private destroyRef = inject(DestroyRef);
  private elRef = inject(ElementRef<HTMLElement>);

  // ── Signals (all state as signals — no plain mutable properties) ──────────
  readonly user = signal<User | null>(null);
  readonly notifications = signal<any[]>([]);
  readonly showNotifications = signal(false);
  readonly showMenu = signal(false);
  readonly showMobileMenu = signal(false);

  // ── Computed signals (derived — never mutated directly) ──────────────────
  readonly unreadCount = computed(() => this.notifications().length);
  readonly initial = computed(() => this.user()?.name?.charAt(0).toUpperCase() ?? '?');
  readonly isRecruiter = computed(() => this.user()?.role === 'RECRUITER');
  readonly isJobSeeker = computed(() => this.user()?.role === 'JOB_SEEKER');

  constructor() {
    this.auth.currentUser$
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((u: User | null) => this.user.set(u));

    afterNextRender(() => {
      if (this.user()) {
        this.loadNotifications();
      }

      // Poll every 60s instead of 30s to reduce load
      interval(60000)
        .pipe(
          filter(() => !!this.user()),
          takeUntilDestroyed(this.destroyRef)
        )
        .subscribe(() => this.loadNotifications());
    });
  }

  loadNotifications(): void {
    if (!this.user()) return;
    
    this.api.getUnreadNotifications().subscribe({
      next: (res: any) => {
        this.notifications.set(res.data ?? []);
      },
      error: () => {
        // Silently ignore
      }
    });
  }

  toggleMobileMenu(): void {
    this.showMobileMenu.update(v => !v);
    this.showNotifications.set(false);
    this.showMenu.set(false);
  }

  toggleNotifications(): void {
    this.showNotifications.update(v => !v);
    this.showMenu.set(false);
  }

  toggleMenu(): void {
    this.showMenu.update(v => !v);
    this.showNotifications.set(false);
  }

  markAllRead(): void {
    this.api.markAllRead().subscribe({
      next: () => {
        this.notifications.set([]);
        this.showNotifications.set(false);
      },
      error: () => {
        // Ignore while services are unavailable; polling will refresh state later.
      }
    });
  }

  logout(): void {
    this.auth.logout();
    this.showMenu.set(false);
  }

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent): void {
    const target = event.target as Node | null;
    if (!target) return;
    if (!this.elRef.nativeElement.contains(target)) {
      this.showNotifications.set(false);
      this.showMenu.set(false);
      this.showMobileMenu.set(false);
    }
  }
}
