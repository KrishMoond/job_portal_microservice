import { Injectable, signal, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { interval, catchError, of, tap } from 'rxjs';
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

    this.http.get(this.healthCheckUrl, { 
      observe: 'response'
    }).pipe(
      tap(() => {
        this.isServerHealthy.set(true);
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
