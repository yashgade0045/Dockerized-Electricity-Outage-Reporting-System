# Deployment Guide
## Electricity Outage Reporting System

---

## 1. Local Development (No Docker)

### Prerequisites
- Java 17 or later (Java 24 works)
- Git

### Steps
```bash
# Clone the repository
git clone https://github.com/yashgade0045/Dockerized-Electricity-Outage-Reporting-System.git
cd Dockerized-Electricity-Outage-Reporting-System

# Run the application (Maven Wrapper included — no Maven install needed)
.\mvnw.cmd spring-boot:run        # Windows
./mvnw spring-boot:run            # Linux / macOS
```

Access: http://localhost:8080
Login: `user/user123` (USER) or `admin/admin123` (ADMIN)

---

## 2. Docker Deployment (Local)

### Prerequisites
- Docker Desktop installed and running

### Install Docker Desktop (Windows)
1. Download from: https://www.docker.com/products/docker-desktop/
2. Run the installer
3. Restart your computer when prompted
4. Start Docker Desktop from the Start menu
5. Verify: `docker --version`

### Build the Docker image
```bash
docker build -t electricity-outage-reporting:1.0 .
```

### Run the container (ephemeral — data lost on stop)
```bash
docker run --rm -p 8080:8080 electricity-outage-reporting:1.0
```

### Run with persistent data (data survives container restarts)
```bash
docker run -d \
  --name outage-app \
  -p 8080:8080 \
  -v outage-data:/app/data \
  -e SPRING_DATASOURCE_URL="jdbc:h2:file:/app/data/outagedb;DB_CLOSE_DELAY=-1" \
  electricity-outage-reporting:1.0
```

### Container lifecycle commands
```bash
docker logs outage-app            # View application logs
docker logs -f outage-app         # Follow live logs
docker stop outage-app            # Stop the container
docker start outage-app           # Start a stopped container
docker restart outage-app         # Restart
docker rm outage-app              # Remove (must be stopped first)
docker rm -f outage-app           # Force remove (running or stopped)
docker inspect outage-app         # Full container details
```

### Health check
```bash
curl http://localhost:8080/actuator/health
# Expected: {"status":"UP"}
```

---

## 3. Docker Image Tagging and Registry

### Tag with version and commit ID
```bash
# Get current commit short hash
git rev-parse --short HEAD

# Build with version + commit tags
docker build \
  -t yashgade0045/electricity-outage-reporting:1.0.0 \
  -t yashgade0045/electricity-outage-reporting:abc1234 \
  -t yashgade0045/electricity-outage-reporting:latest \
  .
```

### Authenticate to Docker Hub
```bash
docker login
# Enter your Docker Hub username and password (or PAT)
```

### Push the image
```bash
docker push yashgade0045/electricity-outage-reporting:1.0.0
docker push yashgade0045/electricity-outage-reporting:latest
```

> **WARNING:** Do not push images until you have verified the image works locally. Do not push if it contains any secrets.

### Pull and run a specific version
```bash
docker pull yashgade0045/electricity-outage-reporting:1.0.0
docker run -d --name outage-app -p 8080:8080 \
  yashgade0045/electricity-outage-reporting:1.0.0
```

---

## 4. Rollback Procedure

If a new deployment is unhealthy:

```bash
# Stop the failed container
docker stop outage-app
docker rm outage-app

# Start the previous stable version
docker run -d --name outage-app -p 8080:8080 \
  yashgade0045/electricity-outage-reporting:PREVIOUS_VERSION

# Verify health
curl http://localhost:8080/actuator/health
```

> **Note:** Application rollback restores the code. Database rollback (schema changes, data changes) is separate and requires database backup/restore procedures. H2 in file mode can be backed up by copying the `.mv.db` file.

---

## 5. Ansible Deployment (Linux Server)

### Prerequisites on control machine (your laptop)
```bash
pip install ansible
ansible --version
```

### Prerequisites on target server
- Ubuntu 22.04 LTS
- SSH access with key authentication
- Python 3 installed

### Setup
```bash
# Copy and configure inventory
cp ansible/inventory.ini.example ansible/inventory.ini
# Edit inventory.ini with your server IP and SSH key

# Test connectivity
ansible -i ansible/inventory.ini all -m ping

# Run the playbook
ansible-playbook -i ansible/inventory.ini ansible/setup.yml

# Verify idempotency: run again, should report no changes
ansible-playbook -i ansible/inventory.ini ansible/setup.yml
```

### With Docker Hub credentials
```bash
ansible-playbook -i ansible/inventory.ini ansible/setup.yml \
  --extra-vars "dockerhub_password=YOUR_DOCKER_HUB_PAT"
```

---

## 6. Jenkins Pipeline Setup

### Install Jenkins (Windows)
```bash
# Download Jenkins WAR
# https://www.jenkins.io/download/ → Generic Java package (.war)

# Run Jenkins
java -jar jenkins.war --httpPort=8090
```

### Install Jenkins (via Winget)
```bash
winget install Jenkins.Jenkins
```

Access: http://localhost:8090

### Required Jenkins Plugins
- Git
- Pipeline
- Maven Integration
- Docker Pipeline
- JUnit
- HTML Publisher (for test reports)

### Jenkins Credentials to Add
| ID | Type | Purpose |
|---|---|---|
| `dockerhub-credentials` | Username + Password | Docker Hub login |
| `github-credentials` | Username + PAT | Private repo access (if needed) |

### Create Pipeline Job
1. New Item → Pipeline
2. Pipeline → Definition: Pipeline script from SCM
3. SCM: Git → Repository URL: your GitHub URL
4. Script Path: `Jenkinsfile`
5. Save and click "Build Now"

---

## 7. Environment Variables Reference

| Variable | Default | Description |
|---|---|---|
| `SERVER_PORT` | `8080` | Application port |
| `SPRING_DATASOURCE_URL` | `jdbc:h2:mem:outagedb` | Database URL |
| `SPRING_PROFILES_ACTIVE` | `default` | Active Spring profile |
| `LOGGING_LEVEL_COM_EXAMPLE_OUTAGE` | `INFO` | Application log level |

Example: override when running Docker container:
```bash
docker run -e SPRING_PROFILES_ACTIVE=prod \
           -e SPRING_DATASOURCE_URL="jdbc:h2:file:/app/data/outagedb" \
           -p 8080:8080 electricity-outage-reporting:1.0
```
