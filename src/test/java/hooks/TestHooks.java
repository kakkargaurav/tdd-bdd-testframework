package hooks;

import framework.core.TestContext;
import framework.reporting.ExtentReportManager;
import framework.utils.LogManager;
import io.cucumber.java.After;
import io.cucumber.java.AfterAll;
import io.cucumber.java.Before;
import io.cucumber.java.BeforeAll;
import io.cucumber.java.Scenario;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Cucumber hooks for test setup and teardown operations
 */
public class TestHooks {
    private static final Logger logger = LoggerFactory.getLogger(TestHooks.class);

    @BeforeAll
    public static void beforeAllTests() {
        logger.info("========== Test Suite Starting ==========");
        ExtentReportManager.initializeReports();
        LogManager.logTestStart("Test Suite");
    }

    @Before
    public void beforeScenario(Scenario scenario) {
        String scenarioName = scenario.getName();
        String threadId = Thread.currentThread().getName();
        
        LogManager.setTestContext(scenarioName, threadId);
        LogManager.logScenarioStart(scenarioName);
        
        TestContext.clear();
        TestContext.setScenarioName(scenarioName);
        
        ExtentReportManager.createTest(scenarioName, getScenarioDescription(scenario));
        
        logger.info("Scenario started: {} on thread: {}", scenarioName, threadId);
    }

    @After
    public void afterScenario(Scenario scenario) {
        String scenarioName = scenario.getName();
        String status = scenario.getStatus().toString();
        
        LogManager.logScenarioEnd(scenarioName, status);
        
        // Attach response details to report if available
        if (TestContext.hasResponse()) {
            try {
                Response response = TestContext.getResponse();
                String responseDetails = formatResponseDetails(response);
                ExtentReportManager.logInfo("Response Details:\n" + responseDetails);
            } catch (Exception e) {
                logger.warn("Failed to attach response details to report", e);
            }
        }
        
        // Handle scenario failure
        if (scenario.isFailed()) {
            handleScenarioFailure(scenario);
        } else {
            ExtentReportManager.logPass("Scenario passed: " + scenarioName);
        }
        
        // Cleanup
        TestContext.cleanup();
        LogManager.clearContext();
        
        logger.info("Scenario completed: {} with status: {}", scenarioName, status);
    }

    @AfterAll
    public static void afterAllTests() {
        LogManager.logTestEnd("Test Suite", "COMPLETED");
        ExtentReportManager.flushReports();
        logger.info("========== Test Suite Completed ==========");
    }

    /**
     * Handle scenario failure by logging details and attaching to report
     */
    private void handleScenarioFailure(Scenario scenario) {
        String errorMessage = "Scenario failed: " + scenario.getName();
        
        // Log failure details
        LogManager.logError(errorMessage, null);
        ExtentReportManager.logFail(errorMessage);
        
        // Attach response details if available
        if (TestContext.hasResponse()) {
            try {
                Response response = TestContext.getResponse();
                String failureDetails = formatFailureDetails(response);
                ExtentReportManager.logFail("Failure Details:\n" + failureDetails);
            } catch (Exception e) {
                logger.warn("Failed to attach failure details", e);
            }
        }
        
        // Attach scenario context information
        String contextInfo = formatContextInfo();
        ExtentReportManager.logInfo("Context Information:\n" + contextInfo);
    }

    /**
     * Format response details for reporting
     */
    private String formatResponseDetails(Response response) {
        StringBuilder details = new StringBuilder();
        details.append("Status Code: ").append(response.getStatusCode()).append("\n");
        details.append("Response Time: ").append(response.getTime()).append("ms\n");
        details.append("Content Type: ").append(response.getContentType()).append("\n");
        
        // Add headers
        details.append("Headers:\n");
        response.getHeaders().forEach(header -> 
            details.append("  ").append(header.getName()).append(": ").append(header.getValue()).append("\n"));
        
        // Add response body (truncate if too long)
        String responseBody = response.getBody().asString();
        if (responseBody.length() > 2000) {
            responseBody = responseBody.substring(0, 2000) + "... (truncated)";
        }
        details.append("Response Body:\n").append(responseBody);
        
        return details.toString();
    }

    /**
     * Format failure details for reporting
     */
    private String formatFailureDetails(Response response) {
        StringBuilder details = new StringBuilder();
        details.append("Failed Request Details:\n");
        details.append("Status Code: ").append(response.getStatusCode()).append("\n");
        details.append("Response Time: ").append(response.getTime()).append("ms\n");
        
        String endpoint = TestContext.getEndpoint();
        if (endpoint != null) {
            details.append("Endpoint: ").append(endpoint).append("\n");
        }
        
        Object requestBody = TestContext.getRequestBody();
        if (requestBody != null) {
            details.append("Request Body: ").append(requestBody.toString()).append("\n");
        }
        
        details.append("Response Body: ").append(response.getBody().asString());
        
        return details.toString();
    }

    /**
     * Format context information for reporting
     */
    private String formatContextInfo() {
        StringBuilder info = new StringBuilder();
        info.append("Thread: ").append(Thread.currentThread().getName()).append("\n");
        info.append("Scenario: ").append(TestContext.getScenarioName()).append("\n");
        
        String userId = TestContext.getUserId();
        if (userId != null) {
            info.append("User ID: ").append(userId).append("\n");
        }
        
        String endpoint = TestContext.getEndpoint();
        if (endpoint != null) {
            info.append("Last Endpoint: ").append(endpoint).append("\n");
        }
        
        return info.toString();
    }

    /**
     * Get scenario description from tags and other metadata
     */
    private String getScenarioDescription(Scenario scenario) {
        StringBuilder description = new StringBuilder();
        
        if (!scenario.getSourceTagNames().isEmpty()) {
            description.append("Tags: ");
            scenario.getSourceTagNames().forEach(tag -> description.append(tag).append(" "));
            description.append("\n");
        }
        
        description.append("URI: ").append(scenario.getUri()).append("\n");
        description.append("Line: ").append(scenario.getLine());
        
        return description.toString();
    }
}