# Job Portal Microservices

A production-ready job portal backend built with Spring Boot microservices architecture. The system supports job posting, job applications, resume management, real-time notifications, search, and analytics.

---

## Architecture Overview

```
                        ┌─────────────────┐
                        │   API Gateway   │  :8080
                        │  JWT Auth + CORS│
                        └────────┬────────┘
                                 │
          ┌──────────────────────┼──────────────────────┐
          │                      │                      │
   ┌──────▼──────┐      ┌────────▼───────┐    ┌────────▼────────┐
   │ user-service│      │  job-service   │    │application-svc  │
   │    :8081    │◄─────│    :8082       │    │    :8083        │
   └─────────────┘ Feign└───────┬────────┘    └───────┬─────────┘
                                │                     │
                         ┌──────▼─────────────────────▼──────┐
                         │           RabbitMQ                 │
                         │         (10 queues)                │
                         └──────┬──────────┬──────────┬───────┘
                                │          │          │
                    ┌───────────▼──┐ ┌─────▼────┐ ┌──▼──────────┐
                    │search-service│ │notif-svc │ │analytics-svc│
                    │    :8085     │ │  :8086   │ │    :8087    │
                    └──────────────┘ └──────────┘ └─────────────┘
```

---

## Services

| Service | Port | Description |
|---------|------|-------------|
| api-gateway | 8080 | JWT authentication, routing, CORS |
| user-service | 8081 | Registration, login, bookmarks, companies |
| job-service | 8082 | Job CRUD, company linking |
| application-service | 8083 | Job applications, interviews, chat messages |
| resume-service | 8084 | Resume upload and management |
| search-service | 8085 | Full-text job search |
| notification-service | 8086 | In-app notifications, email via Mailtrap |
| analytics-service | 8087 | Event tracking, job recommendations |
| config-server | 8888 | Centralized configuration |
| eureka-server | 8761 | Service discovery |

---

## Tech Stack

| Category | Technology |
|----------|-----------|
| Framework | Spring Boot 3.4.5 |
| Language | Java 17 |
| Service Discovery | Netflix Eureka |
| API Gateway | Spring Cloud Gateway |
| Messaging | RabbitMQ (via Docker) |
| Messaging Pattern | Transactional Outbox |
| Database | PostgreSQL 18 |
| DB Migration | Flyway |
| Auth | JWT (jjwt 0.11.5) |
| HTTP Client | OpenFeign |
| API Docs | SpringDoc OpenAPI (Swagger UI) |
| Distributed Tracing | Zipkin + Micrometer Brave |
| Email | Spring Mail + Mailtrap |
| File Storage | AWS S3 |
| Build | Maven (multi-module) |
| CI/CD | GitHub Actions |
| Containerization | Docker |

---

## Prerequisites

- Java 21
- Maven 3.8+
- PostgreSQL 18
- Docker (for RabbitMQ and Zipkin)
- Spring Tools Suite 5 (STS) or any IDE

---

## Infrastructure Setup

### Start RabbitMQ
```bash
docker run -d --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:management
```
Management UI: `http://localhost:15672` (guest/guest)

### Start Zipkin
```bash
docker run -d --name zipkin -p 9411:9411 openzipkin/zipkin
```
UI: `http://localhost:9411`

### Create PostgreSQL Databases
```sql
CREATE DATABASE jobportal_users;
CREATE DATABASE jobportal_jobs;
CREATE DATABASE jobportal_applications;
CREATE DATABASE jobportal_resumes;
CREATE DATABASE jobportal_search;
CREATE DATABASE jobportal_notifications;
CREATE DATABASE jobportal_analytics;
```

---

## Running the Project

### Step 1 — Build common-lib first
```bash
mvn clean install -pl common-lib -Dmaven.compiler.fork=true -Dmaven.compiler.executable="C:\Program Files\Java\jdk-21\bin\javac.exe"
```

### Step 2 — Build all services
```bash
mvn clean package -pl user-service,job-service,application-service,resume-service,search-service,notification-service,analytics-service,api-gateway,config-server,eureka-server -am -DskipTests -Dmaven.compiler.fork=true -Dmaven.compiler.executable="C:\Program Files\Java\jdk-21\bin\javac.exe"
```

### Step 3 — Start services in order
1. config-server (8888)
2. eureka-server (8761)
3. api-gateway (8080)
4. user-service, job-service, application-service, resume-service, search-service, notification-service, analytics-service

---

## API Documentation

### Unified Swagger UI (via Gateway)
```
http://localhost:8080/swagger-ui.html
```
Use the dropdown to switch between services.

### Individual Service Swagger UIs
| Service | URL |
|---------|-----|
| user-service | http://localhost:8081/swagger-ui/index.html |
| job-service | http://localhost:8082/swagger-ui/index.html |
| application-service | http://localhost:8083/swagger-ui/index.html |
| resume-service | http://localhost:8084/swagger-ui/index.html |
| search-service | http://localhost:8085/swagger-ui/index.html |
| notification-service | http://localhost:8086/swagger-ui/index.html |
| analytics-service | http://localhost:8087/swagger-ui/index.html |

---

## Key API Endpoints

