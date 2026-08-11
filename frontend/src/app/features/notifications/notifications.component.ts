import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { LucideAngularModule } from 'lucide-angular';
import { ApiService } from '../../core/services/api.service';
import { ToastService } from '../../core/services/toast.service';

@Component({
  selector: 'app-notifications',
  standalone: true,
  imports: [CommonModule, LucideAngularModule],
  template: `
    <div class="min-h-screen bg-gray-50 py-10 px-4 sm:px-6">
      <div class="max-w-2xl mx-auto">

        <div class="flex items-center justify-between mb-6">
          <div>
            <h1 class="text-2xl font-bold text-gray-900 tracking-tight">Notifications</h1>
            <p class="text-gray-500 text-sm mt-0.5">All your activity updates in one place.</p>
          </div>
          @if (unreadCount() > 0) {
            <button (click)="markAllRead()" class="btn-secondary py-1.5 px-4 text-sm flex items-center gap-1.5">
              <lucide-icon name="check-check" class="w-3.5 h-3.5"></lucide-icon> Mark all read
            </button>
          }
        </div>

        @if (loading()) {
          <div class="bg-white rounded-2xl border border-gray-200 shadow-sm divide-y divide-gray-100">
            @for (sk of [1,2,3,4,5]; track sk) {
              <div class="flex gap-4 px-5 py-4 animate-pulse">
                <div class="w-9 h-9 rounded-xl bg-gray-100 flex-shrink-0"></div>
                <div class="flex-1 space-y-2 pt-1">
                  <div class="h-3.5 bg-gray-200 rounded w-3/4"></div>
                  <div class="h-3 bg-gray-100 rounded w-1/4"></div>
                </div>
              </div>
            }
          </div>
        } @else if (loadFailed()) {
          <div class="bg-white rounded-2xl border border-amber-200 shadow-sm text-center py-16 px-6">
            <div class="w-14 h-14 rounded-2xl bg-amber-50 flex items-center justify-center mx-auto mb-3">
              <lucide-icon name="alert-circle" class="w-7 h-7 text-amber-400"></lucide-icon>
            </div>
            <h3 class="text-base font-semibold text-gray-900 mb-1">Temporarily unavailable</h3>
            <p class="text-gray-500 text-sm mb-5">Your other dashboard features can still be used.</p>
            <button (click)="loadNotifications()" class="btn-secondary py-2 px-4 text-sm inline-flex items-center gap-2">
              <lucide-icon name="refresh-cw" class="w-4 h-4"></lucide-icon> Retry
            </button>
          </div>
        } @else if (notifications().length === 0) {
          <div class="bg-white rounded-2xl border border-gray-200 shadow-sm text-center py-20">
            <div class="w-16 h-16 rounded-2xl bg-gray-50 border border-gray-100 flex items-center justify-center mx-auto mb-4">
              <lucide-icon name="bell" class="w-8 h-8 text-gray-300"></lucide-icon>
            </div>
            <h3 class="text-base font-semibold text-gray-900 mb-1">No notifications yet</h3>
            <p class="text-gray-500 text-sm">Activity updates will appear here as you apply to jobs.</p>
          </div>
        } @else {
          @if (unreadCount() > 0) {
            <div class="flex items-center gap-2 mb-3 px-1">
              <span class="inline-flex items-center gap-1.5 bg-primary-light text-primary text-xs font-bold px-3 py-1 rounded-full border border-primary/20">
                <span class="w-1.5 h-1.5 rounded-full bg-primary animate-pulse"></span>
                {{ unreadCount() }} unread
              </span>
            </div>
          }

          <div class="bg-white rounded-2xl border border-gray-200 shadow-sm overflow-hidden">
            @for (group of groupedNotifications(); track group.label) {
              <div class="notif-group-header">{{ group.label }}</div>
              @for (n of group.items; track n.id) {
                <div class="flex gap-3 px-5 py-3.5 hover:bg-gray-50 transition-colors cursor-pointer border-b border-gray-50 last:border-0"
                  [class.bg-indigo-50]="!n.read"
                  (click)="markRead(n)">

                  <div class="flex-shrink-0 w-9 h-9 rounded-xl flex items-center justify-center mt-0.5"
                    [ngClass]="notifIconClass(n)">
                    <lucide-icon [name]="notifIcon(n)" class="w-4 h-4"></lucide-icon>
                  </div>

                  <div class="flex-1 min-w-0">
                    <p class="text-sm text-gray-800 leading-snug" [class.font-semibold]="!n.read">{{ n.message }}</p>
                    <p class="text-xs text-gray-400 mt-0.5">{{ n.createdAt | date:'MMM d, h:mm a' }}</p>
                  </div>

                  <div class="flex-shrink-0 mt-2">
                    @if (!n.read) {
                      <span class="w-2 h-2 rounded-full bg-primary block"></span>
                    }
                  </div>
                </div>
              }
            }
          </div>
        }
      </div>
    </div>
  `
})
export class NotificationsComponent implements OnInit {
  private api = inject(ApiService);
  private toast = inject(ToastService);

