# Update: Recruiter Verification Notifications

## Overview

This update introduces end-to-end notifications for recruiters when their company registration moves through the admin verification pipeline. Recruiters now receive both **in-app** and **email** notifications at every stage — submission received, under review, verified, rejected, or when more information is requested.

---

## What Changed

### New Service — `recruiter-verification-service`

Previously this service had no messaging capability. It now participates fully in the platform's RabbitMQ event bus using the same **Transactional Outbox Pattern** used by all other services.

#### Added
- `RabbitMQConfig` — declares `job.portal.exchange` and binds `recruiter.verification.notification.queue` with routing key `recruiter.verification.status`
- `OutboxEvent` entity, `OutboxEventRepository`, and `OutboxPoller` — polls every 5 seconds, retries up to 5 times, dead-letters on persistent failure
- `@EnableScheduling` on the main application class
- `spring-boot-starter-amqp` dependency added to `pom.xml`
- RabbitMQ connection properties added to `application.properties`

#### Database Migrations
| Migration | Change |
|-----------|--------|
| `V3__create_outbox_events.sql` | Creates `outbox_events` table with `retry_count` and `dead_lettered` columns |
| `V4__add_recruiter_id_to_submissions.sql` | Adds `recruiter_id VARCHAR(255)` column to `recruiter_submissions` |

#### Model & DTO Changes
- `RecruiterSubmission` — added `recruiterId` field to link a submission to the user who created it
- `CreateRecruiterSubmissionRequest` — added optional `recruiterId` field

#### Service Changes — `RecruiterVerificationService`
Events are now published to the outbox at the following lifecycle points:

| Trigger | Status Published |
|---------|-----------------|
| Recruiter submits company registration | `PENDING` |
| Admin opens submission detail for the first time | `UNDER_REVIEW` |
| Admin approves | `VERIFIED` |
| Admin rejects | `REJECTED` |
| Admin requests more information | `MORE_INFO_REQUESTED` |

---

### `common-lib`

#### Added
- `RecruiterVerificationEvent` — new shared event class with fields:
  - `submissionId` — ID of the recruiter submission
  - `recruiterId` — user ID of the recruiter (used for in-app notification targeting)
  - `recruiterEmail` — work email (used for email delivery)
  - `companyName` — company name for message context
  - `status` — current verification status

---

### `notification-service`

#### Added
- `recruiter.verification.notification.queue` declared and bound in `RabbitMQConfig`
- `onRecruiterVerification()` consumer in `NotificationConsumer` — handles `RecruiterVerificationEvent` and:
  - Saves an **in-app notification** targeted at `recruiterId`
  - Sends an **email** to `recruiterEmail` via Mailtrap

#### Notification Messages by Status

| Status | In-App / Email Message |
|--------|----------------------|
| `PENDING` | Your company registration for '{company}' has been submitted and is awaiting review. |
| `UNDER_REVIEW` | Your company registration for '{company}' is currently under review by our team. |
| `VERIFIED` | Congratulations! Your company '{company}' has been verified. You can now post jobs. |
| `REJECTED` | Your company registration for '{company}' has been rejected. Please contact support. |
| `MORE_INFO_REQUESTED` | Additional information is required for your company registration '{company}'. |

---

### Frontend

#### `ApiService`
- `createRecruiterVerification()` now accepts an optional `recruiterId` field in its payload

#### `CompanyCreateComponent`
- Passes `auth.getUserId()` as `recruiterId` when submitting the verification form, so the backend can target in-app notifications to the correct user

---

## RabbitMQ Queue Added

| Queue | Producer | Consumer |
|-------|----------|----------|
| `recruiter.verification.notification.queue` | recruiter-verification-service | notification-service |

---

## How to Apply (Local Setup)

The two new Flyway migrations run automatically on service startup. No manual SQL is needed beyond what was already required.

If you are running locally and the `recruiter-verification-service` was already started before this update, restart it so the new migrations and RabbitMQ config are picked up:

```bash
# From project root
mvn clean package -pl common-lib -q
mvn clean package -pl recruiter-verification-service,notification-service --also-make -DskipTests -q
```

Then restart both services.

---

## Files Changed

```
common-lib/
  src/main/java/com/jobportal/common/events/
    + RecruiterVerificationEvent.java

recruiter-verification-service/
  pom.xml                                                  (+ amqp dependency)
  src/main/resources/application.properties                (+ rabbitmq config)
  src/main/resources/db/migration/
    + V3__create_outbox_events.sql
    + V4__add_recruiter_id_to_submissions.sql
  src/main/java/com/jobportal/verification/
    + config/RabbitMQConfig.java
    + outbox/OutboxEvent.java
    + outbox/OutboxEventRepository.java
    + outbox/OutboxPoller.java
    model/RecruiterSubmission.java                         (+ recruiterId field)
    dto/CreateRecruiterSubmissionRequest.java               (+ recruiterId field)
    service/RecruiterVerificationService.java               (+ outbox publishing)
    RecruiterVerificationServiceApplication.java            (+ @EnableScheduling)

notification-service/
  src/main/java/com/jobportal/notification/
    config/RabbitMQConfig.java                             (+ new queue + binding)
    consumer/NotificationConsumer.java                     (+ onRecruiterVerification)

frontend/
  src/app/core/services/api.service.ts                     (+ recruiterId in payload)
  src/app/features/recruiter/company-create/
    company-create.component.ts                            (+ pass recruiterId)
```
