# HireHub — Job Portal

A full-stack job portal built with a **Spring Boot microservices** backend and an **Angular 21** frontend. Supports job posting, applications, resume management, interview scheduling, real-time notifications, search, and analytics.

---

## Architecture

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
| application-service | 8083 | Applications, interviews, chat messages |
| resume-service | 8084 | Resume upload — file stored in DB or S3 |
| search-service | 8085 | Full-text job search with PostgreSQL trigram |
| notification-service | 8086 | In-app notifications + email via Mailtrap |
| analytics-service | 8087 | Event tracking, job recommendations |
| config-server | 8888 | Centralized Spring Cloud Config |
| eureka-server | 8761 | Netflix Eureka service discovery |

---

## Tech Stack

### Backend
| Category | Technology |
|----------|-----------|
| Framework | Spring Boot 3.4.5 |
| Language | Java 21 |
| Service Discovery | Netflix Eureka |
| API Gateway | Spring Cloud Gateway |
| Messaging | RabbitMQ |
| Messaging Pattern | Transactional Outbox |
| Database | PostgreSQL |
| DB Migration | Flyway |
| Auth | JWT (jjwt 0.11.5) |
| HTTP Client | OpenFeign |
| API Docs | SpringDoc OpenAPI (Swagger UI) |
| Distributed Tracing | Zipkin + Micrometer Brave |
| Email | Spring Mail + Mailtrap |
| File Storage | AWS S3 / DB fallback |
| Build | Maven (multi-module) |
| CI/CD | GitHub Actions |
| Containerization | Docker |

### Frontend
| Category | Technology |
|----------|-----------|
| Framework | Angular 21 (standalone components) |
| Language | TypeScript |
| Styling | Tailwind CSS v4 |
| Icons | Lucide Angular |
| HTTP | Angular HttpClient with interceptors |
| State | Angular Signals |
| Change Detection | OnPush throughout |

---

## Prerequisites

- Java 21
- Maven 3.8+
- Node.js 20+ and npm
- PostgreSQL
- Docker (for RabbitMQ and Zipkin)

---

## Infrastructure Setup

### Start RabbitMQ
```bash
docker run -d --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:management
```
Management UI: `http://localhost:15672` — credentials: `guest / guest`

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

## Running the Backend

### Step 1 — Build common-lib
```bash
mvn clean install -pl common-lib -q
```

### Step 2 — Build all services
```bash
mvn clean package \
  -pl user-service,job-service,application-service,resume-service,search-service,notification-service,analytics-service,api-gateway,config-server,eureka-server \
  --also-make -DskipTests -q
```

### Step 3 — Start services in order
1. `config-server` (8888)
2. `eureka-server` (8761)
3. `api-gateway` (8080)
4. All remaining services in any order

---

## Running the Frontend

```bash
cd frontend
npm install
npm start
```

App runs at `http://localhost:4200`

The Angular dev server proxies all `/api/**` requests to the gateway at `http://localhost:8080`.

---

## API Documentation

### Unified Swagger UI (via Gateway)
```
http://localhost:8080/swagger-ui.html
```

### Per-Service Swagger UIs
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
| POST | /api/users/register | Register — roles: `RECRUITER`, `JOB_SEEKER`, `ADMIN` |
| POST | /api/users/login | Login — returns JWT in `Authorization` header |

### Jobs
| Method | Endpoint | Role | Description |
|--------|----------|------|-------------|
| POST | /api/jobs | RECRUITER | Post a job |
| GET | /api/jobs | ALL | List all jobs |
| GET | /api/jobs/{jobId} | ALL | Get job by ID |
| PUT | /api/jobs/{jobId}/close | RECRUITER | Close a job |
| PUT | /api/jobs/{jobId}/reopen | RECRUITER | Reopen a job |

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
| POST | /api/applications/{id}/offer-response | JOB_SEEKER | Accept or reject offer |

### Interviews
| Method | Endpoint | Role | Description |
|--------|----------|------|-------------|
| POST | /api/interviews | RECRUITER | Schedule interview |
| PUT | /api/interviews/{id}/status | RECRUITER | Update interview status |
| GET | /api/interviews/mine | ALL | Get my interviews |
| GET | /api/interviews/application/{id} | ALL | Get interviews for an application |

