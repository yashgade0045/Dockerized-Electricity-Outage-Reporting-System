# Viva Preparation — Questions and Answers
## Electricity Outage Reporting System

**Author:** Yash Gade | **Date:** October 2026

---

## Spring Boot & Java

**Q1. What is Spring Boot and why did you use it?**
Spring Boot is a framework that simplifies building Java web applications by providing auto-configuration, embedded server (Tomcat), and starter dependencies. I used it because it lets me focus on business logic instead of boilerplate setup. With `spring-boot-starter-web`, `spring-boot-starter-data-jpa`, and `spring-boot-starter-security`, I had a working application with minimal configuration.

**Q2. Explain the layers in your application.**
My application follows a standard three-layer MVC architecture:
- **Controller** — receives HTTP requests, validates input, calls the service, and returns a view
- **Service** — contains business logic (creating reports, dashboard counts, status transitions)
- **Repository** — data access via Spring Data JPA, backed by H2 database

**Q3. What is Thymeleaf and how does it work?**
Thymeleaf is a server-side Java template engine. The controller adds data to a `Model` object, and Thymeleaf processes HTML templates using `th:` attributes to inject values. For example, `th:text="${report.title}"` renders the report's title. Templates are processed on the server and the client receives plain HTML.

**Q4. What is the difference between `@GetMapping` and `@PostMapping`?**
`@GetMapping` handles HTTP GET requests — used to display pages. `@PostMapping` handles HTTP POST requests — used when forms submit data. In my project, `GET /reports/new` shows the form and `POST /reports/new` processes the submission.

**Q5. What is Bean Validation? Show an example from your code.**
Bean Validation (JSR-380) uses annotations on a DTO to declare constraints. In `OutageReportDto`:
```java
@NotBlank(message = "Title is required.")
@Size(min = 5, max = 150, message = "Title must be between 5 and 150 characters.")
private String title;
```
Spring MVC checks these when `@Valid` is placed on the method parameter. If validation fails, `BindingResult` contains the errors and I redisplay the form.

---

## Spring Security

**Q6. How does Spring Security work in your project?**
Every HTTP request passes through the Spring Security filter chain. I configured it in `SecurityConfig`:
- Public paths (`/login`, `/css/**`, `/actuator/health`) are permitted to all
- All other paths require authentication
- A custom login form at `/login` is used
- Two in-memory users: `user` (ROLE_USER) and `admin` (ROLE_ADMIN)

**Q7. What is BCrypt and why do you use it for passwords?**
BCrypt is an adaptive password hashing algorithm. It automatically adds a random salt and applies a configurable number of rounds (cost factor). Even if the database is compromised, the original passwords cannot be recovered easily. I use `BCryptPasswordEncoder` which is Spring Security's recommended encoder.

**Q8. How do you enforce role-based access control?**
I use both URL-level and method-level security:
- URL-level: in `SecurityConfig.filterChain()` — e.g., H2 console requires ADMIN
- Method-level: `@PreAuthorize("hasRole('ADMIN')")` on controller methods like `editReport()`, `updateStatus()`, and `deleteReport()`
This means even if someone calls the URL directly without the UI, they get a 403 Forbidden response.

**Q9. Why are permissions enforced on the server, not just hidden in the HTML?**
Hiding buttons in HTML doesn't prevent a malicious user from sending a direct POST request to `/reports/1/delete`. The server must verify authorization on every request. I hide admin buttons in Thymeleaf using `sec:authorize="hasRole('ADMIN')"` for UX, but the real protection is `@PreAuthorize` on the controller method.

---

## Docker

**Q10. What is Docker and why is it useful?**
Docker packages an application with all its dependencies into a container — a lightweight, portable unit that runs the same way on any machine. Without Docker, "it works on my laptop but not the server" is a common problem. Docker eliminates environment differences.

**Q11. Explain your Dockerfile — what are the two stages?**
My Dockerfile uses a multi-stage build:
- **Stage 1 (builder):** Uses `eclipse-temurin:21-jdk-alpine` — downloads Maven dependencies and compiles the JAR
- **Stage 2 (runtime):** Uses `eclipse-temurin:21-jre-alpine` — copies only the JAR. The JDK is not included, making the final image much smaller and more secure.

**Q12. What is a health check in Docker and how did you implement it?**
A Docker health check periodically runs a command inside the container to verify the application is working. Mine polls the Spring Boot Actuator endpoint:
```dockerfile
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD wget -qO- http://localhost:8080/actuator/health || exit 1
```
If the application is unhealthy, Docker marks the container as `unhealthy` and the orchestrator can restart it.

**Q13. Why does H2 data not survive container restarts?**
By default, I use `jdbc:h2:mem:outagedb` — an in-memory database that exists only for the life of the JVM process. When the container stops, the JVM stops, and the data is gone. To persist data across restarts, I would:
1. Switch to file-mode: `jdbc:h2:file:/app/data/outagedb`
2. Mount a Docker volume: `-v outage-data:/app/data`

---

## Jenkins & CI/CD

