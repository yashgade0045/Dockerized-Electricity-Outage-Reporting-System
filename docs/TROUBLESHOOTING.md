# Troubleshooting Guide
## Electricity Outage Reporting System

---

## Application Won't Start

### Error: Port 8080 already in use
```
Web server failed to start. Port 8080 was already in use.
```
**Fix:** Find and stop the process using port 8080:
```powershell
# Windows — find the process
netstat -ano | findstr :8080
# Kill it (replace PID with the actual number)
taskkill /PID <PID> /F

# Or run on a different port:
.\mvnw.cmd spring-boot:run -Dspring-boot.run.arguments="--server.port=8081"
```

### Error: Java version mismatch
```
Unsupported class file major version
```
**Fix:** Ensure you're using Java 17 or later:
```bash
java -version
# Must show 17.x, 21.x, or 24.x
```

### Error: Maven build fails — cannot find symbol
Check that Lombok annotation processor is enabled. In IntelliJ IDEA:
- Settings → Build → Compiler → Annotation Processors → Enable annotation processing ✅

---

## Database Issues

### H2 Console shows no data on restart
**Cause:** Using in-memory H2 (`jdbc:h2:mem:`) — data is lost when the app stops.
**Fix:** Switch to file-mode H2 in `application.properties`:
```properties
spring.datasource.url=jdbc:h2:file:./data/outagedb;DB_CLOSE_DELAY=-1
```

### H2 Console: "Connection refused"
The H2 console is at `http://localhost:8080/h2-console`.
- JDBC URL must be `jdbc:h2:mem:outagedb` (not `jdbc:h2:~/test`)
- Login requires Admin credentials (`admin/admin123`)

---

## Spring Security Issues

### 403 Forbidden on all pages after login
**Cause:** CSRF token missing in POST forms.
**Fix:** Thymeleaf automatically adds CSRF tokens when using `th:action`. Make sure all forms use `th:action="@{/path}"` not `action="/path"`.

### Login redirects in a loop
**Cause:** Spring Security cannot find the `UserDetailsService`.
**Fix:** Ensure `SecurityConfig` is annotated with `@Configuration` and `@EnableWebSecurity`.

### Tests fail with 401 even with `@WithMockUser`
**Fix:** Make sure `spring-security-test` is on the test classpath and `@AutoConfigureMockMvc` is present on the test class.

---

## Docker Issues

### Docker: command not found
**Fix:** Install Docker Desktop and ensure it is running. Verify:
```bash
docker --version
docker info
```

### Container exits immediately
Check logs:
```bash
docker logs outage-app
```
Common causes: port conflict, wrong JAR name in Dockerfile.

### Docker build error: `./mvnw: Permission denied`
```bash
# In the Dockerfile builder stage, the mvnw file needs execute permissions
# This is handled by: RUN chmod +x mvnw
# If still failing on Linux, run locally:
git update-index --chmod=+x mvnw
git commit -m "chore: fix mvnw execute permission"
```

### Health check shows "unhealthy"
The application takes up to 60 seconds to start. `--start-period=60s` is set in the Dockerfile. Wait and check again:
```bash
docker inspect --format='{{.State.Health.Status}}' outage-app
```

---

## Jenkins Issues

### Jenkins: mvnw.cmd not found
**Cause:** Jenkins agent is Unix-based but the Jenkinsfile uses `mvnw.cmd`.
**Fix:** The Jenkinsfile uses `isUnix()` to pick the right command:
```groovy
def mvnCmd = isUnix() ? './mvnw' : '.\\mvnw.cmd'
sh "${mvnCmd} clean verify"
```

### Jenkins: Docker not found on agent
**Fix:** Install Docker on the Jenkins agent machine. For Linux agents:
```bash
sudo apt install docker.io
sudo usermod -aG docker jenkins
# Restart Jenkins
```

### Jenkins: Pipeline fails at Docker push
**Cause:** `dockerhub-credentials` not configured.
**Fix:** Jenkins → Manage Jenkins → Credentials → Add → Username with password → ID: `dockerhub-credentials`.

---

## Selenium Test Issues

### Tests fail: ChromeDriver not found
WebDriverManager downloads ChromeDriver automatically. If it fails (network issue):
```bash
# Manually download ChromeDriver matching your Chrome version
# https://chromedriver.chromium.org/downloads
# Set system property:
-Dwebdriver.chrome.driver=C:/path/to/chromedriver.exe
```

### Tests fail: Element not found (timeout)
**Cause:** Page loaded but element ID changed or page is slow.
**Fix:** Increase the wait timeout in `OutageReportingSeleniumTest.java`:
```java
wait = new WebDriverWait(driver, Duration.ofSeconds(20)); // increase from 10
```

---

## Build Issues

### `mvnw.cmd` is not recognized
```
'mvnw.cmd' is not recognized as an internal or external command
```
**Fix:** Run from the project root directory where `mvnw.cmd` is located:
```bash
cd "c:\Devops Project"
.\mvnw.cmd --version
```

### Maven downloads take a long time (first build)
Maven downloads all dependencies on the first run (~50–100 MB). This is normal. Subsequent builds use the local cache at `~/.m2/repository/`.

---

## Ansible Issues

### `ansible: command not found`
```bash
pip install ansible
# Or:
pip3 install ansible
```

### SSH connection refused
```
UNREACHABLE! => SSH connection failed
```
**Fix:** Test SSH manually: `ssh -i ~/.ssh/id_rsa ubuntu@<SERVER_IP>`
Ensure the SSH key is correct and the server is reachable.

### Task fails: Docker module not found
```bash
ansible-galaxy collection install community.docker
```
