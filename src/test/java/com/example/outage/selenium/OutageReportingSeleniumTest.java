package com.example.outage.selenium;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Selenium WebDriver browser tests for the Electricity Outage Reporting System.
 *
 * <p><strong>Prerequisites to run these tests:</strong>
 * <ul>
 *   <li>Google Chrome must be installed on the system.</li>
 *   <li>The application does NOT need to be running separately — it starts automatically
 *       via {@code @SpringBootTest(webEnvironment = RANDOM_PORT)}.</li>
 *   <li>ChromeDriver is downloaded automatically by WebDriverManager.</li>
 * </ul>
 *
 * <p>Run with: {@code mvn verify -Pselenium}
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Selenium Browser Tests")
class OutageReportingSeleniumTest {

    @LocalServerPort
    private int port;

    private static WebDriver driver;
    private static WebDriverWait wait;
    private String baseUrl;

    @BeforeAll
    static void setUpDriver() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");        // Headless mode for CI
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--window-size=1280,800");
        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @BeforeEach
    void setBaseUrl() {
        baseUrl = "http://localhost:" + port;
    }

    @AfterAll
    static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    /** Helper: log in as the given user. */
    private void loginAs(String username, String password) {
        driver.get(baseUrl + "/login");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("username")));
        driver.findElement(By.id("username")).clear();
        driver.findElement(By.id("username")).sendKeys(username);
        driver.findElement(By.id("password")).clear();
        driver.findElement(By.id("password")).sendKeys(password);
        driver.findElement(By.id("loginBtn")).click();
        wait.until(ExpectedConditions.urlContains("/"));
    }

    /** Helper: log out. */
    private void logout() {
        driver.findElement(By.id("logoutBtn")).click();
    }

    // ── Test 1: Login Page ────────────────────────────────────────────────────

    @Test
    @Order(1)
    @DisplayName("Login page loads correctly")
    void loginPageLoads() {
        driver.get(baseUrl + "/login");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("loginForm")));

        assertThat(driver.getTitle()).contains("Sign In");
        assertThat(driver.findElement(By.id("username")).isDisplayed()).isTrue();
        assertThat(driver.findElement(By.id("password")).isDisplayed()).isTrue();
        assertThat(driver.findElement(By.id("loginBtn")).isDisplayed()).isTrue();
    }

    // ── Test 2: Unauthenticated Access ────────────────────────────────────────

    @Test
    @Order(2)
    @DisplayName("Unauthenticated access redirects to login page")
    void unauthenticatedAccessRedirectsToLogin() {
        driver.get(baseUrl + "/reports");
        wait.until(ExpectedConditions.urlContains("/login"));
        assertThat(driver.getCurrentUrl()).contains("/login");
    }

    // ── Test 3: Dashboard ─────────────────────────────────────────────────────

    @Test
    @Order(3)
    @DisplayName("Dashboard loads with stats cards after login")
    void dashboardShowsStatCards() {
        loginAs("user", "user123");

        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("statTotal")));

        assertThat(driver.findElement(By.id("totalCount")).isDisplayed()).isTrue();
        assertThat(driver.findElement(By.id("reportedCount")).isDisplayed()).isTrue();
        assertThat(driver.findElement(By.id("inProgressCount")).isDisplayed()).isTrue();
        assertThat(driver.findElement(By.id("resolvedCount")).isDisplayed()).isTrue();
    }

    // ── Test 4: Create Report ─────────────────────────────────────────────────

    @Test
    @Order(4)
    @DisplayName("Creating an outage report succeeds and shows success message")
    void createOutageReport_success() {
        loginAs("user", "user123");

        driver.get(baseUrl + "/reports/new");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("createReportForm")));

        driver.findElement(By.id("title")).sendKeys("Selenium Test — Outage on Test Lane");
        driver.findElement(By.id("location")).sendKeys("Test Lane, Nashik, Maharashtra");
        driver.findElement(By.id("description"))
              .sendKeys("This is a test outage report submitted via Selenium WebDriver. " +
                        "Affecting approximately 50 households.");
        driver.findElement(By.id("reporterName")).sendKeys("Selenium Test User");

        driver.findElement(By.id("submitReportBtn")).click();

        // Should redirect to detail page with success message
        wait.until(ExpectedConditions.urlMatches(".*/reports/\\d+"));
        assertThat(driver.getPageSource()).contains("submitted successfully");
    }

    // ── Test 5: Form Validation ───────────────────────────────────────────────

    @Test
    @Order(5)
    @DisplayName("Submitting empty form shows validation errors")
    void createReport_emptyForm_showsValidationErrors() {
        loginAs("user", "user123");

        driver.get(baseUrl + "/reports/new");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("createReportForm")));

        // Submit without filling anything
        driver.findElement(By.id("submitReportBtn")).click();

        // Page should reload (not redirect) with errors
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("titleError")));
        assertThat(driver.findElement(By.id("titleError")).isDisplayed()).isTrue();
    }

    // ── Test 6: Search ────────────────────────────────────────────────────────

    @Test
    @Order(6)
    @DisplayName("Search returns matching outage reports")
    void searchByKeyword_returnsMatching() {
        loginAs("user", "user123");

        driver.get(baseUrl + "/reports");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("searchInput")));

        driver.findElement(By.id("searchInput")).sendKeys("Selenium Test");
        driver.findElement(By.id("searchBtn")).click();

        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("reportsTable")));
        assertThat(driver.getPageSource()).contains("Selenium Test");
    }

    // ── Test 7: Unauthorized Status Update ────────────────────────────────────

    @Test
    @Order(7)
    @DisplayName("Regular user cannot see the status update form")
    void regularUser_cannotSeeStatusUpdateForm() {
        loginAs("user", "user123");

        // Navigate to a report detail page
        driver.get(baseUrl + "/reports");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("reportsTable")));

        // Click the first view link
        WebElement firstView = driver.findElements(By.cssSelector(".btn-view")).get(0);
        firstView.click();

        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("detail-location")));

        // The status update form should NOT be present for a regular USER
        assertThat(driver.findElements(By.id("statusUpdateForm"))).isEmpty();
        assertThat(driver.findElements(By.id("deleteReportForm"))).isEmpty();
    }

    // ── Test 8: Admin Status Update ───────────────────────────────────────────

    @Test
    @Order(8)
    @DisplayName("Admin can update report status")
    void admin_canUpdateReportStatus() {
        loginAs("admin", "admin123");

        driver.get(baseUrl + "/reports");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("reportsTable")));

        WebElement firstView = driver.findElements(By.cssSelector(".btn-view")).get(0);
        firstView.click();
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("statusUpdateForm")));

        // Select RESOLVED
        Select statusSelect = new Select(driver.findElement(By.id("newStatusSelect")));
        statusSelect.selectByValue("RESOLVED");

        driver.findElement(By.id("updateStatusBtn")).click();

        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".alert-success")));
        assertThat(driver.getPageSource()).contains("RESOLVED");
    }
}
