package framework.reporting;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
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
}