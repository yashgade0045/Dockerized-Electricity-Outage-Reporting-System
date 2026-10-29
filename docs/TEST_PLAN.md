# Test Plan
## Electricity Outage Reporting System
**Version:** 1.0 | **Date:** October 2026

---

## 1. Testing Strategy

| Level | Tool | Scope |
|---|---|---|
| Unit Tests | JUnit 5 + Mockito | Service layer in isolation |
| Integration Tests | Spring Boot Test + MockMvc | Controllers + services + H2 DB |
| Browser Tests | Selenium WebDriver + Chrome | Full user journeys in a real browser |

---

## 2. Test Execution Commands

```bash
# Run all unit and integration tests
.\mvnw.cmd clean verify

# Run only unit tests (fast)
.\mvnw.cmd test

# Run integration tests
.\mvnw.cmd verify

# Run Selenium browser tests (requires Chrome)
.\mvnw.cmd verify -Pselenium

# Generate Surefire HTML report
.\mvnw.cmd surefire-report:report
# Report: target/site/surefire-report.html
```

---

## 3. Unit Test Cases — OutageReportServiceTest

| TC-ID | Test Method | What is Tested |
|---|---|---|
| UT-01 | `createReport_validDto_savesAndReturns` | Report is persisted and returned |
| UT-02 | `createReport_alwaysSetsReportedStatus` | Status override is ignored; always REPORTED |
| UT-03 | `findByIdOrThrow_existingId_returnsReport` | Report returned when found |
| UT-04 | `findByIdOrThrow_missingId_throwsException` | Exception thrown with ID in message |
| UT-05 | `findReports_noFilter_returnsAll` | All reports returned when no filter |
| UT-06 | `findReports_withKeyword_callsKeywordSearch` | Keyword search delegates correctly |
| UT-07 | `findReports_withStatus_callsStatusFilter` | Status filter delegates correctly |
| UT-08 | `updateReport_validDto_updatesFields` | Editable fields saved correctly |
| UT-09 | `updateStatus_validTransition_savesNewStatus` | Status change persisted |
| UT-10 | `deleteReport_existingId_deletesReport` | Delete called on repository |
| UT-11 | `deleteReport_missingId_throwsException` | Exception thrown when deleting missing report |
| UT-12 | `getDashboardStats_returnsLiveCounts` | Dashboard counts match repository values |

---

## 4. Integration Test Cases — OutageReportControllerTest

| TC-ID | Test Method | What is Tested |
|---|---|---|
| IT-01 | `unauthenticatedRequest_redirectsToLogin` | Security: redirect for anonymous user |
| IT-02 | `listReports_authenticated_returns200` | List page loads for USER role |
| IT-03 | `searchByKeyword_returnsMatchingReports` | Keyword search parameter passed to model |
| IT-04 | `filterByStatus_returnsFilteredReports` | Status filter parameter passed to model |
| IT-05 | `showCreateForm_returns200` | Create form loads |
| IT-06 | `createReport_validData_createsAndRedirects` | Valid form → DB record created → redirect |
| IT-07 | `createReport_blankTitle_showsValidationError` | Invalid form → re-render with errors |
| IT-08 | `viewReport_existingId_returns200` | Detail page shows report content |
| IT-09 | `viewReport_nonExistentId_returns404` | 404 for unknown ID |
| IT-10 | `editReport_asUser_returns403` | USER cannot access edit endpoint |
| IT-11 | `editReport_asAdmin_returns200` | ADMIN can access edit endpoint |
| IT-12 | `updateStatus_asUser_returns403` | USER cannot change status |
| IT-13 | `updateStatus_asAdmin_updatesAndRedirects` | ADMIN status change persisted to DB |
| IT-14 | `deleteReport_asUser_returns403` | USER cannot delete |
| IT-15 | `deleteReport_asAdmin_deletesAndRedirects` | ADMIN delete removes record from DB |
| IT-16 | `dashboard_authenticated_showsStats` | Dashboard page loads with stats attribute |

---

## 5. Selenium Test Cases — OutageReportingSeleniumTest

| TC-ID | Test Method | What is Tested |
|---|---|---|
| ST-01 | `loginPageLoads` | Login form renders with correct elements |
| ST-02 | `unauthenticatedAccessRedirectsToLogin` | Browser redirected to login |
| ST-03 | `dashboardShowsStatCards` | Dashboard cards visible after login |
| ST-04 | `createOutageReport_success` | Full form submit → success message in browser |
| ST-05 | `createReport_emptyForm_showsValidationErrors` | Validation errors displayed in browser |
| ST-06 | `searchByKeyword_returnsMatching` | Search produces matching results |
| ST-07 | `regularUser_cannotSeeStatusUpdateForm` | Status form absent for USER role |
| ST-08 | `admin_canUpdateReportStatus` | Admin can change status; success message shown |

---

## 6. Test Coverage Areas

| Area | Covered By |
|---|---|
| Report creation | UT-01, UT-02, IT-06, IT-07, ST-04, ST-05 |
| Report validation | IT-07, ST-05 |
| Report retrieval | UT-03–07, IT-02, IT-08 |
| Report update | UT-08, IT-11, IT-13 |
| Status update | UT-09, IT-12, IT-13, ST-08 |
| Delete | UT-10, UT-11, IT-14, IT-15 |
| Search/filter | UT-06, UT-07, IT-03, IT-04, ST-06 |
| Dashboard counts | UT-12, IT-16, ST-03 |
| Role-based auth | IT-10–15, ST-07, ST-08 |
| Invalid requests | UT-04, IT-09 |
| Unauthenticated access | IT-01, ST-02 |

---

## 7. Test Environment

- **Unit/Integration tests:** Java 24, H2 in-memory (profile: `test`), Spring Boot Test
- **Selenium tests:** Google Chrome (headless), ChromeDriver via WebDriverManager, Spring Boot on random port
- **CI environment:** Jenkins agent with Java + Chrome installed

---

## 8. Known Test Limitations

- Selenium tests require Google Chrome to be installed. They are excluded from the default Maven build.
- Integration tests use an in-memory H2 database — not identical to a production database engine. Schema compatibility should be verified if switching to PostgreSQL/MySQL.
- Performance tests are not included in the current scope.
