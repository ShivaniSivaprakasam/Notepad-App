# 📝 NoteSphere

**A full-stack, production-style productivity suite** — rich-text notes, categories, tags, to-dos, calendar reminders with email notifications, note sharing, password-protected notes, and a complete CI/CD pipeline deploying to AWS.

![Backend](https://img.shields.io/badge/Backend-Spring%20Boot%204.1-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![Security](https://img.shields.io/badge/Security-Spring%20Security-6DB33F?style=for-the-badge&logo=springsecurity&logoColor=white)
![UI Engine](https://img.shields.io/badge/UI%20Engine-Thymeleaf-005F0F?style=for-the-badge&logo=thymeleaf&logoColor=white)
![Database](https://img.shields.io/badge/Database-MySQL%208.0-4479A1?style=for-the-badge&logo=mysql&logoColor=white)
![Containerized](https://img.shields.io/badge/Containerized-Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)
![CI/CD](https://img.shields.io/badge/CI%2FCD-Jenkins-D24939?style=for-the-badge&logo=jenkins&logoColor=white)
![Deployment](https://img.shields.io/badge/Deployment-AWS%20EC2-FF9900?style=for-the-badge&logo=amazonaws&logoColor=white)
![Live](https://img.shields.io/badge/Live%20Demo-56.228.75.206%3A8080-success?style=for-the-badge)

---

## 🔗 Live Demo

**http://56.228.75.206:8080**

*(Hosted on an AWS EC2 free-tier instance. May be temporarily offline for maintenance or cost management.)*

---

## 📌 Project Description

**NoteSphere** is a full-featured note-taking and personal productivity web application built on a layered Spring Boot architecture. It goes well beyond a simple CRUD notes app — combining rich-text editing, task management, calendar-based reminders with real email delivery, collaborative note sharing with permission levels, and per-note password protection, all wrapped in a complete DevOps pipeline (Jenkins → Docker Hub → AWS EC2).

The system was built incrementally as a portfolio project, with a strong emphasis on:
- Secure, ownership-scoped data access (preventing IDOR vulnerabilities)
- Clean separation of concerns (Controller → Service → Repository → Entity)
- Real deployment infrastructure, not just local demo code

The platform provides:
- 🔐 Full authentication — registration with email verification, login, forgot/reset password, "Remember Me", strong password enforcement
- 📄 Rich-text notes with categories, tags, pin/favorite, soft-delete (Trash), Archive, and password protection
- ✅ Per-note and standalone to-dos with a dedicated task dashboard
- 📅 Calendar with reminders, scheduled email notifications, and daily summary emails
- 🤝 Note sharing with View/Edit permission levels
- 📊 Live dashboard analytics and productivity tracking
- 🌗 Site-wide light/dark theming

---

## 🛠️ Tech Stack

| Layer | Technology |
|---|---|
| **Backend** | Java 17, Spring Boot 4.1.0, Spring Security, Spring Data JPA (Hibernate 7), Spring Mail |
| **Frontend** | Thymeleaf, HTML5/CSS3, Vanilla JavaScript, Quill.js (rich text editor) |
| **Database** | MySQL 8.0 |
| **Build Tool** | Maven |
| **Containerization** | Docker, Docker Compose |
| **CI/CD** | Jenkins (Maven build → Docker image → Docker Hub push) |
| **Hosting** | AWS EC2 (Ubuntu 22.04) |
| **Image Registry** | [Docker Hub](https://hub.docker.com/r/shivaniavani/notepad-app) |

---

## 🏗️ Architecture

- **Layered architecture**: Controller → Service → Repository → Entity, with DTOs separating persistence models from form/view binding
- **Security**: BCrypt password hashing, session-based authentication, ownership-scoped queries (`findByIdAndUser`) throughout to prevent Insecure Direct Object Reference (IDOR) vulnerabilities
- **Configuration**: All secrets (DB credentials, SMTP credentials, base URL) injected via environment variables — nothing sensitive committed to source control
- **Error handling**: Centralized `@ControllerAdvice` exception handling with a styled, standalone error page
- **Background jobs**: Spring `@Scheduled` tasks for reminder email delivery (60s polling) and daily summary emails (cron, timezone-pinned)

Local Code (IntelliJ)
│
▼
Jenkins Pipeline
├─ Maven Build (mvn clean package)
├─ Docker Image Build
└─ Push to Docker Hub
│
▼
Docker Hub Registry
(shivaniavani/notepad-app)
│
▼
AWS EC2 Instance
docker compose pull
docker compose up -d
│
▼
Live Application

---

## 💻 Local Setup

### Prerequisites
- Java 17
- Maven
- MySQL 8.0
- Docker & Docker Compose (optional, for containerized run)

### Option A — Run directly (IntelliJ / Maven)

1. Clone the repository and check out the `develop` branch:
```bash
   git clone https://github.com/yourusername/notesphere.git
   cd notesphere
   git checkout develop
```
2. Create a MySQL database:
```sql
   CREATE DATABASE notepad_db;
```
3. Set the following environment variables:
   | Variable | Description |
   |---|---|
   | `DB_PASSWORD` | Your local MySQL root password |
   | `MAIL_USERNAME` | Gmail address used for sending emails |
   | `MAIL_PASSWORD` | Gmail App Password (not your regular password) |
4. Run `NotepadApplication.java`.
5. Visit `http://localhost:8080`.

### Option B — Run with Docker Compose

```bash
docker-compose up --build
```

---

## ⚠️ Known Limitations & Design Tradeoffs

Documented honestly, as any real production readiness review would expect:

- `spring.jpa.ddl-auto=update` is used for development convenience; a production system would use Flyway/Liquibase migrations instead
- CSRF protection is disabled for development simplicity (would be re-enabled with Thymeleaf's CSRF token support in a hardened build)
- Reminder `repeatType` (Daily/Weekly/Monthly) is stored and displayed but does not yet auto-generate future occurrences
- A few delete operations use GET routes rather than strict REST DELETE/POST semantics
- Verification/reset tokens are stored as plain strings rather than hashed
- The AWS deployment runs on a `t3.micro` instance (1GB RAM) with swap space configured as a memory-pressure mitigation

---

## 🌳 Branching Strategy (Git Flow)

This repository follows **Git Flow**:

| Branch | Contents |
|---|---|
| `main` | This README only — documentation entry point |
| `develop` | Full, integrated application code (merged from feature branches) |
| `feature/backend-code` | Spring Boot backend — entities, services, controllers, repositories, config |
| `feature/frontend-code` | Thymeleaf templates, CSS, JavaScript |
| `feature/deployment` | Dockerfile, Docker Compose, Jenkinsfile |

Feature branches are preserved (not deleted) after merging, for full development history visibility.

---

## 👤 Author

Built by **Shivani** as an end-to-end portfolio project — from initial Spring Boot scaffolding through feature development, UI design, bug fixing, and full CI/CD deployment to AWS.
---

## 🔄 CI/CD Pipeline
