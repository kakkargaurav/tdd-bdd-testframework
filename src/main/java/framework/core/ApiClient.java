package framework.core;

import framework.auth.AuthenticationManager;
import framework.config.ConfigManager;
import framework.reporting.ExtentReportManager;
import framework.utils.LogManager;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.util.Map;
import java.util.Set;
import java.util.HashSet;

/**
 * Central API client that wraps RestAssured functionality
 * Provides a unified interface for making HTTP requests with authentication
 */
public class ApiClient {
    private static final Logger logger = LoggerFactory.getLogger(ApiClient.class);
    private final ConfigManager configManager;
    private final AuthenticationManager authManager;
    private RequestSpecification requestSpec;
    private final ObjectMapper objectMapper;
    private final Set<String> sensitiveHeaders;

    public ApiClient() {
        this.configManager = ConfigManager.getInstance();
        this.authManager = new AuthenticationManager();
        this.objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        this.sensitiveHeaders = initializeSensitiveHeaders();
        initializeRestAssured();
    }

    /**
     * Initialize list of sensitive headers to exclude from logging
     */
    private Set<String> initializeSensitiveHeaders() {
        Set<String> headers = new HashSet<>();
        String excludeHeaders = configManager.getProperty("logging.exclude.sensitive.headers",
                "Authorization,X-API-Key,Cookie");
        String[] headerArray = excludeHeaders.split(",");
        for (String header : headerArray) {
            headers.add(header.trim().toLowerCase());
        }
        return headers;
    }

    /**
     * Initialize RestAssured with base configuration
     */
    private void initializeRestAssured() {
        RestAssured.baseURI = configManager.getBaseUrl();
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        
        requestSpec = RestAssured.given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON);
        
