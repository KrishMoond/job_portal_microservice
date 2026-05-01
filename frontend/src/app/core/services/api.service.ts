import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { ApiResponse, Job, JobRequest, Application, Notification, Company, Interview } from '../../shared/models/models';

@Injectable({ providedIn: 'root' })
export class ApiService {
  private api = environment.apiUrl;
  private http = inject(HttpClient);
  
  private getStoredUser(): { userId?: string; role?: string } | null {
    try {
      const raw = localStorage.getItem('current_user');
      return raw ? JSON.parse(raw) : null;
    } catch {
      return null;
    }
  }

  private normalizeDate(value: unknown): string | unknown {
    // Backend may serialize Java LocalDateTime as an array: [yyyy, mm, dd, hh, mm, ss, nanos?]
    if (Array.isArray(value) && value.length >= 3) {
      const [year, month, day, hour = 0, minute = 0, second = 0] = value as number[];
      const iso = new Date(year, (month as number) - 1, day as number, hour as number, minute as number, second as number).toISOString();
      return iso;
    }
    return value;
  }

  private normalizeJob(job: any): Job {
    if (!job) return job;
    const createdAt = this.normalizeDate(job.createdAt);
    return { ...job, createdAt } as Job;
  }

  private normalizeNotification(n: any): Notification {
    if (!n) return n;
    const createdAt = this.normalizeDate(n.createdAt);
    return { ...n, createdAt } as Notification;
  }

  private normalizeInterview(i: any): Interview {
    if (!i) return i;
    const scheduledAt = this.normalizeDate((i as any).scheduledAt);
    return { ...i, scheduledAt } as Interview;
  }

  // Jobs
  getJobs(): Observable<ApiResponse<Job[]>> {
    return this.http.get<ApiResponse<Job[]>>(`${this.api}/api/jobs`).pipe(
      map(res => ({
        ...res,
        data: Array.isArray(res.data) ? res.data.map(j => this.normalizeJob(j)) : res.data
      }))
    );
  }

  getJobById(id: string): Observable<ApiResponse<Job>> {
    return this.http.get<ApiResponse<Job>>(`${this.api}/api/jobs/${id}`).pipe(
      map(res => ({ ...res, data: this.normalizeJob(res.data) }))
    );
  }

  createJob(data: JobRequest): Observable<ApiResponse<Job>> {
    return this.http.post<ApiResponse<Job>>(`${this.api}/api/jobs`, data);
  }

  closeJob(jobId: string): Observable<ApiResponse<Job>> {
    return this.http.put<ApiResponse<Job>>(`${this.api}/api/jobs/${jobId}/close`, {});
  }

  reopenJob(jobId: string): Observable<ApiResponse<Job>> {
    return this.http.put<ApiResponse<Job>>(`${this.api}/api/jobs/${jobId}/reopen`, {});
  }

  searchJobs(keyword: string, location: string): Observable<ApiResponse<Job[]>> {
    const params: string[] = [];
    if (keyword?.trim()) params.push(`keyword=${encodeURIComponent(keyword.trim())}`);
    if (location?.trim()) params.push(`location=${encodeURIComponent(location.trim())}`);
    const query = params.length ? `?${params.join('&')}` : '';
    return this.http.get<ApiResponse<Job[]>>(`${this.api}/api/search/jobs${query}`).pipe(
      map(res => ({
        ...res,
        data: Array.isArray(res.data) ? res.data.map((j: any) => this.normalizeJob(j)) : res.data
      }))
    );
  }

  /** Returns live category counts from the search-service DB.
   *  [ { category: 'Engineering', count: 14 }, ... ]
   */
  getJobCategories(): Observable<any> {
    return this.http.get<any>(`${this.api}/api/search/categories`);
  }

  updateCompany(id: string, data: Partial<Company>): Observable<any> {
    return this.http.put(`${this.api}/api/companies/${id}`, data);
  }

  updateUserProfile(userId: string, data: { name: string; email: string; password?: string; role: string }): Observable<any> {
    return this.http.put(`${this.api}/api/users/${userId}`, data);
  }

  updateUserProfileDetails(userId: string, data: { name?: string; phone?: string; location?: string; experienceLevel?: string; education?: string; preferredJobTypes?: string }): Observable<any> {
    return this.http.put(`${this.api}/api/users/${userId}/profile`, data);
  }

  // Applications
  applyForJob(jobId: string, candidateId: string, resumeId: string): Observable<any> {
    return this.http.post(`${this.api}/api/applications`, { jobId, candidateId, resumeId });
  }

  getMyApplications(candidateId: string): Observable<ApiResponse<Application[]>> {
    return this.http.get<ApiResponse<Application[]>>(`${this.api}/api/applications/candidate/${candidateId}`);
  }

