// ============================================================
// Electricity Outage Reporting System — Jenkinsfile
// Declarative Pipeline with 9 stages
// ============================================================
pipeline {
    agent any

    environment {
        APP_NAME        = 'electricity-outage-reporting'
        IMAGE_NAME      = "yashgade0045/${APP_NAME}"
        IMAGE_TAG       = "${env.BUILD_NUMBER}-${env.GIT_COMMIT?.take(7) ?: 'local'}"
        REGISTRY_CREDS  = 'dockerhub-credentials'    // Jenkins credential ID
        APP_PORT        = '8080'
        HEALTH_URL      = "http://localhost:${APP_PORT}/actuator/health"
        MAVEN_CMD       = (isUnix() ? './mvnw' : '.\\mvnw.cmd')
    }

    options {
        buildDiscarder(logRotator(numToKeepStr: '10'))
        timeout(time: 30, unit: 'MINUTES')
        timestamps()
    }

    stages {

        // Stage 1 — Checkout
        stage('Checkout') {
            steps {
                echo "Checking out source code..."
                checkout scm
                script {
                    env.GIT_COMMIT_SHORT = sh(
                        script: 'git rev-parse --short HEAD',
                        returnStdout: true
                    ).trim()
                    echo "Commit: ${env.GIT_COMMIT_SHORT}"
                }
            }
        }

        // Stage 2 — Build
        stage('Build') {
            steps {
                echo "Building application with Maven..."
                script {
                    if (isUnix()) {
                        sh 'chmod +x mvnw'
                        sh './mvnw clean compile -B --no-transfer-progress'
                    } else {
                        bat '.\\mvnw.cmd clean compile -B --no-transfer-progress'
                    }
                }
            }
        }

        // Stage 3 — Unit and Integration Tests
        stage('Unit & Integration Tests') {
            steps {
                echo "Running unit and integration tests..."
                script {
                    if (isUnix()) {
                        sh './mvnw verify -B --no-transfer-progress'
                    } else {
                        bat '.\\mvnw.cmd verify -B --no-transfer-progress'
                    }
                }
            }
            post {
                always {
                    // Publish JUnit test results
                    junit testResults: '**/target/surefire-reports/*.xml,**/target/failsafe-reports/*.xml',
                          allowEmptyResults: true
                    // Publish code coverage (if JaCoCo plugin added)
                    // jacoco execPattern: '**/target/jacoco.exec'
                }
            }
        }

        // Stage 4 — Selenium Tests (optional: run only if Chrome available)
        stage('Selenium Tests') {
            when {
                expression {
                    // Only run Selenium tests if explicitly enabled
                    return params.RUN_SELENIUM_TESTS == true
                }
            }
            steps {
                echo "Running Selenium browser tests..."
                script {
                    if (isUnix()) {
                        sh './mvnw verify -Pselenium -B --no-transfer-progress'
                    } else {
                        bat '.\\mvnw.cmd verify -Pselenium -B --no-transfer-progress'
                    }
                }
            }
            post {
                always {
                    junit testResults: '**/target/failsafe-reports/*.xml',
                          allowEmptyResults: true
                }
            }
        }

        // Stage 5 — Test Report Publication
        stage('Publish Test Reports') {
            steps {
                echo "Archiving test reports..."
                // Reports already published in Stage 3 post block.
                // This stage archives any additional HTML reports if generated.
                archiveArtifacts artifacts: '**/target/surefire-reports/**', allowEmptyArchive: true
            }
        }

        // Stage 6 — Package
        stage('Package') {
            steps {
                echo "Packaging application JAR..."
                script {
                    if (isUnix()) {
                        sh './mvnw package -DskipTests -B --no-transfer-progress'
                    } else {
                        bat '.\\mvnw.cmd package -DskipTests -B --no-transfer-progress'
                    }
                }
                archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
            }
        }

        // Stage 7 — Docker Image Build
        stage('Docker Build') {
            steps {
                echo "Building Docker image: ${IMAGE_NAME}:${IMAGE_TAG}"
                script {
                    if (isUnix()) {
                        sh "docker build -t ${IMAGE_NAME}:${IMAGE_TAG} -t ${IMAGE_NAME}:latest ."
                    } else {
                        bat "docker build -t ${IMAGE_NAME}:${IMAGE_TAG} -t ${IMAGE_NAME}:latest ."
                    }
                }
            }
        }

        // Stage 8 — Push & Deploy
        stage('Push & Deploy') {
            when {
                branch 'main'   // Only deploy from the main branch
            }
            steps {
                echo "Pushing image to Docker Hub..."
                withCredentials([usernamePassword(
                    credentialsId: "${REGISTRY_CREDS}",
                    usernameVariable: 'DOCKER_USER',
                    passwordVariable: 'DOCKER_PASS'
                )]) {
                    script {
                        if (isUnix()) {
                            sh '''
                                echo "$DOCKER_PASS" | docker login -u "$DOCKER_USER" --password-stdin
                                docker push ''' + IMAGE_NAME + ':' + IMAGE_TAG + '''
                                docker push ''' + IMAGE_NAME + ''':latest
                                docker logout
                            '''
                        } else {
                            bat """
                                echo %DOCKER_PASS% | docker login -u %DOCKER_USER% --password-stdin
                                docker push ${IMAGE_NAME}:${IMAGE_TAG}
                                docker push ${IMAGE_NAME}:latest
                                docker logout
                            """
                        }
                    }
                }

                echo "Deploying to test environment..."
                script {
                    if (isUnix()) {
                        sh '''
                            docker stop outage-app-test 2>/dev/null || true
                            docker rm   outage-app-test 2>/dev/null || true
                            docker run -d \
                              --name outage-app-test \
                              -p ''' + APP_PORT + ''':8080 \
                              --restart unless-stopped \
                              ''' + IMAGE_NAME + ':' + IMAGE_TAG + '''
                        '''
                    } else {
                        bat """
                            docker stop outage-app-test 2>NUL || exit /b 0
                            docker rm   outage-app-test 2>NUL || exit /b 0
                            docker run -d --name outage-app-test -p ${APP_PORT}:8080 --restart unless-stopped ${IMAGE_NAME}:${IMAGE_TAG}
                        """
                    }
                }
            }
        }

        // Stage 9 — Post-Deployment Health Check
        stage('Health Check') {
            when {
                branch 'main'
            }
            steps {
                echo "Waiting for application to start..."
                script {
                    sleep(time: 30, unit: 'SECONDS')
                    def healthy = false
                    for (int attempt = 1; attempt <= 5; attempt++) {
                        try {
                            if (isUnix()) {
                                def result = sh(
                                    script: "curl -s -o /dev/null -w '%{http_code}' ${HEALTH_URL}",
                                    returnStdout: true
                                ).trim()
                                healthy = (result == '200')
                            } else {
                                def result = bat(
                                    script: "curl -s -o NUL -w \"%%{http_code}\" ${HEALTH_URL}",
                                    returnStdout: true
                                ).trim()
                                healthy = result.contains('200')
                            }
                        } catch (ignored) { /* wait and retry */ }

                        if (healthy) {
                            echo "Health check passed on attempt ${attempt}."
                            break
                        }
                        echo "Attempt ${attempt} failed. Waiting 15s before retry..."
                        sleep(time: 15, unit: 'SECONDS')
                    }

                    if (!healthy) {
                        error("Health check FAILED after 5 attempts. Deployment rejected.")
                    }
                }
            }
        }
    }

    // ── Post-Pipeline Actions ─────────────────────────────────────────────────
    post {
        success {
            echo "Pipeline completed successfully. Build ${env.BUILD_NUMBER} deployed."
        }
        failure {
            echo "Pipeline FAILED. Investigate the failed stage above."
            // Optionally: emailext or Slack notification here
        }
        always {
            cleanWs()
        }
    }

    // ── Parameters ────────────────────────────────────────────────────────────
    // Uncomment the block below after first Jenkins run (parameters need one run to appear):
    /*
    parameters {
        booleanParam(
            name: 'RUN_SELENIUM_TESTS',
            defaultValue: false,
            description: 'Run Selenium browser tests (requires Chrome on agent)'
        )
    }
    */
}
