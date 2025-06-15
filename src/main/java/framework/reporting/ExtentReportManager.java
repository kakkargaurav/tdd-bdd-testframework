package framework.reporting;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.markuputils.MarkupHelper;
import com.aventstack.extentreports.markuputils.CodeLanguage;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import framework.config.ConfigManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Manages Extent Reports for comprehensive test reporting
 */
public class ExtentReportManager {
    private static final Logger logger = LoggerFactory.getLogger(ExtentReportManager.class);
    private static ExtentReports extentReports;
    private static final ThreadLocal<ExtentTest> extentTest = new ThreadLocal<>();
    private static final String REPORTS_PATH = "target/extent-reports/";
    private static final String REPORT_NAME = "API-Test-Report";

    /**
     * Initialize Extent Reports
     */
    public static synchronized void initializeReports() {
        if (extentReports == null) {
            createReportDirectory();
            
            String reportPath = REPORTS_PATH + REPORT_NAME + "-" + getCurrentTimestamp() + ".html";
            ExtentSparkReporter sparkReporter = new ExtentSparkReporter(reportPath);
            
            configureSparkReporter(sparkReporter);
            
            extentReports = new ExtentReports();
            extentReports.attachReporter(sparkReporter);
            setSystemInformation();
            
            logger.info("Extent Reports initialized at: {}", reportPath);
        }
    }

    /**
     * Configure Spark Reporter settings
     */
    private static void configureSparkReporter(ExtentSparkReporter sparkReporter) {
        sparkReporter.config().setTheme(Theme.STANDARD);
        sparkReporter.config().setDocumentTitle("API Test Automation Report");
        sparkReporter.config().setReportName("API Test Execution Report");
        sparkReporter.config().setTimelineEnabled(true);
        sparkReporter.config().setEncoding("utf-8");
        
        // Custom CSS for better styling
        sparkReporter.config().setCss(
                ".badge-primary { background-color: #007bff; } " +
                ".badge-success { background-color: #28a745; } " +
                ".badge-danger { background-color: #dc3545; } " +
                ".badge-warning { background-color: #ffc107; color: #212529; }"
        );
    }

    /**
     * Set system information in the report
     */
    private static void setSystemInformation() {
        ConfigManager configManager = ConfigManager.getInstance();
        
        extentReports.setSystemInfo("Environment", configManager.getCurrentEnvironment().getName());
        extentReports.setSystemInfo("Base URL", configManager.getBaseUrl());
        extentReports.setSystemInfo("Auth Type", configManager.getAuthType());
        extentReports.setSystemInfo("Java Version", System.getProperty("java.version"));
        extentReports.setSystemInfo("OS", System.getProperty("os.name"));
        extentReports.setSystemInfo("User", System.getProperty("user.name"));
        extentReports.setSystemInfo("Test Framework", "Cucumber + RestAssured + TestNG");
        extentReports.setSystemInfo("Report Generated", getCurrentTimestamp());
    }

    /**
     * Create a new test in the report
     */
    public static void createTest(String testName, String description) {
        ExtentTest test = extentReports.createTest(testName, description);
        extentTest.set(test);
        logger.debug("Test created in Extent Report: {}", testName);
    }

    /**
     * Log info message
     */
    public static void logInfo(String message) {
        if (extentTest.get() != null) {
            extentTest.get().log(Status.INFO, message);
        }
    }

    /**
     * Log pass message
     */
    public static void logPass(String message) {
        if (extentTest.get() != null) {
            extentTest.get().log(Status.PASS, message);
        }
    }

    /**
     * Log fail message
     */
    public static void logFail(String message) {
        if (extentTest.get() != null) {
            extentTest.get().log(Status.FAIL, message);
        }
    }

    /**
     * Log warning message
     */
    public static void logWarning(String message) {
        if (extentTest.get() != null) {
            extentTest.get().log(Status.WARNING, message);
        }
    }

    /**
     * Log skip message
     */
    public static void logSkip(String message) {
        if (extentTest.get() != null) {
            extentTest.get().log(Status.SKIP, message);
        }
    }

    /**
     * Add screenshot to report
     */
    public static void addScreenshot(String screenshotPath) {
        if (extentTest.get() != null) {
            try {
                extentTest.get().addScreenCaptureFromPath(screenshotPath);
            } catch (Exception e) {
                logger.error("Failed to add screenshot to report", e);
            }
        }
    }

