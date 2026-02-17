# Security Assessment Core API

A specialized, production-ready RESTful API designed for managing security assessments. Featuring a template-based criteria copying system, distributed tracing, and robust business validation.

## Key Assignment Features

### 1. RESTful API Architecture
- **Versioned Endpoints**: All resources are under `/api/v1/`.
- **Full CRUD**: Support for Organizations, Projects, Templates, and Assessments.
- **Data Handling**: Advanced pagination, sorting, and filtering for all collection endpoints.

### 2. Copy Criteria Logic (`POST /api/v1/assessments/{id}/copy-from-template`)
- **Smart Filtering**: Supports `includeSections` to selectively copy parts of a template.
- **Strict Validation**: Only `PUBLISHED` templates are accepted as sources.
- **Intelligent Mapping**: Automated `TemplateCriterion` → `AssessmentItem` transformation (text, severity, weight).
- **Idempotency**: `overwriteExisting` flag prevents or allows overwriting based on `criterionRef`.
- **Summary Report**: Returns detailed stats: `{ "copied": X, "skippedDuplicates": Y, "filteredOut": Z, "totalSource": W }`.

### 3. Business Guardrails
- **Status Integrity**: Prevents setting an assessment to `COMPLETED` if any items remain unscored (`null` score).
- **JPA Auditing**: Automatic tracking of `createdAt` and `updatedAt` for all entities.

## Tech Stack & Infrastructure

- **Backend**: Java 17, Spring Boot 3.x, Hibernate.
- **Database**: PostgreSQL 15.
- **Schema Management**: Flyway Migrations.
- **Caching**: Redis for high-performance data retrieval.
- **Security**: JWT Bearer Authentication & RBAC (ADMIN, ASSESSOR, VIEWER).
- **Observability**: SLF4J + MDC for Distributed Tracing (Correlation IDs).

## Observability & Troubleshooting

The system is built with a "debug-first" mindset:
- **Distributed Tracing**: Every request is assigned a unique `X-Correlation-ID`.
- **Contextual Logging**: All logs (Controller to Service) automatically include the Trace ID.
- **Standardized Errors**: Implements **RFC 7807 (Problem Details)**. Error responses include the `traceId`, allowing instant correlation between client reports and server logs.

## Deployment & Configuration

### 1. Environment Configuration
Create a `.env` file in the root directory:

```env
# Database Configuration
POSTGRES_DB=secassess
POSTGRES_USER=postgres
POSTGRES_PASSWORD=password

# Security Configuration
JWT_SECRET=secret-key
JWT_EXPIRATION=86400000

# Redis Configuration
REDIS_HOST=cache
REDIS_PORT=6379
