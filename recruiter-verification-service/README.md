# Recruiter Verification Service

Internal admin APIs for manual recruiter verification.

## Endpoints

- `GET /api/admin/recruiter-verifications/queue?status=PENDING&page=0&size=10`
- `GET /api/admin/recruiter-verifications/{id}`
- `POST /api/admin/recruiter-verifications/{id}/review`
- `GET /api/admin/recruiter-verifications/history?search=acme&page=0&size=10`

All `/api/admin/**` endpoints require a JWT with role `ADMIN`. The Angular `/admin/*` routes also use `roleGuard('ADMIN')`.

## Future Automation Hook

Automated checks should plug into `RecruiterVerificationService.buildRiskChecks(...)` or a future injected check provider that returns the same `RiskCheckDto` list shape:

```json
{ "name": "domain_match", "passed": true, "summary": "...", "expected": "...", "actual": "..." }
```

Automated approval should call the existing `reviewRecruiter(id, decision, reason, reviewerId)` service method. The audit-log write lives in that method, so manual and automated decisions keep the same workflow and audit trail.
