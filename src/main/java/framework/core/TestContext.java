package framework.core;

import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe test context for sharing data between test steps
 * Manages test execution state and data across scenarios
 */
public class TestContext {
    private static final Logger logger = LoggerFactory.getLogger(TestContext.class);
    
    // Thread-safe storage for parallel execution
    private static final ThreadLocal<Map<String, Object>> contextStorage = 
            ThreadLocal.withInitial(ConcurrentHashMap::new);
    
    // Special keys for common test artifacts
    public static final String RESPONSE_KEY = "last_response";
    public static final String REQUEST_BODY_KEY = "last_request_body";
    public static final String ENDPOINT_KEY = "last_endpoint";
    public static final String USER_ID_KEY = "user_id";
    public static final String AUTH_TOKEN_KEY = "auth_token";
    public static final String TEST_DATA_KEY = "test_data";
    public static final String SCENARIO_NAME_KEY = "scenario_name";

    /**
     * Store a value in the context
     */
    public static void set(String key, Object value) {
        contextStorage.get().put(key, value);
        logger.debug("Context value set: {} = {}", key, value);
    }

    /**
     * Retrieve a value from the context
     */
    @SuppressWarnings("unchecked")
    public static <T> T get(String key) {
        Object value = contextStorage.get().get(key);
        logger.debug("Context value retrieved: {} = {}", key, value);
        return (T) value;
    }

    /**
     * Retrieve a value with default fallback
     */
    @SuppressWarnings("unchecked")
    public static <T> T get(String key, T defaultValue) {
        Object value = contextStorage.get().getOrDefault(key, defaultValue);
        logger.debug("Context value retrieved with default: {} = {}", key, value);
        return (T) value;
    }

    /**
     * Check if a key exists in the context
     */
    public static boolean has(String key) {
        boolean exists = contextStorage.get().containsKey(key);
        logger.debug("Context key exists check: {} = {}", key, exists);
        return exists;
    }

    /**
     * Remove a value from the context
     */
    public static void remove(String key) {
        Object removed = contextStorage.get().remove(key);
        logger.debug("Context value removed: {} = {}", key, removed);
    }

    /**
     * Clear all context data for current thread
     */
    public static void clear() {
        contextStorage.get().clear();
        logger.debug("Context cleared for current thread");
    }

    /**
     * Get all context data (for debugging)
     */
    public static Map<String, Object> getAll() {
        return new HashMap<>(contextStorage.get());
    }

    /**
     * Store the last API response
     */
    public static void setResponse(Response response) {
        set(RESPONSE_KEY, response);
        logger.info("Response stored in context - Status: {}", response.getStatusCode());
    }

    /**
     * Get the last API response
     */
    public static Response getResponse() {
        return get(RESPONSE_KEY);
    }

    /**
     * Store the last request body
     */
    public static void setRequestBody(Object requestBody) {
        set(REQUEST_BODY_KEY, requestBody);
        logger.debug("Request body stored in context");
    }

    /**
     * Get the last request body
     */
    public static Object getRequestBody() {
        return get(REQUEST_BODY_KEY);
    }

    /**
     * Store the last endpoint used
     */
    public static void setEndpoint(String endpoint) {
        set(ENDPOINT_KEY, endpoint);
        logger.debug("Endpoint stored in context: {}", endpoint);
    }

    /**
     * Get the last endpoint used
     */
    public static String getEndpoint() {
        return get(ENDPOINT_KEY);
    }

    /**
     * Store user ID for test scenarios
     */
    public static void setUserId(String userId) {
        set(USER_ID_KEY, userId);
        logger.debug("User ID stored in context: {}", userId);
    }

    /**
     * Get user ID from context
     */
    public static String getUserId() {
        return get(USER_ID_KEY);
    }

    /**
     * Store authentication token
     */
    public static void setAuthToken(String token) {
        set(AUTH_TOKEN_KEY, token);
        logger.debug("Auth token stored in context");
    }

    /**
     * Get authentication token
     */
    public static String getAuthToken() {
        return get(AUTH_TOKEN_KEY);
    }

    /**
     * Store test data object
     */
    public static void setTestData(Object testData) {
        set(TEST_DATA_KEY, testData);
        logger.debug("Test data stored in context");
    }

    /**
     * Get test data object
     */
    public static <T> T getTestData() {
        return get(TEST_DATA_KEY);
    }

    /**
     * Store scenario name
     */
    public static void setScenarioName(String scenarioName) {
        set(SCENARIO_NAME_KEY, scenarioName);
        logger.debug("Scenario name stored in context: {}", scenarioName);
    }

    /**
     * Get scenario name
     */
    public static String getScenarioName() {
        return get(SCENARIO_NAME_KEY);
    }

    /**
     * Store a custom object with type safety
     */
    public static <T> void setTyped(String key, T value, Class<T> type) {
        set(key, value);
        set(key + "_type", type);
        logger.debug("Typed value stored: {} of type {}", key, type.getSimpleName());
    }

    /**
     * Retrieve a custom object with type safety
     */
    @SuppressWarnings("unchecked")
    public static <T> T getTyped(String key, Class<T> type) {
        Object value = get(key);
        Class<?> storedType = get(key + "_type");
        
        if (value != null && storedType != null && type.isAssignableFrom(storedType)) {
            return (T) value;
        }
        
        logger.warn("Type mismatch or null value for key: {}, expected: {}, actual: {}", 
                   key, type.getSimpleName(), storedType != null ? storedType.getSimpleName() : "null");
        return null;
    }

    /**
     * Get response validator for the last response
     */
    public static ResponseValidator getResponseValidator() {
        Response response = getResponse();
        if (response != null) {
            return new ResponseValidator(response);
        }
        throw new IllegalStateException("No response found in context. Execute an API request first.");
    }

    /**
     * Check if response exists in context
     */
    public static boolean hasResponse() {
        return has(RESPONSE_KEY) && getResponse() != null;
    }

    /**
     * Get response status code from last response
     */
    public static int getResponseStatusCode() {
        Response response = getResponse();
        if (response != null) {
            return response.getStatusCode();
        }
        throw new IllegalStateException("No response found in context");
    }

    /**
     * Get response body as string from last response
     */
    public static String getResponseBodyAsString() {
        Response response = getResponse();
        if (response != null) {
            return response.getBody().asString();
        }
        throw new IllegalStateException("No response found in context");
    }

    /**
     * Extract value from last response using JSON path
     */
    public static <T> T extractFromResponse(String jsonPath) {
        Response response = getResponse();
        if (response != null) {
            return response.jsonPath().get(jsonPath);
        }
        throw new IllegalStateException("No response found in context");
    }

    /**
     * Cleanup method to be called after each scenario
     */
    public static void cleanup() {
        logger.debug("Cleaning up context for thread: {}", Thread.currentThread().getName());
        contextStorage.remove();
    }
}