  getJobApplications(jobId: string): Observable<ApiResponse<Application[]>> {
    return this.http.get<ApiResponse<Application[]>>(`${this.api}/api/applications/job/${jobId}`);
  }

  updateApplicationStatus(appId: string, status: string): Observable<any> {
    return this.http.put(`${this.api}/api/applications/${appId}/status`, { status });
  }

  respondToOffer(appId: string, accepted: boolean): Observable<any> {
    return this.http.post(`${this.api}/api/applications/${appId}/offer-response?accepted=${accepted}`, {});
  }

  // Notifications
  getNotifications(): Observable<ApiResponse<Notification[]>> {
    return this.http.get<ApiResponse<Notification[]>>(`${this.api}/api/notifications`).pipe(
      map(res => ({
        ...res,
        data: Array.isArray(res.data) ? res.data.map(n => this.normalizeNotification(n)) : res.data
      }))
    );
  }

  getUnreadNotifications(): Observable<ApiResponse<Notification[]>> {
    return this.http.get<ApiResponse<Notification[]>>(`${this.api}/api/notifications/unread`).pipe(
      map(res => ({
        ...res,
        data: Array.isArray(res.data) ? res.data.map(n => this.normalizeNotification(n)) : res.data
      }))
    );
  }

  markNotificationRead(id: string): Observable<any> {
    return this.http.put(`${this.api}/api/notifications/${id}/read`, {});
  }

  markAllRead(): Observable<any> {
    return this.http.put(`${this.api}/api/notifications/read-all`, {});
  }

  // Companies
  getUserById(userId: string): Observable<any> {
    return this.http.get(`${this.api}/api/users/${userId}`);
  }

  getCompanies(): Observable<any> {
    return this.http.get(`${this.api}/api/companies`);
  }

  getCompanyById(id: string): Observable<any> {
    return this.http.get(`${this.api}/api/companies/${id}`);
  }

  createCompany(data: Partial<Company>): Observable<any> {
    return this.http.post(`${this.api}/api/companies`, data);
  }

  // Bookmarks
  bookmarkJob(jobId: string): Observable<any> {
    return this.http.post(`${this.api}/api/bookmarks/${jobId}`, {});
  }

  removeBookmark(jobId: string): Observable<any> {
    return this.http.delete(`${this.api}/api/bookmarks/${jobId}`);
  }

  getBookmarks(): Observable<any> {
    return this.http.get(`${this.api}/api/bookmarks`);
  }

  // Interviews
  scheduleInterview(data: any): Observable<any> {
    // Convert datetime-local string to ISO-8601 format for Spring Boot
    const payload = {
      ...data,
      scheduledAt: data.scheduledAt ? new Date(data.scheduledAt).toISOString() : null
    };
    return this.http.post(`${this.api}/api/interviews`, payload);
  }

  getMyInterviews(): Observable<any> {
    return this.http.get(`${this.api}/api/interviews/mine`).pipe(
      map((res: any) => {
        if (res?.data && Array.isArray(res.data)) {
          return { ...res, data: res.data.map((i: any) => this.normalizeInterview(i)) };
        }
        if (Array.isArray(res)) {
          return res.map((i: any) => this.normalizeInterview(i));
        }
        return res;
      })
    );
  }

  // Resumes
  uploadResume(fileUrl: string, fileName: string): Observable<any> {
    const userId = this.getStoredUser()?.userId || '';
    return this.http.post(`${this.api}/api/resumes`, { userId, fileUrl, fileName });
  }

  uploadResumeFile(file: File): Observable<any> {
    const form = new FormData();
    form.append('file', file);
    return this.http.post(`${this.api}/api/resumes/upload`, form);
  }

  downloadResume(resumeId: string): Observable<Blob> {
    return this.http.get(`${this.api}/api/resumes/download/${resumeId}`, {
      responseType: 'blob'
    });
  }

  getMyResumes(userId?: string): Observable<any> {
    const resolvedUserId = userId || this.getStoredUser()?.userId || '';
    return this.http.get(`${this.api}/api/resumes/user/${resolvedUserId}`);
  }

  // Analytics & Recommendations
  getAnalyticsSummary(): Observable<any> {
    const role = this.getStoredUser()?.role || '';
    return this.http.get(`${this.api}/api/analytics/summary`, {
      headers: { 'X-User-Role': role }
    });
  }

  getRecommendations(): Observable<any> {
    const userId = this.getStoredUser()?.userId || '';
    return this.http.get(`${this.api}/api/recommendations`, {
      headers: { 'X-User-Id': userId }
    });
  }
}
