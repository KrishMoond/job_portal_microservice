import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';

export type VerificationStatus = 'PENDING' | 'UNDER_REVIEW' | 'VERIFIED' | 'REJECTED' | 'MORE_INFO_REQUESTED';
export type ReviewDecision = 'APPROVE' | 'REJECT' | 'REQUEST_MORE_INFO';

export interface RiskCheck {
  name: string;
  passed: boolean;
  summary: string;
  expected: string;
  actual: string;
}

export interface ReviewAuditLog {
  id: number;
  reviewerId: string;
  decision: ReviewDecision;
  reason?: string;
  reviewedAt: string;
}

export interface RecruiterSubmission {
  id: number;
  companyName: string;
  workEmail: string;
  emailDomain: string;
  companyWebsite: string;
  registrationNumber: string;
  contactName: string;
  phone?: string;
  documentUrl: string;
  documentType: string;
  status: VerificationStatus;
  submittedAt: string;
  riskChecks: RiskCheck[];
  auditLogs: ReviewAuditLog[];
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  number: number;
  size: number;
}

@Injectable({ providedIn: 'root' })
export class RecruiterVerificationAdminService {
  private http = inject(HttpClient);
  private baseUrl = `${environment.apiUrl}/api/admin/recruiter-verifications`;

  listQueue(page: number, size: number, status?: string): Observable<PageResponse<RecruiterSubmission>> {
    let params = new HttpParams()
      .set('page', page)
      .set('size', size)
      .set('sort', 'submittedAt,asc');
    if (status) params = params.set('status', status);
    return this.http.get<PageResponse<RecruiterSubmission>>(`${this.baseUrl}/queue`, { params });
  }

  getDetail(id: number): Observable<RecruiterSubmission> {
    return this.http.get<RecruiterSubmission>(`${this.baseUrl}/${id}`);
  }

  reviewRecruiter(id: number, decision: ReviewDecision, reason: string, reviewerId: string | null): Observable<RecruiterSubmission> {
    return this.http.post<RecruiterSubmission>(`${this.baseUrl}/${id}/review`, { decision, reason, reviewerId });
  }

  listHistory(page: number, size: number, search: string): Observable<PageResponse<RecruiterSubmission>> {
    let params = new HttpParams()
      .set('page', page)
      .set('size', size)
      .set('sort', 'submittedAt,desc');
    if (search.trim()) params = params.set('search', search.trim());
    return this.http.get<PageResponse<RecruiterSubmission>>(`${this.baseUrl}/history`, { params });
  }
}