### Resumes
| Method | Endpoint | Role | Description |
|--------|----------|------|-------------|
| POST | /api/resumes/upload | JOB_SEEKER | Upload resume file |
| GET | /api/resumes/user/{userId} | JOB_SEEKER | Get my resumes |
| GET | /api/resumes/download/{resumeId} | ALL | Download resume |

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
| GET | /api/search/categories | Get job category counts |

### Bookmarks
| Method | Endpoint | Role | Description |
|--------|----------|------|-------------|
| POST | /api/bookmarks/{jobId} | JOB_SEEKER | Bookmark a job |
| DELETE | /api/bookmarks/{jobId} | JOB_SEEKER | Remove bookmark |
| GET | /api/bookmarks | JOB_SEEKER | Get my bookmarks |

---

## Messaging Architecture

All inter-service events use the **Transactional Outbox Pattern**:
1. Producer saves event to `outbox_events` table in the same DB transaction as the business operation
2. `OutboxPoller` runs every 5 seconds and publishes pending events to RabbitMQ
3. Consumer services receive and process events independently

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

- JWT-based authentication enforced at the API Gateway
- Gateway validates the token and injects `X-User-Id` and `X-User-Role` headers into downstream requests
- Role-based access control enforced at both gateway and service level
- Passwords hashed with BCrypt

### Roles
| Role | Permissions |
|------|-------------|
| JOB_SEEKER | Apply for jobs, upload resumes, bookmarks, view notifications, respond to offers |
| RECRUITER | Post jobs, manage companies, schedule interviews, view applications, view analytics |
| ADMIN | Full access to all endpoints |

---

## Project Structure

```
job-portal-microservices/
├── common-lib/              # Shared DTOs, events, exceptions
├── config-server/           # Spring Cloud Config Server
├── eureka-server/           # Service discovery
├── api-gateway/             # JWT filter, routing, CORS
├── user-service/            # Users, auth, bookmarks, companies
├── job-service/             # Jobs, outbox
├── application-service/     # Applications, interviews, messages
├── resume-service/          # Resume upload, DB/S3 storage
├── search-service/          # Full-text search with PostgreSQL trigram
├── notification-service/    # Email + in-app notifications
├── analytics-service/       # Event tracking, recommendations
├── frontend/                # Angular 21 SPA
├── docker-compose.yml       # Infrastructure (RabbitMQ, Zipkin, PostgreSQL)
├── .github/workflows/       # CI/CD pipeline
└── pom.xml                  # Parent POM
```

---

## Environment Variables

| Variable | Description |
|----------|-------------|
| `JWT_SECRET` | JWT signing secret (min 32 chars) |
| `spring.datasource.password` | PostgreSQL password |
| `spring.rabbitmq.password` | RabbitMQ password |
| `spring.mail.username` | Mailtrap SMTP username |
| `spring.mail.password` | Mailtrap SMTP password |
| `aws.s3.bucket` | S3 bucket name for resume storage |
| `aws.access-key` | AWS access key |
| `aws.secret-key` | AWS secret key |

---

## CI/CD

GitHub Actions pipeline (`.github/workflows/ci-cd.yml`):

- **On every push / PR** → builds and tests all services
- **When SonarQube secrets are configured** → runs SonarQube analysis with JaCoCo coverage
- **On push to `main`** → builds Docker images and pushes to Docker Hub

### Required GitHub Secrets
| Secret | Description |
|--------|-------------|
| `DOCKER_USERNAME` | Docker Hub username |
| `DOCKER_PASSWORD` | Docker Hub access token |
| `SONAR_HOST_URL` | SonarQube server URL, for example `https://sonarqube.example.com` |
| `SONAR_TOKEN` | SonarQube project analysis token |

---

## SonarQube

Start the local SonarQube stack:

```bash
docker compose up -d sonarqube
```

Open `http://localhost:9000`, sign in with `admin` / `admin`, create a project token, then run:

```bash
mvn clean verify org.sonarsource.scanner.maven:sonar-maven-plugin:5.5.0.6356:sonar -Dsonar.token=<token>
```

The Maven parent POM pins the Sonar scanner version and publishes JaCoCo XML reports from all backend modules. `sonar-project.properties` is also present for scanner-based tooling that expects a project file.

---

## Distributed Tracing

All services are instrumented with Micrometer + Zipkin:
- Sampling rate: 100%
- Zipkin UI: `http://localhost:9411`
- Trace ID and Span ID are included in all log lines

---

## Service Discovery

Eureka dashboard: `http://localhost:8761`

All services register automatically on startup.