        logger.info("ApiClient initialized with base URL: {}", configManager.getBaseUrl());
    }

    /**
     * Get a new request specification with authentication if required
     */
    public RequestSpecification getRequestSpec() {
        RequestSpecification spec = RestAssured.given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON);
        
        // Add authentication if available
        if (authManager.isAuthenticated()) {
            spec = authManager.addAuthenticationToRequest(spec);
        }
        
        return spec;
    }

    /**
     * Perform GET request
     */
    public Response get(String endpoint) {
        logger.info("Performing GET request to: {}", endpoint);
        RequestSpecification spec = getRequestSpec();
        
        // Log request if enabled
        logRequest("GET", endpoint, null, spec);
        
        Response response = spec.get(endpoint);
        
        // Log response if enabled
        logResponse(response);
        
        return response;
    }

    /**
     * Perform GET request with path parameters
     */
    public Response get(String endpoint, Map<String, Object> pathParams) {
        logger.info("Performing GET request to: {} with path params: {}", endpoint, pathParams);
        return getRequestSpec()
                .pathParams(pathParams)
                .get(endpoint);
    }

    /**
     * Perform GET request with query parameters
     */
    public Response getWithQueryParams(String endpoint, Map<String, Object> queryParams) {
        logger.info("Performing GET request to: {} with query params: {}", endpoint, queryParams);
        return getRequestSpec()
                .queryParams(queryParams)
                .get(endpoint);
    }

    /**
     * Perform POST request with body
     */
    public Response post(String endpoint, Object body) {
        logger.info("Performing POST request to: {} with body: {}", endpoint, body);
        RequestSpecification spec = getRequestSpec().body(body);
        
        // Log request if enabled
        logRequest("POST", endpoint, body, spec);
        
        Response response = spec.post(endpoint);
        
        // Log response if enabled
        logResponse(response);
        
        return response;
    }

    /**
     * Perform POST request with body and path parameters
     */
    public Response post(String endpoint, Object body, Map<String, Object> pathParams) {
        logger.info("Performing POST request to: {} with body: {} and path params: {}", 
                   endpoint, body, pathParams);
        return getRequestSpec()
                .body(body)
                .pathParams(pathParams)
                .post(endpoint);
    }

    /**
     * Perform PUT request with body
     */
    public Response put(String endpoint, Object body) {
        logger.info("Performing PUT request to: {} with body: {}", endpoint, body);
        return getRequestSpec()
                .body(body)
                .put(endpoint);
    }

    /**
     * Perform PUT request with body and path parameters
     */
    public Response put(String endpoint, Object body, Map<String, Object> pathParams) {
        logger.info("Performing PUT request to: {} with body: {} and path params: {}", 
                   endpoint, body, pathParams);
        return getRequestSpec()
                .body(body)
                .pathParams(pathParams)
                .put(endpoint);
    }

    /**
     * Perform DELETE request
     */
    public Response delete(String endpoint) {
        logger.info("Performing DELETE request to: {}", endpoint);
        return getRequestSpec().delete(endpoint);
    }

    /**
     * Perform DELETE request with path parameters
     */
    public Response delete(String endpoint, Map<String, Object> pathParams) {
        logger.info("Performing DELETE request to: {} with path params: {}", endpoint, pathParams);
        return getRequestSpec()
                .pathParams(pathParams)
                .delete(endpoint);
    }

    /**
     * Perform PATCH request with body
     */
    public Response patch(String endpoint, Object body) {
        logger.info("Performing PATCH request to: {} with body: {}", endpoint, body);
        return getRequestSpec()
                .body(body)
                .patch(endpoint);
    }

    /**
     * Add custom headers to the request
     */
    public RequestSpecification addHeaders(Map<String, String> headers) {
        RequestSpecification spec = getRequestSpec();
        headers.forEach(spec::header);
        logger.debug("Added custom headers: {}", headers);
        return spec;
    }

    /**
     * Add custom header to the request
     */
    public RequestSpecification addHeader(String key, String value) {
        RequestSpecification spec = getRequestSpec();
        spec.header(key, value);
        logger.debug("Added custom header: {} = {}", key, value);
        return spec;
    }

    /**
     * Authenticate the client for subsequent requests
     */
    public void authenticate(String username, String password) {
        authManager.authenticate(username, password);
        logger.info("Authentication completed for user: {}", username);
    }

    /**
     * Authenticate using bearer token
     */
    public void authenticateWithToken(String token) {
        authManager.authenticateWithToken(token);
        logger.info("Authentication completed with bearer token");
    }

    /**
     * Clear authentication
     */
    public void clearAuthentication() {
        authManager.clearAuthentication();
        logger.info("Authentication cleared");
    }

    /**
     * Check if client is authenticated
     */
    public boolean isAuthenticated() {
        return authManager.isAuthenticated();
    }

    /**
     * Reset the client to initial state
     */
    public void reset() {
        clearAuthentication();
        initializeRestAssured();
        logger.info("ApiClient reset to initial state");
    }

    /**
     * Log request details to extent report if enabled
     */
    private void logRequest(String method, String endpoint, Object body, RequestSpecification spec) {
        boolean loggingEnabled = Boolean.parseBoolean(
                configManager.getProperty("logging.request.response.enabled", "false"));
        boolean extentEnabled = Boolean.parseBoolean(
                configManager.getProperty("logging.request.response.in.extent", "false"));
        
        if (!loggingEnabled || !extentEnabled) {
            return;
        }

        try {
            boolean logHeaders = Boolean.parseBoolean(
                    configManager.getProperty("logging.headers.enabled", "true"));
            boolean logBody = Boolean.parseBoolean(
                    configManager.getProperty("logging.request.body.enabled", "true"));
            boolean prettyPrint = Boolean.parseBoolean(
                    configManager.getProperty("logging.pretty.print.json", "true"));

            String fullUrl = configManager.getBaseUrl() + endpoint;
            String headersStr = logHeaders ? buildHeadersString() : null;
            String bodyStr = null;
            
            if (logBody && body != null) {
                bodyStr = formatJsonIfNeeded(body, prettyPrint);
                int maxLength = Integer.parseInt(
                        configManager.getProperty("logging.max.body.length", "10000"));
                if (bodyStr.length() > maxLength) {
                    bodyStr = bodyStr.substring(0, maxLength) + "\n... (truncated)";
                }
            }

            ExtentReportManager.logApiRequestFormatted(method, endpoint, fullUrl, headersStr, bodyStr);
            
        } catch (Exception e) {
            logger.warn("Failed to log request details", e);
        }
    }

    /**
     * Log response details to extent report if enabled
     */
    private void logResponse(Response response) {
        boolean loggingEnabled = Boolean.parseBoolean(
                configManager.getProperty("logging.request.response.enabled", "false"));
        boolean extentEnabled = Boolean.parseBoolean(
                configManager.getProperty("logging.request.response.in.extent", "false"));
        
        if (!loggingEnabled || !extentEnabled) {
            return;
        }

        try {
            boolean logHeaders = Boolean.parseBoolean(
                    configManager.getProperty("logging.headers.enabled", "true"));
            boolean logBody = Boolean.parseBoolean(
                    configManager.getProperty("logging.response.body.enabled", "true"));
            boolean prettyPrint = Boolean.parseBoolean(
                    configManager.getProperty("logging.pretty.print.json", "true"));

            String headersStr = null;
            if (logHeaders) {
                StringBuilder headerBuilder = new StringBuilder();
                response.getHeaders().forEach(header -> {
                    String headerName = header.getName().toLowerCase();
                    if (!sensitiveHeaders.contains(headerName)) {
                        headerBuilder.append(header.getName()).append(": ").append(header.getValue()).append("\n");
                    } else {
                        headerBuilder.append(header.getName()).append(": [MASKED]\n");
                    }
                });
                headersStr = headerBuilder.toString();
            }

            String bodyStr = null;
            if (logBody) {
                bodyStr = response.getBody().asString();
                if (bodyStr != null && !bodyStr.trim().isEmpty()) {
                    bodyStr = formatJsonIfNeeded(bodyStr, prettyPrint);
                    int maxLength = Integer.parseInt(
                            configManager.getProperty("logging.max.body.length", "10000"));
                    if (bodyStr.length() > maxLength) {
                        bodyStr = bodyStr.substring(0, maxLength) + "\n... (truncated)";
                    }
                }
            }

            ExtentReportManager.logApiResponseFormatted(
                    response.getStatusCode(),
                    response.getStatusLine(),
                    response.getTime(),
                    headersStr,
                    bodyStr);
            
        } catch (Exception e) {
            logger.warn("Failed to log response details", e);
        }
    }

    /**
     * Build headers string for logging
     */
    private String buildHeadersString() {
        StringBuilder headers = new StringBuilder();
        if (authManager.isAuthenticated()) {
            headers.append("Authorization: [MASKED]\n");
        }
        headers.append("Content-Type: application/json\n");
        headers.append("Accept: application/json\n");
        return headers.toString();
    }

    /**
     * Format JSON string for better readability if needed
     */
    private String formatJsonIfNeeded(Object obj, boolean prettyPrint) {
        if (!prettyPrint) {
            return obj.toString();
        }

        try {
            if (obj instanceof String) {
                String str = (String) obj;
                if (str.trim().startsWith("{") || str.trim().startsWith("[")) {
                    // It's likely JSON, try to pretty print it
                    Object jsonObj = objectMapper.readValue(str, Object.class);
                    return objectMapper.writeValueAsString(jsonObj);
                }
                return str;
            } else {
                // Convert object to pretty JSON
                return objectMapper.writeValueAsString(obj);
            }
        } catch (Exception e) {
            // If JSON parsing fails, return as string
            return obj.toString();
        }
    }
}