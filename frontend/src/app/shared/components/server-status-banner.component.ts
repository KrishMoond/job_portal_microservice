import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HealthCheckService } from '../../core/services/health-check.service';
import { LucideAngularModule } from 'lucide-angular';

@Component({
  selector: 'app-server-status-banner',
  standalone: true,
  imports: [CommonModule, LucideAngularModule],
  template: `
    @if (!healthService.isServerHealthy()) {
      <div class="banner">
        <div class="banner-content">
          <lucide-icon name="alert-circle" [size]="20"></lucide-icon>
          <span class="banner-message">
            @if (healthService.isChecking()) {
              Connecting to server...
            } @else {
              Server is unavailable. Please try again shortly.
            }
          </span>
          @if (!healthService.isChecking()) {
            <button 
              class="retry-btn"
              (click)="healthService.retryConnection()">
              <lucide-icon name="loader-2" [size]="16" class="spin"></lucide-icon>
              Retry
            </button>
          }
        </div>
      </div>
    }
  `,
  styles: [`
    .banner {
      position: sticky;
      top: 0;
      left: 0;
      right: 0;
      width: 100%;
      z-index: 9998;
      background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
      color: white;
      padding: 0.75rem 1rem;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
      animation: slideDown 0.3s ease-out;
    }

    @keyframes slideDown {
      from {
        transform: translateY(-100%);
      }
      to {
        transform: translateY(0);
      }
    }

    .banner-content {
      max-width: 1200px;
      margin: 0 auto;
      display: flex;
      align-items: center;
      gap: 0.75rem;
    }

    .banner-message {
      flex: 1;
      font-weight: 500;
      font-size: 0.875rem;
    }

    .retry-btn {
      display: flex;
      align-items: center;
      gap: 0.5rem;
      padding: 0.5rem 1rem;
      background: rgba(255, 255, 255, 0.2);
      border: 1px solid rgba(255, 255, 255, 0.3);
      border-radius: 0.375rem;
      color: white;
      font-size: 0.875rem;
      font-weight: 500;
      cursor: pointer;
      transition: all 0.2s;
    }

    .retry-btn:hover {
      background: rgba(255, 255, 255, 0.3);
    }

    .spin {
      animation: spin 1s linear infinite;
    }

    @keyframes spin {
      from {
        transform: rotate(0deg);
      }
      to {
        transform: rotate(360deg);
      }
    }
  `]
})
export class ServerStatusBannerComponent implements OnInit {
  healthService = inject(HealthCheckService);

  ngOnInit() {
    this.healthService.startHealthCheck();
  }
}
