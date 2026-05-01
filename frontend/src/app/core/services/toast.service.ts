import { Injectable, signal } from '@angular/core';

export interface Toast {
  id: number;
  message: string;
  type: 'success' | 'error' | 'warning' | 'info';
  duration: number;
}

@Injectable({
  providedIn: 'root'
})
export class ToastService {
  private toastIdCounter = 0;
  toasts = signal<Toast[]>([]);

  show(message: string, type: Toast['type'], duration = 5000) {
    // queueMicrotask defers the signal update to after Angular's current
    // change-detection pass, preventing NG0100 errors from HTTP callbacks.
    queueMicrotask(() => {
      const id = ++this.toastIdCounter;
      const toast: Toast = { id, message, type, duration };
      this.toasts.update(toasts => [...toasts, toast]);
      setTimeout(() => this.remove(id), duration);
    });
  }

  success(message: string, duration?: number) {
    this.show(message, 'success', duration);
  }

  error(message: string, duration?: number) {
    this.show(message, 'error', duration);
  }

  warning(message: string, duration?: number) {
    this.show(message, 'warning', duration);
  }

  info(message: string, duration?: number) {
    this.show(message, 'info', duration);
  }

  remove(id: number) {
    // Defer to avoid NG0100 if called during a CD pass.
    queueMicrotask(() => {
      this.toasts.update(toasts => toasts.filter(t => t.id !== id));
    });
  }
}
