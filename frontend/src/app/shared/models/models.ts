export interface User {
  userId: string;
  name: string;
  email: string;
  role: 'JOB_SEEKER' | 'RECRUITER' | 'ADMIN';
}

export interface RegisterRequest {
  name: string;
  email: string;
  password: string;
  role: 'JOB_SEEKER' | 'RECRUITER';
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface Job {
  jobId: string;
  title: string;
  companyId: string;
  company: string;
  location: string;
  salary: string;
  description: string;
  category?: string;
  recruiterId: string;
  status: 'OPEN' | 'CLOSED';
  createdAt: string;
}

export interface JobRequest {
  title: string;
  companyId: string;
  location: string;
  salary: string;
  description: string;
  category?: string;
}

export interface Application {
  id: string;
  jobId: string;
  jobTitle: string;
  candidateId: string;
  candidateEmail: string;
  resumeId: string;
  status: 'APPLIED' | 'SHORTLISTED' | 'REJECTED' | 'INTERVIEW_SCHEDULED' | 'HIRED' | 'OFFER_ACCEPTED' | 'OFFER_REJECTED';
  appliedAt: string;
}

export interface Notification {
  id: string;
  userId: string;
  message: string;
  read: boolean;
  createdAt: string;
}

export interface Company {
  id: string;
  name: string;
  description: string;
  website: string;
  logoUrl: string;
  createdByUserId: string;
}

export interface Interview {
  id: string;
  applicationId: string;
  candidateId: string;
  scheduledAt: string;
  meetingLink: string;
  status: string;
}

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
}
