package framework.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

/**
 * Centralized logging management utility
 * Provides standardized logging across the framework
 */
public class LogManager {
    private static final Logger logger = LoggerFactory.getLogger(LogManager.class);

    /**
     * Set context for logging (useful for parallel execution)
     */
    public static void setTestContext(String scenarioName, String threadId) {
        MDC.put("scenario", scenarioName);
        MDC.put("thread", threadId);
    }

    /**
     * Clear logging context
     */
    public static void clearContext() {
        MDC.clear();
    }

    /**
     * Log API request details
     */
    public static void logRequest(String method, String endpoint, Object body) {
        logger.info("API Request - Method: {}, Endpoint: {}, Body: {}", method, endpoint, body);
    }

    /**
     * Log API response details
     */
    public static void logResponse(int statusCode, String responseBody) {
        logger.info("API Response - Status: {}, Body: {}", statusCode, responseBody);
    }

    /**
     * Log test step execution
     */
    public static void logTestStep(String stepDescription) {
        logger.info("Test Step: {}", stepDescription);
    }

    /**
     * Log error with context
     */
    public static void logError(String message, Throwable throwable) {
        logger.error("Error: {}", message, throwable);
    }

    /**
     * Log warning
     */
    public static void logWarning(String message) {
        logger.warn("Warning: {}", message);
    }

    /**
     * Log debug information
     */
    public static void logDebug(String message) {
        logger.debug("Debug: {}", message);
    }

    /**
     * Log test start
     */
    public static void logTestStart(String testName) {
        logger.info("========== Test Started: {} ==========", testName);
    }

    /**
     * Log test completion
     */
    public static void logTestEnd(String testName, String status) {
        logger.info("========== Test Completed: {} - Status: {} ==========", testName, status);
    }

    /**
     * Log scenario start
     */
    public static void logScenarioStart(String scenarioName) {
        logger.info("--- Scenario Started: {} ---", scenarioName);
    }

    /**
     * Log scenario completion
     */
    public static void logScenarioEnd(String scenarioName, String status) {
        logger.info("--- Scenario Completed: {} - Status: {} ---", scenarioName, status);
    }
}