**Q14. What is Continuous Integration (CI)?**
CI is the practice of automatically building and testing code every time it is pushed to the repository. Jenkins watches the GitHub repository, and when new code arrives, it runs all 9 stages — compile, test, build Docker image. If tests fail, the pipeline stops and the developer is notified.

**Q15. Explain the 9 stages in your Jenkinsfile.**
1. **Checkout** — Pulls the latest code from GitHub
2. **Build** — Compiles Java source with Maven
3. **Unit & Integration Tests** — Runs JUnit and MockMvc tests
4. **Selenium Tests** — Runs browser tests (optional, gated by parameter)
5. **Publish Reports** — Archives JUnit XML results in Jenkins UI
6. **Package** — Creates the executable JAR file
7. **Docker Build** — Builds the Docker image with build number tag
8. **Push & Deploy** — Pushes image to Docker Hub, deploys to test server (main branch only)
9. **Health Check** — Polls `/actuator/health`; fails pipeline if not healthy

**Q16. How do you store secrets in Jenkins?**
I use Jenkins Credentials (encrypted secret store). In the Jenkinsfile, I reference credentials by ID:
```groovy
withCredentials([usernamePassword(credentialsId: 'dockerhub-credentials', ...)]) {
    sh "docker login -u $DOCKER_USER --password-stdin"
}
```
The actual password is never printed in logs (`no_log: true` in Ansible; Jenkins masks credential values).

---

## Ansible

**Q17. What is Ansible and what does it do in your project?**
Ansible is a configuration management and automation tool. It connects to servers via SSH and runs tasks defined in YAML playbooks. In my project, `setup.yml` installs Docker on the Linux server, creates the application user and directories, writes a `docker-compose.yml`, and starts the application — all idempotently.

**Q18. What does "idempotent" mean in Ansible?**
Idempotent means you can run the playbook multiple times and get the same result — it doesn't create duplicates or break a working installation. For example, `ansible.builtin.apt` installs a package only if it's not already installed. Running the playbook twice on the same server produces no changes on the second run.

**Q19. Why do you have `inventory.ini.example` instead of `inventory.ini`?**
`inventory.ini` contains the real server IP address and SSH key path, which are environment-specific and could be sensitive. I commit the example file (with placeholder values) to Git, and the real `inventory.ini` is listed in `.gitignore` so it's never committed.

---

## Testing

**Q20. What is the difference between unit tests and integration tests?**
- **Unit tests** test a single class in isolation using mocks. My `OutageReportServiceTest` uses Mockito to mock the repository, so no database is involved.
- **Integration tests** test multiple components together. My `OutageReportControllerTest` uses MockMvc with a real in-memory H2 database and the actual Spring context. It verifies that controllers, services, and repositories work correctly together.

**Q21. What does `@WithMockUser` do in your tests?**
It's a Spring Security Test annotation that sets up a security context for a test, simulating a logged-in user with specific roles. For example:
```java
@WithMockUser(username = "admin", roles = "ADMIN")
```
Without this, the test request would be redirected to the login page.

**Q22. How do Selenium tests work?**
Selenium WebDriver automates a real browser. My tests:
1. Start the Spring Boot application on a random port (`@SpringBootTest(webEnvironment = RANDOM_PORT)`)
2. Launch headless Chrome via WebDriverManager (which downloads ChromeDriver automatically)
3. Navigate to pages, fill forms, click buttons, and assert what appears on screen
4. Verify the full user journey works — login → create report → see success message

---

## Git & GitHub

**Q23. What is a feature branch and why use one?**
A feature branch is a separate branch where a new feature is developed without affecting the `main` branch. Once the feature is complete and tested, it's merged via a pull request that teammates review. This prevents broken code from reaching production.

**Q24. Why is `.gitignore` important?**
It tells Git which files to ignore — preventing accidental commits of:
- `target/` — compiled build output (large, regenerable)
- `.env` — environment secrets (passwords, tokens)
- `.idea/` — IDE-specific files (not needed by others)
- `*.log` — log files

**Q25. What is a Git tag and how do you use it?**
A tag marks a specific commit as a release point:
```bash
git tag -a v1.0.0 -m "First working release"
git push origin v1.0.0
```
Jenkins can trigger a release pipeline when a tag is pushed.

---

## General DevOps

**Q26. What is the difference between Continuous Integration, Continuous Delivery, and Continuous Deployment?**
- **CI** — Automatically build and test every code change
- **Continuous Delivery** — Automatically prepare a deployable artifact (JAR, Docker image), but deployment requires manual approval
- **Continuous Deployment** — Automatically deploy to production if all tests pass (no manual step)

My pipeline implements CI + Continuous Delivery (auto-push to Docker Hub, deploy to test environment; production requires manual approval).

**Q27. What happens if the health check fails in your pipeline?**
In Stage 9, the pipeline polls `/actuator/health` up to 5 times with 15-second delays. If no healthy response is received, `error()` is called, which fails the Jenkins build with a clear message. The previous stable container (if any) continues running — no automatic rollback in this version, but the documentation explains the manual rollback command.
