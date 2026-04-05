# Finance Dashboard Backend

A production-ready REST API backend for a Finance Data Processing and Access Control system built with **Java 17 + Spring Boot 3**.

---

## Table of Contents
- [Tech Stack](#tech-stack)
- [Role & Access Control](#role--access-control)
- [API Endpoints](#api-endpoints)
- [Default Credentials](#default-credentials)
---

## Tech Stack

| Layer        | Technology                         |
|-------------|-------------------------------------|
| Language     | Java 17                            |
| Framework    | Spring Boot 3.2.3                  |
| Security     | Spring Security + JWT (jjwt 0.11)  |
| Database     | H2 (dev, in-memory) / PostgreSQL   |
| ORM          | Spring Data JPA / Hibernate        |
| Validation   | Jakarta Bean Validation            |
| API Docs     | SpringDoc OpenAPI (Swagger UI)     |
| Build Tool   | Maven                              |
| Testing      | JUnit 5 + Mockito + MockMvc        |

---

## Role & Access Control

### Roles

| Role        | Description                                    |
|-------------|------------------------------------------------|
| `VIEWER`    | Read-only access to financial records          |
| `ANALYST`   | Read records + access dashboard analytics      |
| `ADMIN`     | Full access — create/update/delete + manage users |

### Permission Matrix

| Endpoint                      | VIEWER | ANALYST | ADMIN |
|-------------------------------|--------|---------|-------|
| `POST /api/auth/login`        | ✅     | ✅      | ✅    |
| `GET /api/users/me`           | ✅     | ✅      | ✅    |
| `GET /api/users`              | ❌     | ❌      | ✅    |
| `POST /api/users`             | ❌     | ❌      | ✅    |
| `PUT /api/users/{id}`         | ❌     | ❌      | ✅    |
| `DELETE /api/users/{id}`      | ❌     | ❌      | ✅    |
| `GET /api/records`            | ✅     | ✅      | ✅    |
| `GET /api/records/{id}`       | ✅     | ✅      | ✅    |
| `POST /api/records`           | ❌     | ❌      | ✅    |
| `PUT /api/records/{id}`       | ❌     | ❌      | ✅    |
| `DELETE /api/records/{id}`    | ❌     | ❌      | ✅    |
| `GET /api/dashboard/summary`  | ❌     | ✅      | ✅    |
| `GET /api/dashboard/summary/range` | ❌ | ✅    | ✅    |

---

## API Endpoints

### Authentication

| Method | Endpoint          | Auth | Description        |
|--------|-------------------|------|--------------------|
| POST   | `/api/auth/login` | None | Login, get JWT     |

### Users

| Method | Endpoint            | Roles | Description              |
|--------|---------------------|-------|--------------------------|
| GET    | `/api/users/me`     | All   | Get my profile           |
| POST   | `/api/users`        | ADMIN | Create new user          |
| GET    | `/api/users`        | ADMIN | List all users           |
| GET    | `/api/users/{id}`   | ADMIN | Get user by ID           |
| PUT    | `/api/users/{id}`   | ADMIN | Update user              |
| DELETE | `/api/users/{id}`   | ADMIN | Deactivate user          |

### Financial Records

| Method | Endpoint              | Roles                    | Description                   |
|--------|-----------------------|--------------------------|-------------------------------|
| POST   | `/api/records`        | ADMIN                    | Create record                 |
| GET    | `/api/records`        | ADMIN, ANALYST, VIEWER   | List records (with filters)   |
| GET    | `/api/records/{id}`   | ADMIN, ANALYST, VIEWER   | Get record by ID              |
| PUT    | `/api/records/{id}`   | ADMIN                    | Update record                 |
| DELETE | `/api/records/{id}`   | ADMIN                    | Soft-delete record            |

**Query Parameters for `GET /api/records`:**

| Parameter   | Type   | Description                              |
|-------------|--------|------------------------------------------|
| `type`      | enum   | `INCOME` or `EXPENSE`                    |
| `category`  | string | Partial match on category name           |
| `startDate` | date   | Filter from date (`yyyy-MM-dd`)          |
| `endDate`   | date   | Filter to date (`yyyy-MM-dd`)            |
| `page`      | int    | Page number (default: 0)                 |
| `size`      | int    | Page size (default: 10)                  |

### Dashboard

| Method | Endpoint                       | Roles           | Description                          |
|--------|--------------------------------|-----------------|--------------------------------------|
| GET    | `/api/dashboard/summary`       | ADMIN, ANALYST  | Full summary (income, expenses, etc) |
| GET    | `/api/dashboard/summary/range` | ADMIN, ANALYST  | Summary filtered by date range       |

**Dashboard Summary Response includes:**
- `totalIncome`, `totalExpenses`, `netBalance`, `totalRecords`
- `categoryBreakdown` — totals per category + type
- `monthlyTrends` — last 12 months income vs expenses
- `recentActivity` — 10 most recent records


## Default Credentials

These users are automatically seeded on startup:

| Role    | Email                  | Password    |
|---------|------------------------|-------------|
| ADMIN   | admin@finance.com      | admin123    |
| ANALYST | analyst@finance.com    | analyst123  |
| VIEWER  | viewer@finance.com     | viewer123   |


