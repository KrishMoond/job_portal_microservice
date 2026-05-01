import { Component, inject } from '@angular/core';
import { RouterOutlet, Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { NavbarComponent } from './shared/components/navbar/navbar.component';
import { ToastComponent } from './shared/components/toast/toast.component';
import { ServerStatusBannerComponent } from './shared/components/server-status-banner.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, CommonModule, NavbarComponent, ToastComponent, ServerStatusBannerComponent],
  template: `
    <div class="flex flex-col min-h-screen bg-gray-50">
      <app-server-status-banner />
      @if (showNavbar) {
        <app-navbar />
      }
      <main class="flex-1 w-full flex flex-col">
        <router-outlet />
      </main>
      <app-toast />
    </div>
  `
})
export class App {
  private router = inject(Router);

  get showNavbar(): boolean {
    const url = this.router.url;
    return !url.includes('/login') && !url.includes('/register');
  }
}
