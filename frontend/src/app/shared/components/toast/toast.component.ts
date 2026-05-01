import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ToastService } from '../../../core/services/toast.service';

@Component({
  selector: 'app-toast',
  standalone: true,
  imports: [CommonModule],
  styles: [`
    .toast-item {
      animation: slideInRight 0.3s cubic-bezier(0.34, 1.56, 0.64, 1) both;
    }
    @keyframes slideInRight {
      from { opacity: 0; transform: translateX(100%); }
      to   { opacity: 1; transform: translateX(0); }
    }
  `],
  template: `
    <div class="fixed bottom-6 right-6 z-[9999] flex flex-col gap-3">
      @for (toast of toastService.toasts(); track toast.id) {
        <div class="toast-item flex items-center gap-3 px-5 py-4 rounded-xl shadow-2xl text-white text-sm font-medium min-w-64 max-w-sm"
          [class.bg-green-600]="toast.type === 'success'"
          [class.bg-red-600]="toast.type === 'error'"
          [class.bg-amber-500]="toast.type === 'warning'"
          [class.bg-blue-600]="toast.type === 'info'">
          <span class="text-base leading-none flex-shrink-0">
            {{ toast.type === 'success' ? '✅' : toast.type === 'error' ? '❌' : toast.type === 'warning' ? '⚠️' : 'ℹ️' }}
          </span>
          <span class="flex-1 leading-snug">{{ toast.message }}</span>
          <button (click)="toastService.remove(toast.id)"
            class="ml-2 text-white/70 hover:text-white transition-colors flex-shrink-0 text-lg leading-none">✕</button>
        </div>
      }
    </div>
  `
})
export class ToastComponent {
  toastService = inject(ToastService);
}
