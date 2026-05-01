import { Routes } from '@angular/router';
import { authGuard, roleGuard } from './core/guards/auth.guard';

export const routes: Routes = [
  { 
    path: '', 
    loadComponent: () => import('./features/landing/landing/landing').then(m => m.Landing)
  },
  {
    path: 'login',
    loadComponent: () => import('./features/auth/login/login.component').then(m => m.LoginComponent)
  },
  {
    path: 'register',
    loadComponent: () => import('./features/auth/register/register.component').then(m => m.RegisterComponent)
  },
  {
    path: 'jobs',
    loadComponent: () => import('./features/jobs/job-list/job-list.component').then(m => m.JobListComponent),
    canActivate: [authGuard]
  },
  {
    path: 'jobs/:id',
    loadComponent: () => import('./features/jobs/job-detail/job-detail.component').then(m => m.JobDetailComponent),
    canActivate: [authGuard]
  },
  // Companies
  {
    path: 'companies',
    loadComponent: () => import('./features/companies/companies.component').then(m => m.CompaniesComponent),
    canActivate: [authGuard]
  },
  {
    path: 'companies/:id',
    loadComponent: () => import('./features/companies/company-detail.component').then(m => m.CompanyDetailComponent),
    canActivate: [authGuard]
  },
  // Notifications
  {
    path: 'notifications',
    loadComponent: () => import('./features/notifications/notifications.component').then(m => m.NotificationsComponent),
    canActivate: [authGuard]
  },
  // Seeker
  {
    path: 'seeker/applications',
    loadComponent: () => import('./features/applications/my-applications/my-applications.component').then(m => m.MyApplicationsComponent),
    canActivate: [authGuard, roleGuard('JOB_SEEKER')]
  },
  {
    path: 'seeker/bookmarks',
    loadComponent: () => import('./features/jobs/bookmarks/bookmarks.component').then(m => m.BookmarksComponent),
    canActivate: [authGuard, roleGuard('JOB_SEEKER')]
  },
  {
    path: 'seeker/profile',
    loadComponent: () => import('./features/seeker/profile/profile.component').then(m => m.ProfileComponent),
    canActivate: [authGuard, roleGuard('JOB_SEEKER')]
  },
  // Recruiter
  {
    path: 'recruiter/dashboard',
    loadComponent: () => import('./features/recruiter/dashboard/recruiter-dashboard.component').then(m => m.RecruiterDashboardComponent),
    canActivate: [authGuard, roleGuard('RECRUITER')]
  },
  {
    path: 'recruiter/post-job',
    loadComponent: () => import('./features/jobs/job-create/job-create.component').then(m => m.JobCreateComponent),
    canActivate: [authGuard, roleGuard('RECRUITER')]
  },
  {
    path: 'recruiter/company',
    loadComponent: () => import('./features/recruiter/company-create/company-create.component').then(m => m.CompanyCreateComponent),
    canActivate: [authGuard, roleGuard('RECRUITER')]
  },
  {
    path: 'recruiter/company/:id/edit',
    loadComponent: () => import('./features/recruiter/company-edit/company-edit.component').then(m => m.CompanyEditComponent),
    canActivate: [authGuard, roleGuard('RECRUITER')]
  },
  {
    path: 'recruiter/applicants/:jobId',
    loadComponent: () => import('./features/recruiter/applicants/applicants.component').then(m => m.ApplicantsComponent),
    canActivate: [authGuard, roleGuard('RECRUITER')]
  },
  {
    path: 'recruiter/pipeline/:jobId',
    loadComponent: () => import('./features/recruiter/pipeline/pipeline.component').then(m => m.PipelineComponent),
    canActivate: [authGuard, roleGuard('RECRUITER')]
  },
  { path: '**', redirectTo: '/jobs' }
];
