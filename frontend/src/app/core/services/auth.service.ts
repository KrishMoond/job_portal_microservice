import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { User, LoginRequest, RegisterRequest, ApiResponse } from '../../shared/models/models';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly TOKEN_KEY = 'jwt_token';
  private readonly USER_KEY = 'current_user';
  private http = inject(HttpClient);
  private router = inject(Router);
  private currentUserSubject = new BehaviorSubject<User | null>(this.getStoredUser());
  currentUser$ = this.currentUserSubject.asObservable();

  register(data: RegisterRequest): Observable<ApiResponse<User>> {
    return this.http.post<ApiResponse<User>>(`${environment.apiUrl}/api/users/register`, data);
  }

  login(data: LoginRequest): Observable<any> {
    return this.http.post<ApiResponse<any>>(`${environment.apiUrl}/api/users/login`, data, { observe: 'response' }).pipe(
      tap(response => {
        const token = response.headers.get('Authorization')?.replace('Bearer ', '');
        if (!token) {
          this.clearSession();
          return;
        }

        const user = response.body?.data;
        if (user) {
          this.setSession(token, user);
          const userId = user.userId;
          if (userId) {
            // Fetch full profile details (name/email) for navbar/profile screens.
            this.http.get<ApiResponse<User>>(`${environment.apiUrl}/api/users/${userId}`).subscribe({
              next: (profileRes) => {
                const fullUser = { ...user, ...(profileRes.data || {}) };
                localStorage.setItem(this.USER_KEY, JSON.stringify(fullUser));
                this.currentUserSubject.next(fullUser);
              },
              error: () => {
                // Keep minimal login payload if profile fetch fails.
              }
            });
          }
        }
      })
    );
  }

  logout(): void {
    this.clearSession();
    this.router.navigate(['/login']);
  }

  getToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }

  isLoggedIn(): boolean {
    return !!this.getToken();
  }

  getCurrentUser(): User | null {
    return this.currentUserSubject.value;
  }

  getUserId(): string | null {
    const stored = this.getCurrentUser() as any;
    const id = stored?.userId || stored?.id;
    if (typeof id === 'string' && id.trim()) return id;

    const token = this.getToken();
    if (!token) return null;
    try {
      // JWT: header.payload.signature (payload is base64url JSON)
      const payload = token.split('.')[1];
      if (!payload) return null;
      const base64 = payload.replace(/-/g, '+').replace(/_/g, '/');
      const padded = base64 + '='.repeat((4 - (base64.length % 4)) % 4);
      const json = decodeURIComponent(
        atob(padded)
          .split('')
          .map(c => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
          .join('')
      );
      const parsed = JSON.parse(json);
      const sub = parsed?.sub;
      return typeof sub === 'string' && sub.trim() ? sub : null;
    } catch {
      return null;
    }
  }

  getRole(): string | null {
    return this.getCurrentUser()?.role ?? null;
  }

  isRecruiter(): boolean {
    return this.getRole() === 'RECRUITER';
  }

  isJobSeeker(): boolean {
    return this.getRole() === 'JOB_SEEKER';
  }

  private getStoredUser(): User | null {
    const stored = localStorage.getItem(this.USER_KEY);
    if (!stored) return null;
    try {
      return JSON.parse(stored);
    } catch {
      this.clearSession();
      return null;
    }
  }

  private setSession(token: string, user: any): void {
    localStorage.setItem(this.TOKEN_KEY, token);
    localStorage.setItem(this.USER_KEY, JSON.stringify(user));
    this.currentUserSubject.next(user);
  }

  private clearSession(): void {
    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem(this.USER_KEY);
    this.currentUserSubject.next(null);
  }
}
