import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, retry, timer, throwError } from 'rxjs';
import { ToastService } from '../services/toast.service';
import { AuthService } from '../services/auth.service';

// Endpoints that poll silently in the background — errors must NOT show toasts
// to avoid flooding the UI when services are starting up or temporarily unavailable.
const SILENT_ENDPOINTS = [
  '/api/notifications',
  '/api/notifications/unread',
];

function isSilent(url: string): boolean {
  return SILENT_ENDPOINTS.some(ep => url.includes(ep));
}

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const toastService = inject(ToastService);
  const authService = inject(AuthService);
  const silent = isSilent(req.url);

  return next(req).pipe(
    retry({
      count: silent ? 1 : 2, // Fewer retries for background polling
      delay: (error: HttpErrorResponse, retryCount) => {
        // Only retry on server errors (502, 503, 504)
        if ([502, 503, 504].includes(error.status)) {
          if (!silent) console.log(`Retry attempt ${retryCount} for ${req.url}`);
          return timer(silent ? 5000 : 2000); // Longer delay for background polls
        }
        // Don't retry on client errors (4xx)
        throw error;
      }
    }),
    catchError((error: HttpErrorResponse) => {
      let errorMessage = 'An unexpected error occurred';

      // Silent endpoints never show toasts — they fail quietly
      if (silent) {
        return throwError(() => new Error(error.message));
      }

      if (error.status === 0) {
        // Network error
        errorMessage = 'Unable to connect to server. Please check your internet connection.';
        toastService.error(errorMessage);
      } else if (error.status === 503) {
        errorMessage = 'Server is starting up, please try again shortly';
        toastService.warning(errorMessage);
      } else if (error.status === 502 || error.status === 504) {
        errorMessage = 'Gateway timeout. The server is temporarily unavailable.';
        toastService.warning(errorMessage);
      } else if (error.status === 401) {
        errorMessage = 'Unauthorized. Please login again.';
        if (!req.url.includes('/api/users/login') && authService.isLoggedIn()) {
          authService.logout();
        }
        toastService.error(errorMessage);
      } else if (error.status === 403) {
        errorMessage = 'Access denied. You do not have permission.';
        toastService.error(errorMessage);
      } else if (error.status === 404) {
        errorMessage = 'Resource not found.';
        toastService.error(errorMessage);
      } else if (error.status >= 400 && error.status < 500) {
        // Client error - use server message if available
        errorMessage = error.error?.message || error.error?.error || 'Invalid request';
        toastService.error(errorMessage);
      } else if (error.status >= 500) {
        errorMessage = error.error?.message || error.error?.error || 'Server error. Please try again later.';
        toastService.error(errorMessage);
      }

      return throwError(() => new Error(errorMessage));
    })
  );
};
