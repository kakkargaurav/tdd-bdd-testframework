package framework.core;

import framework.auth.AuthenticationManager;
import framework.config.ConfigManager;
import framework.utils.LogManager;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Central API client that wraps RestAssured functionality
 * Provides a unified interface for making HTTP requests with authentication
 */
public class ApiClient {
    private static final Logger logger = LoggerFactory.getLogger(ApiClient.class);
    private final ConfigManager configManager;
    private final AuthenticationManager authManager;
    private RequestSpecification requestSpec;

    public ApiClient() {
        this.configManager = ConfigManager.getInstance();
        this.authManager = new AuthenticationManager();
        initializeRestAssured();
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
        return getRequestSpec().get(endpoint);
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
        return getRequestSpec()
                .body(body)
                .post(endpoint);
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
}