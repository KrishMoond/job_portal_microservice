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
    <div class="min-h-screen bg-gray-50 py-10 px-6">
      <div class="max-w-3xl mx-auto">

        <div class="flex items-center justify-between mb-8">
          <div>
            <h1 class="text-2xl font-bold text-gray-900 tracking-tight">Notifications</h1>
            <p class="text-gray-500 text-sm mt-1">All your activity updates in one place.</p>
          </div>
          @if (unreadCount() > 0) {
            <button (click)="markAllRead()" class="btn-secondary py-1.5 px-4 text-sm flex items-center gap-1.5">
              <lucide-icon name="check" class="w-3.5 h-3.5"></lucide-icon> Mark all read
            </button>
          }
        </div>

        @if (loading()) {
          <div class="bg-white rounded-2xl border border-gray-200 shadow-sm divide-y divide-gray-100">
            @for (sk of [1,2,3,4,5]; track sk) {
              <div class="flex gap-4 px-6 py-4 animate-pulse">
                <div class="w-2 h-2 rounded-full bg-gray-200 mt-2 flex-shrink-0"></div>
                <div class="flex-1 space-y-2">
                  <div class="h-4 bg-gray-200 rounded w-3/4"></div>
                  <div class="h-3 bg-gray-100 rounded w-1/4"></div>
                </div>
              </div>
            }
          </div>
        } @else if (loadFailed()) {
          <div class="bg-white rounded-2xl border border-amber-200 shadow-sm text-center py-20 px-6">
            <lucide-icon name="alert-circle" class="w-16 h-16 text-amber-500 mx-auto mb-4"></lucide-icon>
            <h3 class="text-lg font-semibold text-gray-900 mb-1">Notifications are temporarily unavailable</h3>
            <p class="text-gray-500 text-sm mb-5">Your other dashboard features can still be used.</p>
            <button (click)="loadNotifications()" class="btn-secondary py-2 px-4 text-sm inline-flex items-center gap-2">
              <lucide-icon name="refresh-cw" class="w-4 h-4"></lucide-icon>
              Retry
            </button>
          </div>
        } @else if (notifications().length === 0) {
          <div class="bg-white rounded-2xl border border-gray-200 shadow-sm text-center py-20">
            <lucide-icon name="bell" class="w-16 h-16 text-gray-300 mx-auto mb-4"></lucide-icon>
            <h3 class="text-lg font-semibold text-gray-900 mb-1">No notifications yet</h3>
            <p class="text-gray-500 text-sm">Activity updates will appear here.</p>
          </div>
        } @else {
          <div class="bg-white rounded-2xl border border-gray-200 shadow-sm overflow-hidden">
            <!-- Unread count badge -->
            @if (unreadCount() > 0) {
              <div class="px-6 py-3 bg-primary-light border-b border-primary/10 flex items-center gap-2">
                <div class="w-2 h-2 rounded-full bg-primary"></div>
                <span class="text-xs font-semibold text-primary">{{ unreadCount() }} unread</span>
              </div>
            }
            <div class="divide-y divide-gray-100">
              @for (n of notifications(); track n.id) {
                <div class="flex gap-4 px-6 py-4 hover:bg-gray-50 transition-colors cursor-pointer"
                  [class.bg-indigo-50]="!n.read"
                  (click)="markRead(n)">
                  <div class="mt-1.5 flex-shrink-0">
                    @if (!n.read) {
                      <div class="w-2 h-2 rounded-full bg-primary"></div>
                    } @else {
                      <div class="w-2 h-2 rounded-full border border-gray-300"></div>
                    }
                  </div>
                  <div class="flex-1 min-w-0">
                    <p class="text-sm text-gray-800 leading-snug" [class.font-semibold]="!n.read">{{ n.message }}</p>
                    <p class="text-xs text-gray-400 mt-1">{{ n.createdAt | date:'medium' }}</p>
                  </div>
                </div>
              }
            </div>
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
  unreadCount = () => this.notifications().filter(n => !n.read).length;

  ngOnInit(): void {
    this.loadNotifications();
  }

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
      next: () => {
        this.notifications.update(list =>
          list.map(item => item.id === n.id ? { ...item, read: true } : item)
        );
      },
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
