import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { ToastContainerComponent } from './shared/components/toast-container.component';
import { ServerStatusBannerComponent } from './shared/components/server-status-banner.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, ToastContainerComponent, ServerStatusBannerComponent],
  template: `
    <app-server-status-banner />
    <app-toast-container />
    <router-outlet />
  `,
  styles: []
})
export class AppComponent {
  title = 'job-portal';
}
