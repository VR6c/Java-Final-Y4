# Spring Boot RESTful API & Management System

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.1-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Gradle](https://img.shields.io/badge/Gradle-8.x-02303A.svg)](https://gradle.org/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Database-blue.svg)](https://www.postgresql.org/)
[![Swagger](https://img.shields.io/badge/OpenAPI%203.1-Swagger%20UI-85EA2D.svg)](http://localhost:8080/swagger-ui.html)
[![JWT](https://img.shields.io/badge/Security-JWT%20Auth-black.svg)](https://jwt.io/)

A modern, robust, and production-ready Spring Boot 3 enterprise REST API built with Java 21. It incorporates JWT authentication, OTP email delivery via Gmail SMTP, Caffeine caching, asynchronous audit logging, and multi-domain management for educational institutions and e-commerce.

---

## 🚀 Key Features

- **🔐 Security & Authentication**:
  - JWT (JSON Web Tokens) with HMAC-SHA256 signature.
  - Access Token and Refresh Token flow.
  - Role-based authorization (`ROLE_ADMIN`, `ROLE_USER`, etc.).
  - Password hashing using BCrypt.

- **📧 Email & OTP Verification**:
  - Gmail SMTP integration via Spring Mail.
  - Secure One-Time Password (OTP) generation and email delivery.
  - Automatic expiration and verification lifecycle.

- **⚡ High-Performance Caching**:
  - Caffeine Cache integration with Spring Cache.
  - Dedicated Cache Management endpoints to monitor and evict cache entries dynamically.

- **📊 Activity Audit & Logging**:
  - Custom HTTP request/response filter capturing client IP, endpoints, status codes, and latency.
  - Asynchronous database logging to the `activity_logs` table.

- **🏫 School Management Module**:
  - Manage **Students**, **Teachers**, **Cards**, **Majors**, and **Subjects**.
  - Complex entity associations, cascade operations, and DTO mappings via ModelMapper.

- **🛒 E-Commerce & Product Module**:
  - Manage **Customers**, **Products**, **Orders**, **Product-Orders**, and **Customer Emails**.

- **📖 Interactive API Documentation**:
  - Auto-generated Swagger UI / OpenAPI 3.1 schema.

---

## 🛠️ Tech Stack & Dependencies

| Category | Technology |
|---|---|
| **Language & SDK** | Java 21 |
| **Framework** | Spring Boot 3.4.1 |
| **Build Tool** | Gradle (Kotlin DSL - `build.gradle.kts`) |
| **Database & ORM** | PostgreSQL, Spring Data JPA / Hibernate |
| **Security** | Spring Security 6, JJWT (`0.12.6`) |
| **Cache Engine** | Spring Cache, Caffeine Cache |
| **Mail Service** | Spring Boot Starter Mail (Gmail SMTP) |
| **Documentation** | SpringDoc OpenAPI Starter WebMVC UI (`2.8.5`) |
| **Mapping & Utils** | ModelMapper (`3.2.0`), Project Lombok |

---

## 📋 Prerequisites

Ensure you have the following installed on your machine:
- **Java 21+** (`java -version`)
- **PostgreSQL** running locally on port `5432`
- **Git**

---

## ⚙️ Configuration & Environment

Configuration is located in [`src/main/resources/application.properties`](file:///Users/THARY-VIREAK/Documents/Year4/Java/Thary%20Vireak/src/main/resources/application.properties).

### 1. PostgreSQL Database
Create the database in PostgreSQL before starting the application:
```sql
CREATE DATABASE db_one;
```
Configure your credentials in `application.properties`:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/db_one
spring.datasource.username=reak
spring.datasource.password=123
spring.jpa.hibernate.ddl-auto=update
```

### 2. JWT Configuration
```properties
application.security.jwt.secret-key=<your-256-bit-secret-key>
application.security.jwt.expiration=86400000
application.security.jwt.refresh-token.expiration=604800000
```

### 3. Gmail SMTP & OTP
To send OTP emails, configure your Gmail App Password:
```properties
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your-email@gmail.com
spring.mail.password=your-gmail-app-password
app.mail.otp.expiration-minutes=5
```

---

## 🚦 Getting Started

### 1. Clone the Repository
```bash
git clone <repository-url>
cd "Thary Vireak"
```

### 2. Build the Project
Using the Gradle wrapper:
```bash
./gradlew clean build -x test
```

### 3. Run the Application
```bash
./gradlew bootRun
```
The server will start at `http://localhost:8080`.

---

## 📖 API Documentation & Swagger UI

Once the application is running, access the interactive Swagger UI documentation at:

👉 **[http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)**

OpenAPI JSON specification:
👉 **[http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)**

---

## 📌 API Endpoints Overview

### 🔐 Authentication (`/api/v1/auth`)
| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/v1/auth/register` | Register new user account |
| `POST` | `/api/v1/auth/login` | Authenticate and obtain JWT token |
| `POST` | `/api/v1/auth/refresh-token` | Refresh expired access token |

### ✉️ Email OTP (`/api/v1/otp`)
| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/v1/otp/send` | Generate and send OTP code to email |
| `POST` | `/api/v1/otp/verify` | Verify submitted OTP code |

### 🏫 School Management
| Resource | Base Path | Key Operations |
|---|---|---|
| **Students** | `/api/v1/students` | CRUD, assign card, enroll major |
| **Teachers** | `/api/v1/teachers` | CRUD, assign subjects |
| **Cards** | `/api/v1/cards` | CRUD student identification cards |
| **Majors** | `/api/v1/majors` | CRUD academic departments / majors |
| **Subjects** | `/api/v1/subjects` | CRUD course subjects |

### 🛒 E-Commerce & Products
| Resource | Base Path | Key Operations |
|---|---|---|
| **Customers** | `/api/v1/customers` | CRUD customer records |
| **Products** | `/api/v1/products` | CRUD products, inventory tracking |
| **Orders** | `/api/v1/orders` | Manage customer purchase orders |
| **Product-Orders** | `/api/v1/product-orders` | Line items and order relationships |
| **Emails** | `/api/v1/emails` | Customer email correspondence records |

### ⚡ Cache Management & Logs
| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/v1/cache` | Inspect cache names and stats |
| `DELETE` | `/api/v1/cache/{name}` | Clear a specific cache |
| `DELETE` | `/api/v1/cache` | Evict all caches |
| `GET` | `/api/v1/activity-logs` | Query activity audit history |

---

## 🧪 Testing

Run automated unit and integration tests:
```bash
./gradlew test
```

---

## 📁 Project Structure

```text
├── build.gradle.kts          # Gradle build dependencies & plugins
├── settings.gradle.kts       # Gradle project settings
├── src
│   ├── main
│   │   ├── java/com/example/demo
│   │   │   ├── config/       # Security, Swagger, Cache, Logging configs
│   │   │   ├── constant/     # Application constants
│   │   │   ├── controller/   # REST Controllers (Auth, OTP, School, Product, etc.)
│   │   │   ├── dto/          # Request & Response Data Transfer Objects
│   │   │   ├── entity/       # JPA Entities & Table mappings
│   │   │   ├── exception/    # Global exception handler & custom exceptions
│   │   │   ├── repository/   # Spring Data JPA repositories
│   │   │   ├── service/      # Business logic interfaces & implementations
│   │   │   └── util/         # Utility helpers
│   │   └── resources
│   │       ├── application.properties
│   └── test/                 # Unit & integration test suites
└── .gitignore                # Git ignore rules
```

---

## 📄 License
This project is licensed under the MIT License - see the LICENSE file for details.