    /**
     * Assign category to test
     */
    public static void assignCategory(String... categories) {
        if (extentTest.get() != null) {
            extentTest.get().assignCategory(categories);
        }
    }

    /**
     * Assign author to test
     */
    public static void assignAuthor(String... authors) {
        if (extentTest.get() != null) {
            extentTest.get().assignAuthor(authors);
        }
    }

    /**
     * Add device information
     */
    public static void assignDevice(String deviceName) {
        if (extentTest.get() != null) {
            extentTest.get().assignDevice(deviceName);
        }
    }

    /**
     * Log API request details
     */
    public static void logApiRequest(String method, String endpoint, String requestBody) {
        if (extentTest.get() != null) {
            String requestDetails = String.format(
                    "<b>API Request:</b><br/>" +
                    "<b>Method:</b> %s<br/>" +
                    "<b>Endpoint:</b> %s<br/>" +
                    "<b>Request Body:</b><br/>" +
                    "<pre>%s</pre>",
                    method, endpoint, requestBody != null ? requestBody : "No body"
            );
            extentTest.get().log(Status.INFO, requestDetails);
        }
    }

    /**
     * Log API response details
     */
    public static void logApiResponse(int statusCode, String responseBody, long responseTime) {
        if (extentTest.get() != null) {
            Status status = statusCode >= 200 && statusCode < 300 ? Status.PASS : Status.FAIL;
            
            String responseDetails = String.format(
                    "<b>API Response:</b><br/>" +
                    "<b>Status Code:</b> %d<br/>" +
                    "<b>Response Time:</b> %d ms<br/>" +
                    "<b>Response Body:</b><br/>" +
                    "<pre>%s</pre>",
                    statusCode, responseTime, 
                    responseBody != null && responseBody.length() > 1000 ? 
                        responseBody.substring(0, 1000) + "... (truncated)" : responseBody
            );
            extentTest.get().log(status, responseDetails);
        }
    }

    /**
     * Create a child test (for step-wise reporting)
     */
    public static ExtentTest createChild(String childTestName) {
        if (extentTest.get() != null) {
            return extentTest.get().createNode(childTestName);
        }
        return null;
    }

    /**
     * Flush reports (write to file)
     */
    public static synchronized void flushReports() {
        if (extentReports != null) {
            extentReports.flush();
            logger.info("Extent Reports flushed successfully");
        }
    }

    /**
     * Get current extent test
     */
    public static ExtentTest getCurrentTest() {
        return extentTest.get();
    }

    /**
     * Remove current test from thread local
     */
    public static void removeTest() {
        extentTest.remove();
    }

    /**
     * Create reports directory if it doesn't exist
     */
    private static void createReportDirectory() {
        File directory = new File(REPORTS_PATH);
        if (!directory.exists()) {
            boolean created = directory.mkdirs();
            if (created) {
                logger.info("Reports directory created: {}", REPORTS_PATH);
            } else {
                logger.error("Failed to create reports directory: {}", REPORTS_PATH);
            }
        }
    }

