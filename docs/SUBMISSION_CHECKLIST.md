# Final Submission Checklist
## Electricity Outage Reporting System

> [!NOTE]
> Items marked ✅ are complete. Items marked 🔲 require **manual capture** (screenshots, logs) after running the application.

---

## Source Code & Repository

- [x] `pom.xml` — Maven project with all dependencies
- [x] `src/main/java/` — Full application source code
- [x] `src/test/java/` — Unit, integration, and Selenium tests
- [x] `src/main/resources/templates/` — All Thymeleaf templates
- [x] `src/main/resources/static/` — CSS and JavaScript
- [x] `src/main/resources/application.properties` — Application config
- [x] `mvnw` / `mvnw.cmd` — Maven Wrapper
- [x] `.gitignore` — Covers build output, secrets, IDE files
- [x] `README.md` — Setup instructions and documentation

---

## Requirements & Architecture

- [x] `docs/SRS.md` — Problem statement, requirements, use cases, user stories, 15-week plan
- [x] `docs/ARCHITECTURE.md` — System design with Mermaid diagrams
- [x] `docs/TEST_PLAN.md` — Test strategy and all test case IDs
- [x] `docs/DEPLOYMENT.md` — Docker, Ansible, Jenkins setup guide
- [x] `docs/TROUBLESHOOTING.md` — Common issues and fixes
- [x] `docs/VIVA_QUESTIONS.md` — 27 viva Q&A covering all technologies

---

## DevOps Files

- [x] `Dockerfile` — Multi-stage build (JDK build → JRE runtime)
- [x] `.dockerignore` — Excludes unnecessary files from image
- [x] `Jenkinsfile` — 9-stage declarative pipeline
- [x] `ansible/setup.yml` — Idempotent deployment playbook
- [x] `ansible/inventory.ini.example` — Inventory template (no secrets)

---

## Git & GitHub

- [x] `.gitignore` properly configured
- [x] Git initialized (run `git init` if not done)
- [ ] 🔲 Initial commit pushed to GitHub
- [ ] 🔲 Screenshot of GitHub repository with commit history
- [ ] 🔲 Feature branch created (`git checkout -b feature/something`)
- [ ] 🔲 Pull request created and merged (GitHub UI)

### Git commands to run:
```bash
git init
git branch -M main
git add .
git commit -m "feat: initial project — complete Electricity Outage Reporting System"
git remote add origin https://github.com/yashgade0045/Dockerized-Electricity-Outage-Reporting-System.git
git push -u origin main
```

---

## Application Evidence (Capture Manually)

### Running Application Screenshots
- [ ] 🔲 Login page (`/login`)
- [ ] 🔲 Dashboard with real counts (`/`)
- [ ] 🔲 Reports list with data (`/reports`)
- [ ] 🔲 Create report form (`/reports/new`)
- [ ] 🔲 Report detail page with status badge (`/reports/1`)
- [ ] 🔲 Edit report form (logged in as admin)
- [ ] 🔲 Status update — before and after
- [ ] 🔲 Search results (with keyword in search bar)
- [ ] 🔲 Filter by status (REPORTED / IN_PROGRESS / RESOLVED)
- [ ] 🔲 Empty state (search with no results)
- [ ] 🔲 Validation error on create form (submit empty form)
- [ ] 🔲 403 Forbidden — access admin URL as regular user
- [ ] 🔲 Mobile view (browser dev tools → responsive mode)

Save screenshots to: `evidence/screenshots/`

---

## Test Results (Capture After Build)

- [ ] 🔲 `.\mvnw.cmd clean verify` — Copy terminal output to `evidence/logs/maven-test-output.txt`
- [ ] 🔲 Test report HTML — copy `target/site/surefire-report.html` to `evidence/test-reports/`
- [ ] 🔲 All 28 test cases shown as PASSED

```bash
# Run tests and generate report
.\mvnw.cmd clean verify surefire-report:report
```

---

## Docker Evidence

- [ ] 🔲 `docker build` command output — save to `evidence/logs/docker-build.txt`
- [ ] 🔲 `docker images` showing the built image
- [ ] 🔲 `docker run` — application running in container
- [ ] 🔲 `curl http://localhost:8080/actuator/health` — {"status":"UP"}
- [ ] 🔲 `docker logs outage-app` — application startup logs

```bash
docker build -t electricity-outage-reporting:1.0 . 2>&1 | tee evidence/logs/docker-build.txt
docker run -d --name outage-app -p 8080:8080 electricity-outage-reporting:1.0
docker logs outage-app > evidence/logs/docker-logs.txt
curl http://localhost:8080/actuator/health
```

---

## Jenkins Evidence

- [ ] 🔲 Jenkins pipeline — all 9 stages shown as green ✅
- [ ] 🔲 Screenshot of Jenkins Blue Ocean or classic view
- [ ] 🔲 Jenkins console output for a successful build
- [ ] 🔲 JUnit test results published in Jenkins
- [ ] 🔲 Archived JAR artifact

---

## Ansible Evidence (If Deploying to Linux Server)

- [ ] 🔲 First `ansible-playbook` run — shows tasks CHANGED
- [ ] 🔲 Second run — shows tasks OK (idempotency)
- [ ] 🔲 Health check passes on server: `curl http://SERVER_IP:8080/actuator/health`
- [ ] 🔲 Rollback demonstrated (stop container, start previous version)

---

## Viva Preparation

- [x] `docs/VIVA_QUESTIONS.md` — 27 questions across all topics
- [ ] 🔲 Read and practice explaining each layer of the application
- [ ] 🔲 Be able to walk through the Dockerfile and explain each instruction
- [ ] 🔲 Be able to explain what happens when `@PreAuthorize` blocks a request
- [ ] 🔲 Be able to draw the architecture diagram on paper

---

## Known Limitations to Mention in Viva

1. H2 in-memory database: data lost on restart (documented; file-mode is solution)
2. In-memory user store: suitable for demo, not production
3. Selenium tests require Chrome (excluded from default build)
4. Docker Desktop required (not available on all systems)
5. No real production database migration strategy (would need Flyway)
