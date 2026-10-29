# Electricity Outage Reporting System

A student DevOps engineering project — a fully functional web application for citizens to report and track electricity outages, built with Java, Spring Boot, Docker, Jenkins, and Ansible.

**GitHub Repository:** https://github.com/yashgade0045/Dockerized-Electricity-Outage-Reporting-System

---

## Technology Stack

| Layer | Technology |
|---|---|
| Language | Java 21 (runs on Java 17+) |
| Framework | Spring Boot 3.2, Spring MVC |
| Templates | Thymeleaf |
| Security | Spring Security (BCrypt, role-based) |
| Database | H2 (in-memory, dev) |
| Build | Maven 3.9 / Maven Wrapper |
| Testing | JUnit 5, Spring Boot Test, Mockito, Selenium WebDriver |
| Container | Docker (multi-stage build) |
| CI/CD | Jenkins (Declarative Pipeline) |
| Config Mgmt | Ansible |

---

## Quick Start (Local Development)

### Prerequisites
- Java 17 or later (Java 24 works fine)
- Git
- Docker Desktop (for container commands)

### 1. Clone the repository
```bash
git clone https://github.com/yashgade0045/Dockerized-Electricity-Outage-Reporting-System.git
cd Dockerized-Electricity-Outage-Reporting-System
```

### 2. Run the application
```bash
# Windows
.\mvnw.cmd spring-boot:run

# Linux / macOS
./mvnw spring-boot:run
```

### 3. Open in your browser
```
http://localhost:8080
```

### Development credentials
| Username | Password | Role |
|----------|----------|------|
| `user` | `user123` | Reporter (can create and view reports) |
| `admin` | `admin123` | Administrator (can edit, update status, delete) |

> **Note:** These credentials are for development only. In production, replace with a database-backed user store and load passwords from environment variables.

---

## Features

- **Dashboard** — Live counts: total, reported, in-progress, resolved
- **Create report** — Submit outage with title, location, description, reporter name
- **View all reports** — Paginated table with search and status filter
- **View detail** — Full report information with timestamps
- **Edit report** — Modify report details (Admin only)
- **Update status** — Change REPORTED → IN_PROGRESS → RESOLVED (Admin only)
- **Delete report** — Permanently remove a report with confirmation (Admin only)
- **Role-based access** — Permissions enforced server-side, not just in UI
- **Validation** — All fields validated with clear error messages
- **Responsive design** — Works on desktop and mobile

---

## Running Tests

```bash
# Run all unit and integration tests
.\mvnw.cmd clean verify

# Run with test report
.\mvnw.cmd clean verify surefire-report:report

# Run Selenium browser tests (requires Google Chrome)
.\mvnw.cmd verify -Pselenium
```

Test reports are generated at:
- `target/surefire-reports/` — Unit test results
- `target/failsafe-reports/` — Integration/Selenium test results

---

## Docker

### Build the Docker image
```bash
docker build -t electricity-outage-reporting:1.0 .
```

### Run the container
```bash
docker run --rm -p 8080:8080 electricity-outage-reporting:1.0
```

### Persistent data (use a volume to survive restarts)
```bash
docker run -d \
  --name outage-app \
  -p 8080:8080 \
  -v outage-data:/app/data \
  -e SPRING_DATASOURCE_URL="jdbc:h2:file:/app/data/outagedb" \
  electricity-outage-reporting:1.0
```

### Container management
```bash
docker logs outage-app          # View logs
docker stop outage-app          # Stop
docker start outage-app         # Start
docker rm outage-app            # Remove
```

---

## H2 Database Console (Development)

Access the H2 web console at: `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:outagedb`
- Username: `sa`
- Password: *(leave blank)*

> Requires Admin login to access.

---

## Project Structure

```
src/
├── main/
│   ├── java/com/example/outage/
│   │   ├── controller/       DashboardController, OutageReportController, AuthController
│   │   ├── service/          OutageReportService, OutageReportServiceImpl
│   │   ├── repository/       OutageReportRepository (JPA)
│   │   ├── model/            OutageReport, OutageStatus
│   │   ├── dto/              OutageReportDto, DashboardStatsDto
│   │   ├── config/           SecurityConfig, DataInitializer
│   │   └── exception/        OutageReportNotFoundException, GlobalExceptionHandler
│   └── resources/
│       ├── templates/        Thymeleaf HTML templates
│       ├── static/css/       main.css
│       ├── static/js/        main.js
│       └── application.properties
└── test/
    ├── unit/                 OutageReportServiceTest
    ├── integration/          OutageReportControllerTest
    └── selenium/             OutageReportingSeleniumTest

docs/           SRS, Architecture, Test Plan, Deployment, Troubleshooting, Viva Q&A
ansible/        Ansible inventory example and setup playbook
Dockerfile      Multi-stage Docker build
Jenkinsfile     9-stage Jenkins declarative pipeline
```

---

## Git Workflow

### Feature branch workflow
```bash
# Create a feature branch
git checkout -b feature/add-export-function

# Work, commit with meaningful messages
git add .
git commit -m "feat: add CSV export for resolved reports"

# Push and create a pull request
git push origin feature/add-export-function
```

### Commit message convention
```
feat:     new feature
fix:      bug fix
docs:     documentation only
test:     add or fix tests
refactor: code change with no feature or bug fix
chore:    build, CI, tooling changes
```

### Release tagging
```bash
git tag -a v1.0.0 -m "Release v1.0.0 — initial working application"
git push origin v1.0.0
```

---

## Jenkins Setup

1. Install Jenkins locally or use a server
2. Install plugins: Git, Maven Integration, Pipeline, Docker Pipeline, JUnit
3. Create a Pipeline job pointing to this repository
4. Add a `dockerhub-credentials` credential (Docker Hub username + PAT)
5. Run the pipeline

---

## Ansible Deployment

```bash
# Copy and configure the inventory
cp ansible/inventory.ini.example ansible/inventory.ini
# Edit inventory.ini with your server IP

# Run the playbook (does NOT run automatically — requires your approval)
ansible-playbook -i ansible/inventory.ini ansible/setup.yml
```

---

## Known Limitations

- H2 in-memory database data is lost on restart. Use the file-based H2 URL or switch to MySQL/PostgreSQL for persistence.
- Selenium tests require Google Chrome. They are excluded from the default `mvn verify` build.
- No email notification on new reports (future enhancement).
- Single-server deployment only (no load balancing).

## Future Enhancements

- Switch to PostgreSQL for production persistence
- Email/SMS notification on new reports
- Map view showing outage locations
- Photo upload capability
- Report export (CSV/PDF)
- Multi-tenancy support for different utility companies

---

## Documentation

| Document | Description |
|----------|-------------|
| [SRS.md](docs/SRS.md) | Software Requirements Specification |
| [ARCHITECTURE.md](docs/ARCHITECTURE.md) | System architecture and design |
| [TEST_PLAN.md](docs/TEST_PLAN.md) | Testing strategy and test cases |
| [DEPLOYMENT.md](docs/DEPLOYMENT.md) | Docker and production deployment guide |
| [TROUBLESHOOTING.md](docs/TROUBLESHOOTING.md) | Common issues and fixes |
| [VIVA_QUESTIONS.md](docs/VIVA_QUESTIONS.md) | Viva preparation Q&A |

---

## Author

**Yash Gade** — Engineering Student, DevOps Project