    /**
     * Get current timestamp for report naming
     */
    private static String getCurrentTimestamp() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
    }

    /**
     * Add environment details to test
     */
    public static void addEnvironmentDetails() {
        if (extentTest.get() != null) {
            ConfigManager configManager = ConfigManager.getInstance();
            String envDetails = String.format(
                    "<b>Environment Details:</b><br/>" +
                    "<b>Environment:</b> %s<br/>" +
                    "<b>Base URL:</b> %s<br/>" +
                    "<b>Auth Type:</b> %s",
                    configManager.getCurrentEnvironment().getName(),
                    configManager.getBaseUrl(),
                    configManager.getAuthType()
            );
            extentTest.get().log(Status.INFO, envDetails);
        }
    }

    /**
     * Log test data used
     */
    public static void logTestData(String testDataName, Object testData) {
        if (extentTest.get() != null) {
            String dataDetails = String.format(
                    "<b>Test Data Used:</b><br/>" +
                    "<b>Data Set:</b> %s<br/>" +
                    "<b>Data:</b><br/>" +
                    "<pre>%s</pre>",
                    testDataName, testData.toString()
            );
            extentTest.get().log(Status.INFO, dataDetails);
        }
    }

    /**
     * Log API request with proper code formatting
     */
    public static void logApiRequestFormatted(String method, String endpoint, String fullUrl,
                                             String headers, String requestBody) {
        if (extentTest.get() != null) {
            StringBuilder requestLog = new StringBuilder();
            requestLog.append("<h4 style='color: #007bff; margin: 10px 0;'>🔵 API Request</h4>");
            requestLog.append("<div style='background-color: #f8f9fa; padding: 15px; border-radius: 5px; border-left: 4px solid #007bff; margin: 10px 0;'>");
            requestLog.append(String.format("<strong>Method:</strong> <span style='color: #007bff; font-weight: bold;'>%s</span><br/>", method));
            requestLog.append(String.format("<strong>Endpoint:</strong> %s<br/>", endpoint));
            requestLog.append(String.format("<strong>Full URL:</strong> %s<br/>", fullUrl));
            
            if (headers != null && !headers.trim().isEmpty()) {
                requestLog.append("<br/><strong>Headers:</strong><br/>");
                requestLog.append("<div style='background-color: #e9ecef; padding: 10px; border-radius: 3px; font-family: monospace; white-space: pre-wrap;'>");
                requestLog.append(headers);
                requestLog.append("</div>");
            }
            
            if (requestBody != null && !requestBody.trim().isEmpty()) {
                requestLog.append("<br/><strong>Request Body:</strong><br/>");
                requestLog.append("</div>");
                
                // Log the request body as a separate code block for better formatting
                extentTest.get().log(Status.INFO, requestLog.toString());
                extentTest.get().log(Status.INFO, MarkupHelper.createCodeBlock(requestBody, CodeLanguage.JSON));
            } else {
                requestLog.append("</div>");
                extentTest.get().log(Status.INFO, requestLog.toString());
            }
        }
    }

    /**
     * Log API response with proper code formatting
     */
    public static void logApiResponseFormatted(int statusCode, String statusLine, long responseTime,
                                              String headers, String responseBody) {
        if (extentTest.get() != null) {
            Status logStatus = statusCode >= 200 && statusCode < 300 ? Status.PASS : Status.FAIL;
            String statusColor = statusCode >= 200 && statusCode < 300 ? "#28a745" : "#dc3545";
            
            StringBuilder responseLog = new StringBuilder();
            responseLog.append("<h4 style='color: ").append(statusColor).append("; margin: 10px 0;'>🔴 API Response</h4>");
            responseLog.append("<div style='background-color: #f8f9fa; padding: 15px; border-radius: 5px; border-left: 4px solid ").append(statusColor).append("; margin: 10px 0;'>");
            responseLog.append(String.format("<strong>Status Code:</strong> <span style='color: %s; font-weight: bold;'>%d</span><br/>", statusColor, statusCode));
            responseLog.append(String.format("<strong>Status Line:</strong> %s<br/>", statusLine));
            responseLog.append(String.format("<strong>Response Time:</strong> <span style='color: #6c757d;'>%d ms</span><br/>", responseTime));
            
            if (headers != null && !headers.trim().isEmpty()) {
                responseLog.append("<br/><strong>Response Headers:</strong><br/>");
                responseLog.append("<div style='background-color: #e9ecef; padding: 10px; border-radius: 3px; font-family: monospace; white-space: pre-wrap;'>");
                responseLog.append(headers);
                responseLog.append("</div>");
            }
            
            if (responseBody != null && !responseBody.trim().isEmpty()) {
                responseLog.append("<br/><strong>Response Body:</strong><br/>");
                responseLog.append("</div>");
                
                // Log the response info first
                extentTest.get().log(logStatus, responseLog.toString());
                
                // Then log the response body as a separate formatted code block
                extentTest.get().log(logStatus, MarkupHelper.createCodeBlock(responseBody, CodeLanguage.JSON));
            } else {
                responseLog.append("<br/><em>Empty response body</em>");
                responseLog.append("</div>");
                extentTest.get().log(logStatus, responseLog.toString());
            }
        }
    }

    /**
     * Log code block with syntax highlighting
     */
    public static void logCodeBlock(String code, CodeLanguage language, Status status) {
        if (extentTest.get() != null) {
            extentTest.get().log(status, MarkupHelper.createCodeBlock(code, language));
        }
    }

    /**
     * Log JSON code block with syntax highlighting
     */
    public static void logJsonCodeBlock(String json, Status status) {
        logCodeBlock(json, CodeLanguage.JSON, status);
    }

    /**
     * Log info message with HTML markup support
     */
    public static void logInfoMarkup(String htmlMessage) {
        if (extentTest.get() != null) {
            extentTest.get().log(Status.INFO, MarkupHelper.createLabel(htmlMessage, com.aventstack.extentreports.markuputils.ExtentColor.BLUE));
        }
    }
}