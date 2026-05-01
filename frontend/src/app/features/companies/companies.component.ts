import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { LucideAngularModule } from 'lucide-angular';
import { ApiService } from '../../core/services/api.service';
import { ToastService } from '../../core/services/toast.service';

@Component({
  selector: 'app-companies',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule, LucideAngularModule],
  template: `
    <div class="min-h-screen bg-gray-50 py-10 px-6">
      <div class="max-w-7xl mx-auto">

        <!-- Header -->
        <div class="mb-8">
          <h1 class="text-2xl font-bold text-gray-900 tracking-tight">Companies</h1>
          <p class="text-gray-500 text-sm mt-1">Discover companies hiring on HireHub.</p>
        </div>

        <!-- Search -->
        <div class="relative mb-8 max-w-md">
          <lucide-icon name="search" class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400"></lucide-icon>
          <input [(ngModel)]="searchTerm" (ngModelChange)="filterCompanies()"
            placeholder="Search companies..."
            class="w-full pl-9 pr-4 py-2.5 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-primary focus:outline-none" />
        </div>

        @if (loading()) {
          <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
            @for (sk of [1,2,3,4,5,6]; track sk) {
              <div class="bg-white rounded-2xl border border-gray-200 p-6 animate-pulse">
                <div class="flex items-center gap-4 mb-4">
                  <div class="w-14 h-14 rounded-xl bg-gray-200"></div>
                  <div class="flex-1 space-y-2">
                    <div class="h-4 bg-gray-200 rounded w-3/4"></div>
                    <div class="h-3 bg-gray-100 rounded w-1/2"></div>
                  </div>
                </div>
                <div class="h-12 bg-gray-100 rounded"></div>
              </div>
            }
          </div>
        } @else if (filtered().length === 0) {
          <div class="text-center py-20 bg-white rounded-2xl border border-gray-200">
            <lucide-icon name="building-2" class="w-16 h-16 text-gray-300 mx-auto mb-4"></lucide-icon>
            <h3 class="text-lg font-semibold text-gray-900 mb-1">No companies found</h3>
            <p class="text-gray-500 text-sm">Try a different search term.</p>
          </div>
        } @else {
          <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
            @for (company of filtered(); track company.id) {
              <a [routerLink]="['/companies', company.id]"
                class="bg-white rounded-2xl border border-gray-200 p-6 hover:shadow-md hover:-translate-y-0.5 transition-all duration-200 block group">
                <div class="flex items-center gap-4 mb-4">
                  <!-- Logo -->
                  <div class="w-14 h-14 rounded-xl border border-gray-100 bg-gray-50 flex items-center justify-center overflow-hidden flex-shrink-0">
                    @if (company.logoUrl) {
                      <img [src]="company.logoUrl" [alt]="company.name" class="w-full h-full object-cover" />
                    } @else {
                      <span class="text-xl font-black text-gray-400">{{ company.name.substring(0,2).toUpperCase() }}</span>
                    }
                  </div>
                  <div class="min-w-0">
                    <h3 class="font-bold text-gray-900 group-hover:text-primary transition-colors truncate">{{ company.name }}</h3>
                    @if (company.website) {
                      <p class="text-xs text-gray-400 truncate">{{ company.website }}</p>
                    }
                  </div>
                </div>
                <p class="text-sm text-gray-600 line-clamp-2 leading-relaxed">
                  {{ company.description || 'No description available.' }}
                </p>
                <div class="mt-4 pt-4 border-t border-gray-100 flex items-center justify-between">
                  <span class="text-xs text-gray-400">View jobs &rarr;</span>
                  <lucide-icon name="arrow-right" class="w-4 h-4 text-gray-300 group-hover:text-primary transition-colors"></lucide-icon>
                </div>
              </a>
            }
          </div>
        }
      </div>
    </div>
  `
})
export class CompaniesComponent implements OnInit {
  private api = inject(ApiService);
  private toast = inject(ToastService);

  private companies = signal<any[]>([]);
  filtered = signal<any[]>([]);
  loading = signal(true);
  searchTerm = '';

  ngOnInit(): void {
    this.api.getCompanies().subscribe({
      next: (res: any) => {
        const list = Array.isArray(res) ? res : (res?.data ?? []);
        this.companies.set(list);
        this.filtered.set(list);
        this.loading.set(false);
      },
      error: () => {
        this.toast.error('Failed to load companies');
        this.loading.set(false);
      }
    });
  }

  filterCompanies(): void {
    const term = this.searchTerm.toLowerCase().trim();
    if (!term) {
      this.filtered.set(this.companies());
      return;
    }
    this.filtered.set(
      this.companies().filter(c =>
        c.name?.toLowerCase().includes(term) ||
        c.description?.toLowerCase().includes(term)
      )
    );
  }
}
