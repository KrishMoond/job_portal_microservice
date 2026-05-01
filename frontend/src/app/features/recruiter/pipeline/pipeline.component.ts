import { Component, OnInit, inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { CdkDragDrop, DragDropModule, moveItemInArray, transferArrayItem } from '@angular/cdk/drag-drop';
import { LucideAngularModule } from 'lucide-angular';
import { ApiService } from '../../../core/services/api.service';
import { ToastService } from '../../../core/services/toast.service';
import { Application } from '../../../shared/models/models';

interface Column {
  id: string;
  title: string;
  items: Application[];
  color: string;
  bgColor: string;
  borderColor: string;
}

@Component({
  selector: 'app-pipeline',
  standalone: true,
  imports: [CommonModule, RouterLink, LucideAngularModule, DragDropModule],
  styles: [`
    .board-container {
      display: flex;
      gap: 16px;
      overflow-x: auto;
      padding-bottom: 20px;
    }
    .column {
      min-width: 280px;
      width: 280px;
      flex-shrink: 0;
    }
    .column-header {
      background: white;
      padding: 12px 16px;
      border-radius: 12px 12px 0 0;
      border-top: 4px solid;
      box-shadow: 0 1px 3px rgba(0,0,0,0.08);
      margin-bottom: 8px;
    }
    .drop-list {
      min-height: 200px;
      background: rgba(249,250,251,0.8);
      border-radius: 0 0 12px 12px;
      padding: 8px;
    }
    .drop-list.cdk-drop-list-dragging {
      background: rgba(108,99,255,0.05);
    }
    .card {
      background: white;
      border: 1px solid #e5e7eb;
      border-radius: 10px;
      padding: 14px;
      margin-bottom: 10px;
      cursor: move;
      box-shadow: 0 1px 3px rgba(0,0,0,0.06);
      transition: box-shadow 0.2s;
    }
    .card:hover {
      box-shadow: 0 4px 12px rgba(0,0,0,0.1);
    }
    .cdk-drag-preview {
      box-shadow: 0 8px 24px rgba(0,0,0,0.15);
      opacity: 0.9;
    }
    .cdk-drag-placeholder {
      opacity: 0.3;
    }
    .cdk-drag-animating {
      transition: transform 250ms cubic-bezier(0, 0, 0.2, 1);
    }
    .drop-list.cdk-drop-list-dragging .card:not(.cdk-drag-placeholder) {
      transition: transform 250ms cubic-bezier(0, 0, 0.2, 1);
    }
    .empty-state {
      border: 2px dashed #d1d5db;
      border-radius: 10px;
      padding: 32px 16px;
      text-align: center;
      color: #9ca3af;
      background: rgba(255,255,255,0.5);
    }
  `],
  template: `
    <div style="min-height:100vh;background:#f9fafb;padding:40px 24px 64px">
      <div style="max-width:1500px;margin:0 auto">

        <a routerLink="/recruiter/dashboard"
           style="display:inline-flex;align-items:center;gap:4px;font-size:14px;color:#6b7280;text-decoration:none;margin-bottom:24px">
          <lucide-icon name="arrow-left" [size]="16"></lucide-icon>
          Back to Dashboard
        </a>

        <!-- Header -->
        <div style="display:flex;align-items:center;justify-content:space-between;margin-bottom:32px;flex-wrap:wrap;gap:16px">
          <div>
            <h1 style="font-size:24px;font-weight:800;color:#111827;margin:0">Applicant Pipeline</h1>
            <p style="font-size:14px;color:#6b7280;margin:4px 0 0">Drag candidates between stages to update their status.</p>
          </div>
          <div style="display:flex;align-items:center;gap:16px">
            <div style="text-align:right">
              <p style="font-size:12px;color:#6b7280;margin:0">Total Applicants</p>
              <p style="font-size:28px;font-weight:900;color:#111827;margin:0">{{ getTotalCount() }}</p>
            </div>
            <div style="width:1px;height:40px;background:#e5e7eb"></div>
            <div style="display:flex;gap:12px">
              @for (col of columns; track col.id) {
                <div style="text-align:center">
                  <div [style.background]="col.color" style="width:10px;height:10px;border-radius:50%;margin:0 auto 4px"></div>
                  <span style="font-size:12px;font-weight:700;color:#374151">{{ col.items.length }}</span>
                </div>
              }
            </div>
          </div>
        </div>

        <!-- Loading -->
        @if (loading) {
          <div class="board-container">
            @for (i of [1,2,3,4,5]; track i) {
              <div class="column">
                <div style="background:#fff;padding:12px;border-radius:12px;margin-bottom:8px">
                  <div style="height:16px;background:#e5e7eb;border-radius:6px;width:80px;animation:pulse 1.5s infinite"></div>
                </div>
                <div style="height:100px;background:#f3f4f6;border-radius:10px;margin-bottom:8px;animation:pulse 1.5s infinite"></div>
                <div style="height:100px;background:#f3f4f6;border-radius:10px;animation:pulse 1.5s infinite"></div>
              </div>
            }
          </div>
        }

        <!-- Kanban Board -->
        @if (!loading) {
          <div class="board-container" cdkDropListGroup>
            @for (col of columns; track col.id) {
              <div class="column">

                <!-- Column Header -->
                <div class="column-header" [style.border-top-color]="col.color">
                  <div style="display:flex;align-items:center;gap:8px">
                    <div [style.background]="col.color" style="width:10px;height:10px;border-radius:50%;flex-shrink:0"></div>
                    <span style="font-size:14px;font-weight:700;color:#111827">{{ col.title }}</span>
                    <span [style.color]="col.color"
                          [style.background]="col.bgColor"
                          [style.border-color]="col.borderColor"
                          style="margin-left:auto;font-size:12px;font-weight:700;padding:2px 10px;border-radius:999px;border:1px solid">
                      {{ col.items.length }}
                    </span>
                  </div>
                </div>

                <!-- Drop List -->
                <div class="drop-list"
                     cdkDropList
                     [id]="col.id"
                     [cdkDropListData]="col.items"
                     (cdkDropListDropped)="onDrop($event)">

                  @for (app of col.items; track app.id) {
                    <div class="card" cdkDrag>

                      <!-- Avatar + Email -->
                      <div style="display:flex;align-items:center;gap:10px;margin-bottom:10px">
                        <div style="width:36px;height:36px;border-radius:50%;background:linear-gradient(135deg,#6c63ff,#8b5cf6);display:flex;align-items:center;justify-content:center;color:#fff;font-weight:700;font-size:14px;flex-shrink:0">
                          {{ getInitial(app.candidateEmail) }}
                        </div>
                        <div style="min-width:0;flex:1">
                          <p style="font-size:14px;font-weight:600;color:#111827;margin:0;overflow:hidden;text-overflow:ellipsis;white-space:nowrap">
                            {{ app.candidateEmail }}
                          </p>
                          <p style="font-size:12px;color:#9ca3af;margin:2px 0 0">
                            {{ app.appliedAt | date:'mediumDate' }}
                          </p>
                        </div>
                      </div>

                      <!-- Stage Badge -->
                      <span [style.color]="col.color"
                            [style.background]="col.bgColor"
                            [style.border-color]="col.borderColor"
                            style="display:inline-block;font-size:10px;font-weight:700;text-transform:uppercase;letter-spacing:0.05em;padding:2px 8px;border-radius:6px;border:1px solid;margin-bottom:10px">
                        {{ col.title }}
                      </span>

                      <!-- Actions -->
                      <div style="display:flex;align-items:center;gap:8px;padding-top:8px;border-top:1px solid #f3f4f6">
                        @if (app.resumeId) {
                          <button (click)="openResume(app.resumeId)"
                                  style="font-size:12px;color:#6c63ff;font-weight:600;background:none;border:none;cursor:pointer;display:flex;align-items:center;gap:4px;padding:0"
                                  aria-label="Open resume">
                            <lucide-icon name="paperclip" [size]="12"></lucide-icon> Resume
                          </button>
                        }
                        @if (app.status !== 'HIRED' && app.status !== 'REJECTED') {
                          <div style="margin-left:auto;display:flex;gap:6px">
                            <button (click)="quickHire(app)"
                                    style="width:28px;height:28px;display:flex;align-items:center;justify-content:center;border-radius:8px;background:#f0fdf4;color:#15803d;border:1px solid #bbf7d0;cursor:pointer"
                                    title="Mark as Hired"
                                    aria-label="Mark candidate as hired">
                              <lucide-icon name="check" [size]="14"></lucide-icon>
                            </button>
                            <button (click)="quickReject(app)"
                                    style="width:28px;height:28px;display:flex;align-items:center;justify-content:center;border-radius:8px;background:#fef2f2;color:#dc2626;border:1px solid #fecaca;cursor:pointer"
                                    title="Reject"
                                    aria-label="Reject candidate">
                              <lucide-icon name="x" [size]="14"></lucide-icon>
                            </button>
                          </div>
                        }
                      </div>

                    </div>
                  }

                  @if (col.items.length === 0) {
                    <div class="empty-state">
                      <lucide-icon name="inbox" [size]="28" style="margin:0 auto 8px;display:block;opacity:0.3"></lucide-icon>
                      <p style="font-size:12px;font-weight:500;margin:0">Drop here</p>
                    </div>
                  }

                </div>

              </div>
            }
          </div>
        }

      </div>
    </div>
  `
})
export class PipelineComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private api = inject(ApiService);
  private toast = inject(ToastService);
  private cdr = inject(ChangeDetectorRef);

  loading = true;
  columns: Column[] = [
    { id: 'APPLIED', title: 'Applied', items: [], color: '#3b82f6', bgColor: '#eff6ff', borderColor: '#bfdbfe' },
    { id: 'SHORTLISTED', title: 'Shortlisted', items: [], color: '#8b5cf6', bgColor: '#f5f3ff', borderColor: '#ddd6fe' },
    { id: 'INTERVIEW_SCHEDULED', title: 'Interview', items: [], color: '#f59e0b', bgColor: '#fffbeb', borderColor: '#fde68a' },
    { id: 'HIRED', title: 'Hired', items: [], color: '#10b981', bgColor: '#f0fdf4', borderColor: '#bbf7d0' },
    { id: 'REJECTED', title: 'Rejected', items: [], color: '#ef4444', bgColor: '#fef2f2', borderColor: '#fecaca' },
  ];

  private jobId = '';

  ngOnInit(): void {
    this.jobId = this.route.snapshot.paramMap.get('jobId') || '';
    if (!this.jobId) {
      this.loading = false;
      return;
    }

    this.loadApplications();
  }

  private loadApplications(): void {
    this.api.getJobApplications(this.jobId).subscribe({
      next: (res: any) => {
        let apps: any[] = [];
        if (Array.isArray(res)) apps = res;
        else if (Array.isArray(res?.data)) apps = res.data;
        else if (Array.isArray(res?.content)) apps = res.content;

        const normalized = apps.map(a => this.normalizeApp(a));

        // Distribute apps into columns
        this.columns.forEach(col => {
          col.items = normalized.filter(app => app.status === col.id);
        });

        this.loading = false;
        this.cdr.detectChanges(); // Force change detection
      },
      error: () => {
        this.toast.error('Failed to load applicants');
        this.loading = false;
        this.cdr.detectChanges(); // Force change detection
      }
    });
  }

  private normalizeApp(a: any): Application {
    // Normalize date
    let appliedAt = a.appliedAt;
    if (Array.isArray(appliedAt) && appliedAt.length >= 3) {
      const [y, mo, d, h = 0, mi = 0, s = 0] = appliedAt;
      appliedAt = new Date(y, mo - 1, d, h, mi, s).toISOString();
    }
    // Normalize status to uppercase
    const status = String(a.status || 'APPLIED').toUpperCase().replace(/ /g, '_');
    return { ...a, appliedAt, status } as Application;
  }

  onDrop(event: CdkDragDrop<Application[]>): void {
    if (event.previousContainer === event.container) {
      // Same column - just reorder
      moveItemInArray(event.container.data, event.previousIndex, event.currentIndex);
      this.cdr.detectChanges();
    } else {
      // Different column - move and update status
      const app = event.previousContainer.data[event.previousIndex];
      const newStatus = event.container.id;

      transferArrayItem(
        event.previousContainer.data,
        event.container.data,
        event.previousIndex,
        event.currentIndex
      );

      this.cdr.detectChanges(); // Force UI update
      
      // Update backend
      this.updateStatus(app, newStatus);
    }
  }

  private updateStatus(app: Application, newStatus: string): void {
    const oldStatus = app.status;
    app.status = newStatus as any;

    this.api.updateApplicationStatus(app.id, newStatus).subscribe({
      next: () => {
        this.toast.success(`Moved to ${newStatus.replace(/_/g, ' ')}`);
      },
      error: () => {
        // Rollback
        app.status = oldStatus;
        this.toast.error('Failed to update status');
        // Reload to fix UI
        this.loadApplications();
      }
    });
  }

  quickHire(app: Application): void {
    const fromCol = this.columns.find(c => c.items.includes(app));
    const toCol = this.columns.find(c => c.id === 'HIRED');
    if (!fromCol || !toCol) return;

    const index = fromCol.items.indexOf(app);
    fromCol.items.splice(index, 1);
    toCol.items.push(app);

    this.cdr.detectChanges(); // Force UI update
    this.updateStatus(app, 'HIRED');
  }

  quickReject(app: Application): void {
    const fromCol = this.columns.find(c => c.items.includes(app));
    const toCol = this.columns.find(c => c.id === 'REJECTED');
    if (!fromCol || !toCol) return;

    const index = fromCol.items.indexOf(app);
    fromCol.items.splice(index, 1);
    toCol.items.push(app);

    this.cdr.detectChanges(); // Force UI update
    this.updateStatus(app, 'REJECTED');
  }

  openResume(resumeId: string): void {
    this.api.downloadResume(resumeId).subscribe({
      next: (blob: Blob) => {
        const url = URL.createObjectURL(blob);
        window.open(url, '_blank', 'noopener,noreferrer');
        setTimeout(() => URL.revokeObjectURL(url), 60_000);
      },
      error: () => this.toast.error('Unable to open resume')
    });
  }

  getTotalCount(): number {
    return this.columns.reduce((sum, col) => sum + col.items.length, 0);
  }

  getInitial(email: string): string {
    return (email || '?').charAt(0).toUpperCase();
  }
}
