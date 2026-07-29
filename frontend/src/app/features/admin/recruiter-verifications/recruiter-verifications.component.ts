import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, ChangeDetectorRef, Component, Inject, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialog, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatDividerModule } from '@angular/material/divider';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { MatSortModule } from '@angular/material/sort';
import { MatTableModule } from '@angular/material/table';
import { MatTabsModule } from '@angular/material/tabs';
import { AuthService } from '../../../core/services/auth.service';
import {
  RecruiterSubmission,
  RecruiterVerificationAdminService,
  ReviewDecision
} from './recruiter-verification-admin.service';

@Component({
  selector: 'app-recruiter-verifications',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    CommonModule,
    FormsModule,
    MatButtonModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatPaginatorModule,
    MatProgressSpinnerModule,
    MatSelectModule,
    MatSortModule,
    MatTableModule,
    MatTabsModule,
    MatDialogModule
  ],
  template: `
    <main class="admin-shell">
      <header class="admin-header">
        <div>
          <p class="eyebrow">Internal Admin</p>
          <h1>Recruiter Verification</h1>
        </div>
        <button mat-stroked-button type="button" (click)="refresh()">
          <mat-icon>refresh</mat-icon>
          Refresh
        </button>
      </header>

      <mat-tab-group mat-stretch-tabs="false">
        <mat-tab label="Queue">
          <section class="toolbar-row">
            <mat-form-field appearance="outline">
              <mat-label>Status</mat-label>
              <mat-select [(ngModel)]="queueStatus" (selectionChange)="queuePageIndex = 0; loadQueue()">
                <mat-option value="">All open</mat-option>
                <mat-option value="PENDING">Pending</mat-option>
                <mat-option value="UNDER_REVIEW">Under review</mat-option>
              </mat-select>
            </mat-form-field>
          </section>

          <section class="table-wrap">
            <div class="loading" *ngIf="queueLoading"><mat-spinner diameter="34"></mat-spinner></div>
            <table mat-table matSort [dataSource]="queueRows" *ngIf="queueRows.length">
              <ng-container matColumnDef="companyName">
                <th mat-header-cell *matHeaderCellDef>Company</th>
                <td mat-cell *matCellDef="let row">{{ row.companyName }}</td>
              </ng-container>
              <ng-container matColumnDef="workEmail">
                <th mat-header-cell *matHeaderCellDef>Work email</th>
                <td mat-cell *matCellDef="let row">{{ row.workEmail }}</td>
              </ng-container>
              <ng-container matColumnDef="emailDomain">
                <th mat-header-cell *matHeaderCellDef>Domain</th>
                <td mat-cell *matCellDef="let row">{{ row.emailDomain }}</td>
              </ng-container>
              <ng-container matColumnDef="registrationNumber">
                <th mat-header-cell *matHeaderCellDef>Registration</th>
                <td mat-cell *matCellDef="let row">{{ row.registrationNumber }}</td>
              </ng-container>
              <ng-container matColumnDef="submittedAt">
                <th mat-header-cell mat-sort-header *matHeaderCellDef>Submitted</th>
                <td mat-cell *matCellDef="let row">{{ row.submittedAt | date:'mediumDate' }}</td>
              </ng-container>
              <ng-container matColumnDef="status">
                <th mat-header-cell *matHeaderCellDef>Status</th>
                <td mat-cell *matCellDef="let row"><span class="status-chip" [class.review]="row.status === 'UNDER_REVIEW'">{{ row.status }}</span></td>
              </ng-container>
              <ng-container matColumnDef="action">
                <th mat-header-cell *matHeaderCellDef></th>
                <td mat-cell *matCellDef="let row">
                  <button mat-button type="button" (click)="openReview(row)">Review</button>
                </td>
              </ng-container>

              <tr mat-header-row *matHeaderRowDef="queueColumns"></tr>
              <tr mat-row *matRowDef="let row; columns: queueColumns"></tr>
            </table>
            <div class="empty-state" *ngIf="!queueLoading && !queueRows.length">
              <mat-icon>inbox</mat-icon>
              <h2>No submissions to review</h2>
              <p>The queue is clear for the selected status.</p>
            </div>
            <mat-paginator [length]="queueTotal" [pageIndex]="queuePageIndex" [pageSize]="pageSize" [pageSizeOptions]="[5,10,25]" (page)="onQueuePage($event)"></mat-paginator>
          </section>
        </mat-tab>

        <mat-tab label="History">
          <section class="toolbar-row">
            <mat-form-field appearance="outline" class="search-field">
              <mat-label>Search company or email</mat-label>
              <input matInput [(ngModel)]="historySearch" (keyup.enter)="historyPageIndex = 0; loadHistory()">
            </mat-form-field>
            <button mat-flat-button type="button" (click)="historyPageIndex = 0; loadHistory()">Search</button>
          </section>

          <section class="table-wrap">
            <div class="loading" *ngIf="historyLoading"><mat-spinner diameter="34"></mat-spinner></div>
            <table mat-table [dataSource]="historyRows" *ngIf="historyRows.length">
              <ng-container matColumnDef="companyName">
                <th mat-header-cell *matHeaderCellDef>Company</th>
                <td mat-cell *matCellDef="let row">{{ row.companyName }}</td>
              </ng-container>
              <ng-container matColumnDef="workEmail">
                <th mat-header-cell *matHeaderCellDef>Work email</th>
                <td mat-cell *matCellDef="let row">{{ row.workEmail }}</td>
              </ng-container>
              <ng-container matColumnDef="registrationNumber">
                <th mat-header-cell *matHeaderCellDef>Registration</th>
                <td mat-cell *matCellDef="let row">{{ row.registrationNumber }}</td>
              </ng-container>
              <ng-container matColumnDef="submittedAt">
                <th mat-header-cell *matHeaderCellDef>Submitted</th>
                <td mat-cell *matCellDef="let row">{{ row.submittedAt | date:'mediumDate' }}</td>
              </ng-container>
              <ng-container matColumnDef="status">
                <th mat-header-cell *matHeaderCellDef>Decision</th>
                <td mat-cell *matCellDef="let row"><span class="status-chip closed">{{ row.status }}</span></td>
              </ng-container>
              <ng-container matColumnDef="action">
                <th mat-header-cell *matHeaderCellDef></th>
                <td mat-cell *matCellDef="let row">
                  <button mat-button type="button" (click)="openReview(row, true)">View</button>
                </td>
              </ng-container>

              <tr mat-header-row *matHeaderRowDef="historyColumns"></tr>
              <tr mat-row *matRowDef="let row; columns: historyColumns"></tr>
            </table>
            <div class="empty-state" *ngIf="!historyLoading && !historyRows.length">
              <mat-icon>history</mat-icon>
              <h2>No history yet</h2>
              <p>Verified and rejected recruiter accounts will appear here.</p>
            </div>
            <mat-paginator [length]="historyTotal" [pageIndex]="historyPageIndex" [pageSize]="pageSize" [pageSizeOptions]="[5,10,25]" (page)="onHistoryPage($event)"></mat-paginator>
          </section>
        </mat-tab>
      </mat-tab-group>
    </main>
  `,
  styles: [`
    .admin-shell { padding: 24px; max-width: 1280px; margin: 0 auto; }
    .admin-header { display: flex; align-items: center; justify-content: space-between; gap: 16px; margin-bottom: 18px; }
    .admin-header h1 { margin: 0; font-size: 28px; line-height: 1.2; }
    .eyebrow { margin: 0 0 4px; color: #64748b; font-size: 12px; font-weight: 700; text-transform: uppercase; letter-spacing: .08em; }
    .toolbar-row { display: flex; align-items: center; gap: 12px; padding: 18px 0 8px; flex-wrap: wrap; }
    .search-field { min-width: min(420px, 100%); }
    .table-wrap { position: relative; overflow-x: auto; border: 1px solid #e5e7eb; border-radius: 8px; background: #fff; }
    table { width: 100%; min-width: 860px; }
    .loading { position: absolute; inset: 0; min-height: 180px; display: grid; place-items: center; background: rgba(255,255,255,.68); z-index: 1; }
    .status-chip { display: inline-flex; align-items: center; padding: 4px 8px; border-radius: 999px; font-size: 12px; font-weight: 700; background: #fef3c7; color: #92400e; }
    .status-chip.review { background: #dbeafe; color: #1d4ed8; }
    .status-chip.closed { background: #dcfce7; color: #166534; }
    .empty-state { min-height: 220px; display: grid; place-items: center; align-content: center; text-align: center; color: #64748b; padding: 24px; }
    .empty-state mat-icon { width: 42px; height: 42px; font-size: 42px; color: #94a3b8; }
    .empty-state h2 { margin: 10px 0 4px; font-size: 18px; }
    .empty-state p { margin: 0; }
    @media (max-width: 700px) {
      .admin-shell { padding: 16px; }
      .admin-header { align-items: flex-start; flex-direction: column; }
    }
  `]
})
export class RecruiterVerificationsComponent implements OnInit {
  private service = inject(RecruiterVerificationAdminService);
  private dialog = inject(MatDialog);
  private cdr = inject(ChangeDetectorRef);

