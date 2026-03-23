CREATE DATABASE jobportal_users;
CREATE DATABASE jobportal_jobs;
CREATE DATABASE jobportal_applications;
CREATE DATABASE jobportal_resumes;
CREATE DATABASE jobportal_search;
CREATE DATABASE jobportal_notifications;
CREATE DATABASE jobportal_analytics;

CREATE USER user_svc_user WITH PASSWORD 'user_pass';
CREATE USER job_svc_user WITH PASSWORD 'job_pass';
CREATE USER app_svc_user WITH PASSWORD 'app_pass';
CREATE USER resume_svc_user WITH PASSWORD 'resume_pass';
CREATE USER search_svc_user WITH PASSWORD 'search_pass';
CREATE USER notif_svc_user WITH PASSWORD 'notif_pass';
CREATE USER analytics_svc_user WITH PASSWORD 'analytics_pass';

GRANT ALL PRIVILEGES ON DATABASE jobportal_users TO user_svc_user;
GRANT ALL PRIVILEGES ON DATABASE jobportal_jobs TO job_svc_user;
GRANT ALL PRIVILEGES ON DATABASE jobportal_applications TO app_svc_user;
GRANT ALL PRIVILEGES ON DATABASE jobportal_resumes TO resume_svc_user;
GRANT ALL PRIVILEGES ON DATABASE jobportal_search TO search_svc_user;
GRANT ALL PRIVILEGES ON DATABASE jobportal_notifications TO notif_svc_user;
GRANT ALL PRIVILEGES ON DATABASE jobportal_analytics TO analytics_svc_user;
