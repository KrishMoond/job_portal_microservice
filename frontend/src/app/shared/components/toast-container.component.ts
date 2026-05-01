import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ToastService } from '../../core/services/toast.service';
import { LucideAngularModule } from 'lucide-angular';

@Component({
  selector: 'app-toast-container',
  standalone: true,
  imports: [CommonModule, LucideAngularModule],
  template: `
    <div class="toast-container">
      @for (toast of toastService.toasts(); track toast.id) {
        <div 
          class="toast toast-{{ toast.type }}"
          (click)="toastService.remove(toast.id)">
          <div class="toast-icon">
            @switch (toast.type) {
              @case ('success') {
                <lucide-icon name="check" [size]="20"></lucide-icon>
              }
              @case ('error') {
                <lucide-icon name="x" [size]="20"></lucide-icon>
              }
              @case ('warning') {
                <lucide-icon name="alert-circle" [size]="20"></lucide-icon>
              }
              @case ('info') {
                <lucide-icon name="info" [size]="20"></lucide-icon>
              }
            }
          </div>
          <span class="toast-message">{{ toast.message }}</span>
        </div>
      }
    </div>
  `,
  styles: [`
    .toast-container {
      position: fixed;
      top: 1rem;
      right: 1rem;
      z-index: 9999;
      display: flex;
      flex-direction: column;
      gap: 0.5rem;
      max-width: 400px;
    }

    .toast {
      display: flex;
      align-items: center;
      gap: 0.75rem;
      padding: 1rem;
      border-radius: 0.5rem;
      box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
      cursor: pointer;
      animation: slideIn 0.3s ease-out;
      background: white;
      border-left: 4px solid;
    }

    @keyframes slideIn {
      from {
        transform: translateX(100%);
        opacity: 0;
      }
      to {
        transform: translateX(0);
        opacity: 1;
      }
    }

    .toast-success {
      border-left-color: #10b981;
      color: #065f46;
    }

    .toast-error {
      border-left-color: #ef4444;
      color: #991b1b;
    }

    .toast-warning {
      border-left-color: #f59e0b;
      color: #92400e;
    }

    .toast-info {
      border-left-color: #3b82f6;
      color: #1e40af;
    }

    .toast-icon {
      display: flex;
      align-items: center;
      justify-content: center;
    }

    .toast-message {
      flex: 1;
      font-size: 0.875rem;
      font-weight: 500;
    }
  `]
})
export class ToastContainerComponent {
  toastService = inject(ToastService);
}