  notifications = signal<any[]>([]);
  loading = signal(true);
  loadFailed = signal(false);
  unreadCount = () => this.notifications().filter((n: any) => !n.read).length;

  groupedNotifications(): { label: string; items: any[] }[] {
    const now = new Date();
    const today = new Date(now.getFullYear(), now.getMonth(), now.getDate());
    const weekAgo = new Date(today.getTime() - 6 * 24 * 60 * 60 * 1000);
    const groups: Record<string, any[]> = { 'Today': [], 'This Week': [], 'Earlier': [] };
    for (const n of this.notifications()) {
      const d = new Date(n.createdAt);
      const day = new Date(d.getFullYear(), d.getMonth(), d.getDate());
      if (day.getTime() === today.getTime()) groups['Today'].push(n);
      else if (day >= weekAgo) groups['This Week'].push(n);
      else groups['Earlier'].push(n);
    }
    return Object.entries(groups)
      .filter(([, items]) => items.length > 0)
      .map(([label, items]) => ({ label, items }));
  }

  notifIcon(n: any): string {
    const msg = (n.message || '').toLowerCase();
    if (msg.includes('interview')) return 'calendar-check';
    if (msg.includes('offer') || msg.includes('hired')) return 'party-popper';
    if (msg.includes('reject')) return 'x-circle';
    if (msg.includes('shortlist') || msg.includes('review')) return 'star';
    if (msg.includes('applied') || msg.includes('application')) return 'send';
    if (msg.includes('verified') || msg.includes('approved')) return 'badge-check';
    return 'bell';
  }

  notifIconClass(n: any): string {
    const msg = (n.message || '').toLowerCase();
    if (msg.includes('interview')) return 'notif-icon-interview';
    if (msg.includes('offer') || msg.includes('hired')) return 'notif-icon-offer';
    if (msg.includes('reject')) return 'notif-icon-rejected';
    if (msg.includes('shortlist') || msg.includes('review')) return 'notif-icon-shortlist';
    if (msg.includes('applied') || msg.includes('application')) return 'notif-icon-applied';
    if (msg.includes('verified') || msg.includes('approved')) return 'notif-icon-verified';
    return 'notif-icon-default';
  }

  ngOnInit(): void { this.loadNotifications(); }

  loadNotifications(): void {
    this.loading.set(true);
    this.loadFailed.set(false);
    this.api.getNotifications().subscribe({
      next: (res: any) => {
        this.notifications.set(res?.data ?? []);
        this.loadFailed.set(false);
        this.loading.set(false);
      },
      error: () => {
        this.loadFailed.set(true);
        this.loading.set(false);
      }
    });
  }

  markRead(n: any): void {
    if (n.read) return;
    this.api.markNotificationRead(n.id).subscribe({
      next: () => this.notifications.update(list => list.map(item => item.id === n.id ? { ...item, read: true } : item)),
      error: () => { /* silent */ }
    });
  }

  markAllRead(): void {
    this.api.markAllRead().subscribe({
      next: () => {
        this.notifications.update(list => list.map(n => ({ ...n, read: true })));
        this.toast.success('All notifications marked as read');
      },
      error: () => this.toast.error('Failed to mark notifications as read')
    });
  }
}
