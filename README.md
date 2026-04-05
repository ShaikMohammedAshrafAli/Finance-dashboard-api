# 💰 Finance Dashboard Backend

A production-ready REST API backend for a Finance Data Processing and Access Control system built with **Java 17 + Spring Boot 3**.

---

## 📋 Table of Contents
- [Tech Stack](#tech-stack)
- [Architecture Overview](#architecture-overview)
- [Project Structure](#project-structure)
- [Role & Access Control](#role--access-control)
- [Data Model](#data-model)
- [API Endpoints](#api-endpoints)
- [Quick Start](#quick-start)
- [Running with PostgreSQL](#running-with-postgresql)
- [Default Credentials](#default-credentials)
- [Sample API Requests](#sample-api-requests)
- [Assumptions & Design Decisions](#assumptions--design-decisions)

---

## 🛠 Tech Stack

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

## 🏗 Architecture Overview

```
┌─────────────────────────────────────────────────────────────────┐
│                        CLIENT (HTTP)                            │
└────────────────────────────┬────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│                   SECURITY LAYER                                │
│  JwtAuthenticationFilter → SecurityConfig (role-based rules)   │
└────────────────────────────┬────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│                   CONTROLLER LAYER                              │
│  AuthController │ UserController │ RecordController │ Dashboard │
└────────────────────────────┬────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│                   SERVICE LAYER                                 │
│  AuthService │ UserService │ FinancialRecordService │ Dashboard │
└────────────────────────────┬────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│                   REPOSITORY LAYER                              │
│        UserRepository │ FinancialRecordRepository               │
└────────────────────────────┬────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│                   DATABASE                                      │
│              H2 (dev)  /  PostgreSQL (prod)                     │
└─────────────────────────────────────────────────────────────────┘
```

---

## 📁 Project Structure

```
finance-dashboard/
├── src/
│   ├── main/
│   │   ├── java/com/finance/dashboard/
│   │   │   ├── FinanceDashboardApplication.java   ← Entry point
│   │   │   ├── config/
│   │   │   │   ├── SecurityConfig.java            ← JWT + role-based security rules
│   │   │   │   ├── OpenApiConfig.java             ← Swagger configuration
│   │   │   │   └── DataSeeder.java                ← Seeds default users + sample data
│   │   │   ├── controller/
│   │   │   │   ├── AuthController.java            ← POST /api/auth/login
│   │   │   │   ├── UserController.java            ← /api/users/**
│   │   │   │   ├── FinancialRecordController.java ← /api/records/**
│   │   │   │   └── DashboardController.java       ← /api/dashboard/**
│   │   │   ├── service/
│   │   │   │   ├── AuthService.java
│   │   │   │   ├── UserService.java
│   │   │   │   ├── FinancialRecordService.java
│   │   │   │   ├── DashboardService.java
│   │   │   │   └── impl/                          ← All implementations
│   │   │   ├── entity/
│   │   │   │   ├── User.java
│   │   │   │   ├── Role.java                      ← VIEWER | ANALYST | ADMIN
│   │   │   │   ├── FinancialRecord.java
│   │   │   │   └── TransactionType.java           ← INCOME | EXPENSE
│   │   │   ├── repository/
│   │   │   │   ├── UserRepository.java
│   │   │   │   └── FinancialRecordRepository.java ← Custom JPQL queries
│   │   │   ├── dto/
│   │   │   │   ├── request/                       ← Input DTOs with validation
│   │   │   │   └── response/                      ← Output DTOs
│   │   │   ├── security/
│   │   │   │   ├── JwtUtils.java                  ← Token generation & validation
│   │   │   │   ├── JwtAuthenticationFilter.java   ← Per-request JWT check
│   │   │   │   └── UserDetailsServiceImpl.java
│   │   │   └── exception/
│   │   │       ├── GlobalExceptionHandler.java    ← Centralised error handling
│   │   │       ├── ResourceNotFoundException.java
│   │   │       ├── DuplicateResourceException.java
│   │   │       └── AccessDeniedException.java
│   │   └── resources/
│   │       ├── application.properties             ← H2 dev config
│   │       └── application-postgres.properties    ← PostgreSQL config
│   └── test/
│       └── java/com/finance/dashboard/
│           ├── FinanceDashboardApplicationTests.java
│           ├── service/
│           │   ├── UserServiceTest.java
│           │   └── FinancialRecordServiceTest.java
│           └── controller/
│               └── AuthControllerTest.java
├── pom.xml
└── README.md
```

---

## 🔐 Role & Access Control

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

## 🗄 Data Model

### Entity Relationship Diagram

```
┌──────────────────────────┐          ┌────────────────────────────────┐
│         users            │          │       financial_records         │
├──────────────────────────┤          ├────────────────────────────────┤
│ id          BIGINT (PK)  │◄────┐    │ id           BIGINT (PK)       │
│ name        VARCHAR(100) │     │    │ amount       DECIMAL(15,2)     │
│ email       VARCHAR(150) │     └────│ created_by   BIGINT (FK)       │
│ password    VARCHAR      │          │ type         VARCHAR (ENUM)    │
│ role        VARCHAR(20)  │          │ category     VARCHAR(100)      │
│ active      BOOLEAN      │          │ date         DATE              │
│ created_at  TIMESTAMP    │          │ notes        VARCHAR(500)      │
│ updated_at  TIMESTAMP    │          │ deleted      BOOLEAN           │
└──────────────────────────┘          │ created_at   TIMESTAMP         │
                                      │ updated_at   TIMESTAMP         │
                                      └────────────────────────────────┘

Role ENUM:            VIEWER | ANALYST | ADMIN
TransactionType ENUM: INCOME | EXPENSE
```

### Key Design Decisions
- **Soft Delete**: Records are never physically deleted. `deleted = true` hides them from all queries, preserving the audit trail.
- **User Deactivation**: Deleting a user sets `active = false` — they cannot login but their records remain intact.
- **BigDecimal for Money**: All monetary values use `BigDecimal` with `DECIMAL(15,2)` precision — never `float` or `double`.

---

## 🌐 API Endpoints

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

---

## ⚡ Quick Start

### Prerequisites
- Java 17+
- Maven 3.8+

### 1. Clone & Run

```bash
git clone https://github.com/YOUR_USERNAME/finance-dashboard.git
cd finance-dashboard
mvn spring-boot:run
```

The app starts on **http://localhost:8080**

### 2. Open Swagger UI

```
http://localhost:8080/swagger-ui.html
```

### 3. Open H2 Console (dev database browser)

```
http://localhost:8080/h2-console
JDBC URL: jdbc:h2:mem:financedb
Username: sa
Password: (leave blank)
```

### 4. Run Tests

```bash
mvn test
```

### 5. Build JAR

```bash
mvn clean package -DskipTests
java -jar target/finance-dashboard-1.0.0.jar
```

---

## 🐘 Running with PostgreSQL

1. Create a PostgreSQL database:
```sql
CREATE DATABASE financedb;
```

2. Update `src/main/resources/application-postgres.properties`:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/financedb
spring.datasource.username=your_username
spring.datasource.password=your_password
```

3. Run with the postgres profile:
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=postgres
```
Or with the JAR:
```bash
java -jar target/finance-dashboard-1.0.0.jar --spring.profiles.active=postgres
```

---

## 🔑 Default Credentials

These users are automatically seeded on startup:

| Role    | Email                  | Password    |
|---------|------------------------|-------------|
| ADMIN   | admin@finance.com      | admin123    |
| ANALYST | analyst@finance.com    | analyst123  |
| VIEWER  | viewer@finance.com     | viewer123   |

---

## 📬 Sample API Requests

### Step 1 — Login

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "admin@finance.com", "password": "admin123"}'
```

**Response:**
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "type": "Bearer",
    "userId": 1,
    "name": "Super Admin",
    "email": "admin@finance.com",
    "role": "ADMIN"
  }
}
```

> Copy the `token` value and use it in all subsequent requests.

---

### Step 2 — Use the token

```bash
export TOKEN="eyJhbGciOiJIUzI1NiJ9..."
```

---

### Create a Financial Record (ADMIN)

```bash
curl -X POST http://localhost:8080/api/records \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 5000.00,
    "type": "INCOME",
    "category": "Freelance",
    "date": "2024-03-15",
    "notes": "Web design project"
  }'
```

---

### Get All Records with Filters

```bash
# All income records
curl "http://localhost:8080/api/records?type=INCOME" \
  -H "Authorization: Bearer $TOKEN"

# Filter by date range and category
curl "http://localhost:8080/api/records?startDate=2024-01-01&endDate=2024-03-31&category=Salary" \
  -H "Authorization: Bearer $TOKEN"

# With pagination
curl "http://localhost:8080/api/records?page=0&size=5" \
  -H "Authorization: Bearer $TOKEN"
```

---

### Get Dashboard Summary (ADMIN / ANALYST)

```bash
curl http://localhost:8080/api/dashboard/summary \
  -H "Authorization: Bearer $TOKEN"
```

**Response:**
```json
{
  "success": true,
  "data": {
    "totalIncome": 118000.00,
    "totalExpenses": 5700.00,
    "netBalance": 112300.00,
    "totalRecords": 12,
    "categoryBreakdown": [
      { "category": "Salary", "type": "INCOME", "total": 98000.00 },
      { "category": "Rent",   "type": "EXPENSE", "total": 2400.00 }
    ],
    "monthlyTrends": [
      { "year": 2024, "month": 2, "monthName": "Feb 2024", "income": 48000.00, "expenses": 1600.00, "net": 46400.00 },
      { "year": 2024, "month": 3, "monthName": "Mar 2024", "income": 70000.00, "expenses": 4100.00, "net": 65900.00 }
    ],
    "recentActivity": [ ... ]
  }
}
```

---

### Get Dashboard Summary by Date Range

```bash
curl "http://localhost:8080/api/dashboard/summary/range?startDate=2024-01-01&endDate=2024-03-31" \
  -H "Authorization: Bearer $TOKEN"
```

---

### Create a New User (ADMIN only)

```bash
curl -X POST http://localhost:8080/api/users \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Alice Smith",
    "email": "alice@finance.com",
    "password": "securepass123",
    "role": "ANALYST"
  }'
```

---

### Viewer trying to create a record (will be DENIED)

```bash
# Login as viewer first
export VIEWER_TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"viewer@finance.com","password":"viewer123"}' \
  | python3 -c "import sys,json; print(json.load(sys.stdin)['data']['token'])")

curl -X POST http://localhost:8080/api/records \
  -H "Authorization: Bearer $VIEWER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"amount":100,"type":"INCOME","category":"Test","date":"2024-03-01"}'
# Returns 403 Forbidden
```

---

## 🏛 Assumptions & Design Decisions

| # | Decision | Reason |
|---|----------|--------|
| 1 | **Soft delete** for both users and records | Preserves audit trail; records are hidden via `deleted=false` filter |
| 2 | **H2 in-memory** as default database | Zero-setup for development and evaluation; switch to PostgreSQL in production |
| 3 | **JWT stateless auth** (24h expiry) | No session storage needed; scales horizontally |
| 4 | **Only ADMIN can write records** | Clear separation; analyst/viewer are read-only consumers |
| 5 | **BigDecimal for all monetary values** | Avoids floating-point precision errors in financial calculations |
| 6 | **DataSeeder** pre-populates 3 users + 12 records | Evaluators can test all role behaviours immediately |
| 7 | **Category is a free-text string** | Flexible; no fixed category table means the frontend can evolve categories freely |
| 8 | **Paginated record listing** | Prevents large payloads; default page size = 10 |
| 9 | **Centralised GlobalExceptionHandler** | Consistent JSON error shape across all endpoints |
| 10 | **Monthly trends cover last 12 months** by default | Practical default for dashboard charts |

---

## ✅ Features Implemented

- [x] JWT Authentication
- [x] Role-based access control (VIEWER / ANALYST / ADMIN)
- [x] User CRUD with role assignment
- [x] User deactivation (soft delete)
- [x] Financial records CRUD
- [x] Soft delete for records
- [x] Filtering by type, category, date range
- [x] Pagination for record listing
- [x] Dashboard: total income / expenses / net balance
- [x] Dashboard: category-wise breakdown
- [x] Dashboard: monthly trends (last 12 months)
- [x] Dashboard: recent activity
- [x] Dashboard by custom date range
- [x] Input validation with meaningful errors
- [x] Centralised exception handling
- [x] Swagger UI / OpenAPI documentation
- [x] H2 console for dev inspection
- [x] PostgreSQL profile for production
- [x] Automatic data seeding
- [x] Unit tests (service layer)
- [x] Integration tests (controller layer)

---

## 📄 License

This project is created for evaluation purposes.
