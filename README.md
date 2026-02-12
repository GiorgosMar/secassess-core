# Security Assessment Core API

A specialized RESTful API designed for managing security assessments, featuring a template-based criteria copying system and robust business validation.

## Assignment Requirements Implementation

This project implements the following core requirements from the assignment:

### 1. RESTful API Architecture
- Versioned endpoints under `/api/v1/`.
- Full CRUD support for **Organizations**, **Projects**, **Templates**, and **Assessments**.
- Pagination, sorting, and filtering support for all list endpoints.

### 2. Copy Criteria Logic (`POST /api/v1/assessments/{id}/copy-from-template`)
Implements the core business logic as specified:
- **Filtering**: Supports `includeSections` to copy only specific parts of a template.
- **Validation**: Only templates with `PUBLISHED` status can be used as sources.
- **Mapping**: Automates `TemplateCriterion` → `AssessmentItem` mapping (copying section, text, severity, and weight).
- **Duplicate Prevention**: If `overwriteExisting` is set to `false`, the system skips items already present (based on `criterionRef`).
- **Response**: Returns a detailed summary: `{ "copied": X, "skippedDuplicates": Y, "filteredOut": Z, "totalSource": W }`.

### 3. Status Transitions (`PATCH /api/v1/assessments/{id}/status`)
- Enforces strict validation rules: An assessment cannot be set to `COMPLETED` if any `AssessmentItem` has a `null` score.

### 4. Security & Access Control
- **JWT Bearer Authentication**.
- **Role-Based Access Control (RBAC)**:
    - `ADMIN`: Organization management and Template publishing.
    - `ASSESSOR`: Assessment management and Criteria copying.
    - `VIEWER`: Read-only access.

## Infrastructure & Deployment

### Tech Stack
- **Backend**: Java 17, Spring Boot 3.x, Hibernate.
- **Database**: PostgreSQL 15.
- **Performance**: Redis Caching.
- **DevOps**: Docker & Docker Compose.

### How to Run
1. Ensure Docker Desktop is running.
2. Execute the following command in the root directory:
   ```bash
   docker compose up --build