  queueColumns = ['companyName', 'workEmail', 'emailDomain', 'registrationNumber', 'submittedAt', 'status', 'action'];
  historyColumns = ['companyName', 'workEmail', 'registrationNumber', 'submittedAt', 'status', 'action'];
  queueRows: RecruiterSubmission[] = [];
  historyRows: RecruiterSubmission[] = [];
  queueStatus = '';
  historySearch = '';
  queueLoading = false;
  historyLoading = false;
  queueTotal = 0;
  historyTotal = 0;
  queuePageIndex = 0;
  historyPageIndex = 0;
  pageSize = 10;

  ngOnInit(): void {
    this.loadQueue();
    this.loadHistory();
  }

  refresh(): void {
    this.loadQueue();
    this.loadHistory();
  }

  loadQueue(): void {
    this.queueLoading = true;
    this.service.listQueue(this.queuePageIndex, this.pageSize, this.queueStatus).subscribe({
      next: page => {
        this.queueRows = page.content;
        this.queueTotal = page.totalElements;
        this.queueLoading = false;
        this.cdr.markForCheck();
      },
      error: () => { this.queueLoading = false; this.cdr.markForCheck(); }
    });
  }

  loadHistory(): void {
    this.historyLoading = true;
    this.service.listHistory(this.historyPageIndex, this.pageSize, this.historySearch).subscribe({
      next: page => {
        this.historyRows = page.content;
        this.historyTotal = page.totalElements;
        this.historyLoading = false;
        this.cdr.markForCheck();
      },
      error: () => { this.historyLoading = false; this.cdr.markForCheck(); }
    });
  }