### Auth
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | /api/users/register | Register (roles: RECRUITER, JOB_SEEKER, ADMIN) |
| POST | /api/users/login | Login — returns JWT in Authorization header |

### Jobs
| Method | Endpoint | Role | Description |
|--------|----------|------|-------------|
| POST | /api/jobs | RECRUITER | Post a job |
| GET | /api/jobs | ALL | List all jobs |
| GET | /api/jobs/{jobId} | ALL | Get job by ID |
| PUT | /api/jobs/{jobId}/close | RECRUITER | Close a job |

### Companies
| Method | Endpoint | Role | Description |
|--------|----------|------|-------------|
| POST | /api/companies | RECRUITER | Create company |
| PUT | /api/companies/{id} | RECRUITER | Update company |
| GET | /api/companies | ALL | List all companies |

### Applications
| Method | Endpoint | Role | Description |
|--------|----------|------|-------------|
| POST | /api/applications | JOB_SEEKER | Apply for a job |
| GET | /api/applications/job/{jobId} | RECRUITER | View applications for a job |
| GET | /api/applications/candidate/{id} | JOB_SEEKER | View my applications |
| PUT | /api/applications/{id}/status | RECRUITER | Update application status |

### Interviews
| Method | Endpoint | Role | Description |
|--------|----------|------|-------------|
| POST | /api/interviews | RECRUITER | Schedule interview |
| PUT | /api/interviews/{id}/status | RECRUITER | Update interview status |
| GET | /api/interviews/mine | ALL | Get my interviews |

### Notifications
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | /api/notifications | Get all my notifications |
| GET | /api/notifications/unread | Get unread notifications |
| PUT | /api/notifications/{id}/read | Mark as read |
| PUT | /api/notifications/read-all | Mark all as read |

### Search
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | /api/search/jobs?keyword=&location= | Search jobs (public) |

### Bookmarks
| Method | Endpoint | Role | Description |
|--------|----------|------|-------------|
| POST | /api/bookmarks/{jobId} | JOB_SEEKER | Bookmark a job |
| DELETE | /api/bookmarks/{jobId} | JOB_SEEKER | Remove bookmark |
| GET | /api/bookmarks | JOB_SEEKER | Get my bookmarks |

---

## Messaging Architecture

All inter-service events use the **Transactional Outbox Pattern**:
1. Producer saves event to `outbox_events` table in same DB transaction
2. `OutboxPoller` runs every 5 seconds and sends pending events to RabbitMQ
3. Consumer services receive and process events

### RabbitMQ Queues

| Queue | Producer | Consumer |
|-------|----------|----------|
| job.created.search.queue | job-service | search-service |
| job.created.notification.queue | job-service | notification-service |
| job.created.analytics.queue | job-service | analytics-service |
| job.closed.search.queue | job-service | search-service |
| job.closed.notification.queue | job-service | notification-service |
| job.applied.notification.queue | application-service | notification-service |
| job.applied.analytics.queue | application-service | analytics-service |
| interview.scheduled.notification.queue | application-service | notification-service |
| resume.uploaded.notification.queue | resume-service | notification-service |
| resume.uploaded.analytics.queue | resume-service | analytics-service |

---

## Security

- JWT-based authentication via API Gateway
- Gateway validates token and injects `X-User-Id` and `X-User-Role` headers
- Role-based access control enforced at gateway level
- Passwords hashed with BCrypt

### Roles
| Role | Permissions |
|------|-------------|
| JOB_SEEKER | Apply for jobs, upload resumes, bookmarks, view notifications |
| RECRUITER | Post jobs, manage companies, schedule interviews, view analytics |
| ADMIN | Full access to all endpoints |

---

## CI/CD

GitHub Actions pipeline (`.github/workflows/ci-cd.yml`):

- **On every push/PR** → builds and tests all services
- **On push to main** → builds Docker images and pushes to Docker Hub

### Required GitHub Secrets
| Secret | Description |
|--------|-------------|
| DOCKER_HUB_USERNAME | Docker Hub username |
| DOCKER_HUB_TOKEN | Docker Hub access token |

---

## Project Structure

```
job-portal-microservices/
├── common-lib/              # Shared DTOs, events, exceptions
├── config-server/           # Spring Cloud Config Server
├── eureka-server/           # Service discovery
├── api-gateway/             # JWT filter, routing
├── user-service/            # Users, auth, bookmarks, companies
├── job-service/             # Jobs, outbox
├── application-service/     # Applications, interviews, messages
├── resume-service/          # Resume upload, S3 storage
├── search-service/          # Job search with PostgreSQL trigram
├── notification-service/    # Email + in-app notifications
├── analytics-service/       # Event tracking, recommendations
├── .github/workflows/       # CI/CD pipeline
└── pom.xml                  # Parent POM
```

---

## Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| JWT_SECRET | (set in properties) | JWT signing secret |
| spring.datasource.password | root | PostgreSQL password |
| spring.rabbitmq.password | guest | RabbitMQ password |

---

## Distributed Tracing

All services are configured with Micrometer + Zipkin:
- Sampling rate: 100% (`probability=1.0`)
- Zipkin UI: `http://localhost:9411`
- Trace ID and Span ID included in all log lines

---

## Service Discovery

Eureka dashboard: `http://localhost:8761`

All services register automatically on startup.
