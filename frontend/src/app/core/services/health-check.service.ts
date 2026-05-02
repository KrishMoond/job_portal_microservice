import { Injectable, signal, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { interval, catchError, of, tap, timeout } from 'rxjs';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class HealthCheckService {
  private http = inject(HttpClient);
  
  isServerHealthy = signal<boolean>(true);
  isChecking = signal<boolean>(false);
  lastCheckTime = signal<Date | null>(null);

  private healthCheckUrl = `${environment.apiUrl}/actuator/health`;

  startHealthCheck() {
    this.checkHealth();
    interval(30000).subscribe(() => this.checkHealth());
  }

  checkHealth() {
    this.isChecking.set(true);

    this.http.get<{ status?: string }>(this.healthCheckUrl, {
      observe: 'response'
    }).pipe(
      timeout(5000),
      tap(response => {
        const healthy = response.status >= 200
          && response.status < 300
          && response.body?.status === 'UP';

        this.isServerHealthy.set(healthy);
        this.lastCheckTime.set(new Date());
        this.isChecking.set(false);
      }),
      catchError(() => {
        this.isServerHealthy.set(false);
        this.lastCheckTime.set(new Date());
        this.isChecking.set(false);
        return of(null);
      })
    ).subscribe();
  }

  retryConnection() {
    this.checkHealth();
  }
}