  onQueuePage(event: PageEvent): void {
    this.queuePageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadQueue();
  }

  onHistoryPage(event: PageEvent): void {
    this.historyPageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadHistory();
  }

  openReview(row: RecruiterSubmission, readOnly = false): void {
    this.service.getDetail(row.id).subscribe(detail => {
      const ref = this.dialog.open(RecruiterReviewDialogComponent, {
        width: 'min(1120px, 96vw)',
        maxWidth: '96vw',
        data: { submission: detail, readOnly }
      });
      ref.afterClosed().subscribe(changed => {
        if (changed) this.refresh();
      });
    });
  }
}

@Component({
  selector: 'app-recruiter-review-dialog',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatButtonModule,
    MatDialogModule,
    MatDividerModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatProgressSpinnerModule
  ],
  template: `
    <h2 mat-dialog-title>{{ submission.companyName }}</h2>
    <mat-dialog-content>
      <div class="detail-grid">
        <section class="facts">
          <h3>Submitted Fields</h3>
          <dl>
            <div><dt>Contact</dt><dd>{{ submission.contactName }}</dd></div>
            <div><dt>Work email</dt><dd>{{ submission.workEmail }}</dd></div>
            <div><dt>Email domain</dt><dd>{{ submission.emailDomain }}</dd></div>
            <div><dt>Company website</dt><dd>{{ submission.companyWebsite }}</dd></div>
            <div><dt>Registration</dt><dd>{{ submission.registrationNumber }}</dd></div>
            <div><dt>Phone</dt><dd>{{ submission.phone || 'Not supplied' }}</dd></div>
            <div><dt>Status</dt><dd>{{ submission.status }}</dd></div>
            <div><dt>Submitted</dt><dd>{{ submission.submittedAt | date:'medium' }}</dd></div>
          </dl>

          <mat-divider></mat-divider>

          <h3>Risk Flags</h3>
          <div class="risk-list">
            <article class="risk-item" *ngFor="let check of submission.riskChecks" [class.failed]="!check.passed">
              <mat-icon>{{ check.passed ? 'check_circle' : 'error' }}</mat-icon>
              <div>
                <strong>{{ check.name }}</strong>
                <p>{{ check.summary }}</p>
                <small>Expected: {{ check.expected }} | Actual: {{ check.actual }}</small>
              </div>
            </article>
          </div>
        </section>

        <section class="document">
          <h3>Uploaded Document</h3>
          <img *ngIf="isImage" [src]="submission.documentUrl" alt="Uploaded verification document preview">
          <iframe *ngIf="isPdf" [src]="safeDocumentUrl" title="Uploaded verification PDF"></iframe>
          <div class="unsupported" *ngIf="!isImage && !isPdf">
            <mat-icon>description</mat-icon>
            <p>Preview is not available for {{ submission.documentType }}.</p>
          </div>
        </section>
      </div>

      <section class="audit">
        <h3>Audit Trail</h3>
        <div class="empty-audit" *ngIf="!submission.auditLogs.length">No decisions logged yet.</div>
        <article class="audit-row" *ngFor="let log of submission.auditLogs">
          <strong>{{ log.decision }}</strong>
          <span>{{ log.reviewedAt | date:'medium' }}</span>
          <span>Reviewer: {{ log.reviewerId }}</span>
          <p *ngIf="log.reason">{{ log.reason }}</p>
        </article>
      </section>

      <mat-form-field appearance="outline" class="reason-field" *ngIf="!data.readOnly">
        <mat-label>Reason for reject or request more info</mat-label>
        <textarea matInput rows="3" [(ngModel)]="reason"></textarea>
      </mat-form-field>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button mat-dialog-close type="button">Close</button>
      <button mat-stroked-button type="button" *ngIf="!data.readOnly" [disabled]="saving" (click)="submit('REQUEST_MORE_INFO')">Request more info</button>
      <button mat-stroked-button color="warn" type="button" *ngIf="!data.readOnly" [disabled]="saving" (click)="submit('REJECT')">Reject</button>
      <button mat-flat-button color="primary" type="button" *ngIf="!data.readOnly" [disabled]="saving" (click)="submit('APPROVE')">Approve</button>
    </mat-dialog-actions>
  `,
  styles: [`
    h3 { margin: 0 0 12px; font-size: 15px; }
    .detail-grid { display: grid; grid-template-columns: minmax(280px, 0.9fr) minmax(320px, 1.1fr); gap: 20px; }
    dl { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 12px; margin: 0 0 18px; }
    dt { color: #64748b; font-size: 12px; font-weight: 700; text-transform: uppercase; }
    dd { margin: 3px 0 0; color: #111827; overflow-wrap: anywhere; }
    .risk-list { display: grid; gap: 10px; }
    .risk-item { display: flex; gap: 10px; padding: 12px; border: 1px solid #bbf7d0; border-radius: 8px; background: #f0fdf4; color: #166534; }
    .risk-item.failed { border-color: #fed7aa; background: #fff7ed; color: #9a3412; }
    .risk-item p { margin: 2px 0; color: #334155; }
    .risk-item small { color: #64748b; }
    .document img, .document iframe { width: 100%; height: 560px; border: 1px solid #e5e7eb; border-radius: 8px; background: #f8fafc; object-fit: contain; }
    .unsupported { height: 260px; display: grid; place-items: center; align-content: center; border: 1px dashed #cbd5e1; border-radius: 8px; color: #64748b; }
    .audit { margin-top: 20px; }
    .audit-row { display: flex; gap: 12px; align-items: baseline; flex-wrap: wrap; padding: 10px 0; border-top: 1px solid #e5e7eb; }
    .audit-row p { flex-basis: 100%; margin: 0; color: #475569; }
    .empty-audit { color: #64748b; padding: 10px 0; }
    .reason-field { width: 100%; margin-top: 18px; }
    @media (max-width: 820px) {
      .detail-grid { grid-template-columns: 1fr; }
      dl { grid-template-columns: 1fr; }
      .document img, .document iframe { height: 420px; }
    }
  `]
})
export class RecruiterReviewDialogComponent {
  private service = inject(RecruiterVerificationAdminService);
  private auth = inject(AuthService);
  private sanitizer = inject(DomSanitizer);
  private dialogRef = inject(MatDialogRef<RecruiterReviewDialogComponent>);

  submission: RecruiterSubmission;
  reason = '';
  saving = false;
  safeDocumentUrl: SafeResourceUrl;

  constructor(@Inject(MAT_DIALOG_DATA) public data: { submission: RecruiterSubmission; readOnly: boolean }) {
    this.submission = data.submission;
    this.safeDocumentUrl = this.sanitizer.bypassSecurityTrustResourceUrl(this.submission.documentUrl);
  }

  get isImage(): boolean {
    return this.submission.documentType.startsWith('image/');
  }

  get isPdf(): boolean {
    return this.submission.documentType === 'application/pdf';
  }

  submit(decision: ReviewDecision): void {
    if ((decision === 'REJECT' || decision === 'REQUEST_MORE_INFO') && !this.reason.trim()) {
      return;
    }
    this.saving = true;
    this.service.reviewRecruiter(this.submission.id, decision, this.reason.trim(), this.auth.getUserId()).subscribe({
      next: () => this.dialogRef.close(true),
      error: () => this.saving = false
    });
  }
}
