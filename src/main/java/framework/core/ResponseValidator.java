package framework.core;

import io.restassured.module.jsv.JsonSchemaValidator;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Comprehensive response validation utilities
 * Provides various methods to validate API responses
 */
public class ResponseValidator {
    private static final Logger logger = LoggerFactory.getLogger(ResponseValidator.class);
    private final Response response;

    public ResponseValidator(Response response) {
        this.response = response;
    }

    /**
     * Validate status code
     */
    public ResponseValidator statusCode(int expectedStatusCode) {
        int actualStatusCode = response.getStatusCode();
        assertThat(actualStatusCode)
                .as("Status code validation failed")
                .isEqualTo(expectedStatusCode);
        
        logger.info("Status code validation passed: {}", actualStatusCode);
        return this;
    }

    /**
     * Validate status code is in success range (200-299)
     */
    public ResponseValidator statusCodeSuccess() {
        int actualStatusCode = response.getStatusCode();
        assertThat(actualStatusCode)
                .as("Status code should be in success range (200-299)")
                .isBetween(200, 299);
        
        logger.info("Success status code validation passed: {}", actualStatusCode);
        return this;
    }

    /**
     * Validate response time is within expected range
     */
    public ResponseValidator responseTime(long maxTimeInMs) {
        long actualTime = response.getTime();
        assertThat(actualTime)
                .as("Response time validation failed")
                .isLessThanOrEqualTo(maxTimeInMs);
        
        logger.info("Response time validation passed: {}ms (max: {}ms)", actualTime, maxTimeInMs);
        return this;
    }

    /**
     * Validate response body contains expected text
     */
    public ResponseValidator bodyContains(String expectedText) {
        String responseBody = response.getBody().asString();
        assertThat(responseBody)
                .as("Response body should contain: " + expectedText)
                .contains(expectedText);
        
        logger.info("Body contains validation passed for: {}", expectedText);
        return this;
    }

    /**
     * Validate response body does not contain text
     */
    public ResponseValidator bodyNotContains(String unexpectedText) {
        String responseBody = response.getBody().asString();
        assertThat(responseBody)
                .as("Response body should not contain: " + unexpectedText)
                .doesNotContain(unexpectedText);
        
        logger.info("Body not contains validation passed for: {}", unexpectedText);
        return this;
    }

    /**
     * Validate response body is empty
     */
    public ResponseValidator bodyIsEmpty() {
        String responseBody = response.getBody().asString();
        assertThat(responseBody)
                .as("Response body should be empty")
                .isEmpty();
        
        logger.info("Empty body validation passed");
        return this;
    }

    /**
     * Validate response body is not empty
     */
    public ResponseValidator bodyIsNotEmpty() {
        String responseBody = response.getBody().asString();
        assertThat(responseBody)
                .as("Response body should not be empty")
                .isNotEmpty();
        
        logger.info("Non-empty body validation passed");
        return this;
    }

    /**
     * Validate JSON path exists
     */
    public ResponseValidator jsonPathExists(String jsonPath) {
        Object value = response.jsonPath().get(jsonPath);
        assertThat(value)
                .as("JSON path should exist: " + jsonPath)
                .isNotNull();
        
        logger.info("JSON path exists validation passed: {}", jsonPath);
        return this;
    }

    /**
     * Validate JSON path value equals expected
     */
    public ResponseValidator jsonPathEquals(String jsonPath, Object expectedValue) {
        Object actualValue = response.jsonPath().get(jsonPath);
        assertThat(actualValue)
                .as("JSON path value validation failed for: " + jsonPath)
                .isEqualTo(expectedValue);
        
        logger.info("JSON path equals validation passed: {} = {}", jsonPath, expectedValue);
        return this;
    }

    /**
     * Validate JSON path value matches pattern
     */
    public ResponseValidator jsonPathMatches(String jsonPath, String regex) {
        String actualValue = response.jsonPath().getString(jsonPath);
        assertThat(actualValue)
                .as("JSON path should match pattern: " + regex)
                .matches(regex);
        
        logger.info("JSON path pattern validation passed: {} matches {}", jsonPath, regex);
        return this;
    }

    /**
     * Validate JSON array size
     */
    public ResponseValidator jsonArraySize(String jsonPath, int expectedSize) {
        List<Object> array = response.jsonPath().getList(jsonPath);
        assertThat(array)
                .as("JSON array size validation failed for: " + jsonPath)
                .hasSize(expectedSize);
        
        logger.info("JSON array size validation passed: {} has size {}", jsonPath, expectedSize);
        return this;
    }

    /**
     * Validate JSON array is not empty
     */
    public ResponseValidator jsonArrayNotEmpty(String jsonPath) {
        List<Object> array = response.jsonPath().getList(jsonPath);
        assertThat(array)
                .as("JSON array should not be empty: " + jsonPath)
                .isNotEmpty();
        
        logger.info("JSON array not empty validation passed: {}", jsonPath);
        return this;
    }

    /**
     * Validate header exists
     */
    public ResponseValidator headerExists(String headerName) {
        String headerValue = response.getHeader(headerName);
        assertThat(headerValue)
                .as("Header should exist: " + headerName)
                .isNotNull();
        
        logger.info("Header exists validation passed: {}", headerName);
        return this;
    }

    /**
     * Validate header value
     */
    public ResponseValidator headerEquals(String headerName, String expectedValue) {
        String actualValue = response.getHeader(headerName);
        assertThat(actualValue)
                .as("Header value validation failed for: " + headerName)
                .isEqualTo(expectedValue);
        
        logger.info("Header equals validation passed: {} = {}", headerName, expectedValue);
        return this;
    }

    /**
     * Validate content type
     */
    public ResponseValidator contentType(String expectedContentType) {
        String actualContentType = response.getContentType();
        assertThat(actualContentType)
                .as("Content type validation failed")
                .contains(expectedContentType);
        
        logger.info("Content type validation passed: {}", actualContentType);
        return this;
    }

    /**
     * Validate JSON schema
     */
    public ResponseValidator jsonSchema(String schemaPath) {
        InputStream schemaStream = this.getClass().getClassLoader().getResourceAsStream(schemaPath);
        assertThat(schemaStream)
                .as("Schema file not found: " + schemaPath)
                .isNotNull();
        
        response.then().assertThat().body(JsonSchemaValidator.matchesJsonSchema(schemaStream));
        logger.info("JSON schema validation passed: {}", schemaPath);
        return this;
    }

    /**
     * Validate JSON schema from string
     */
    public ResponseValidator jsonSchemaString(String jsonSchema) {
        response.then().assertThat().body(JsonSchemaValidator.matchesJsonSchema(jsonSchema));
        logger.info("JSON schema validation passed from string");
        return this;
    }

    /**
     * Custom validation using lambda
     */
    public ResponseValidator custom(java.util.function.Consumer<Response> customValidation) {
        customValidation.accept(response);
        logger.info("Custom validation executed");
        return this;
    }

    /**
     * Log response details for debugging
     */
    public ResponseValidator logResponse() {
        logger.info("Response Status: {}", response.getStatusCode());
        logger.info("Response Time: {}ms", response.getTime());
        logger.info("Response Headers: {}", response.getHeaders());
        logger.info("Response Body: {}", response.getBody().asString());
        return this;
    }

    /**
     * Get the underlying response for advanced operations
     */
    public Response getResponse() {
        return response;
    }

    /**
     * Extract value from JSON path
     */
    public <T> T extractValue(String jsonPath, Class<T> type) {
        T value = response.jsonPath().getObject(jsonPath, type);
        logger.debug("Extracted value from {}: {}", jsonPath, value);
        return value;
    }

    /**
     * Extract string value from JSON path
     */
    public String extractString(String jsonPath) {
        String value = response.jsonPath().getString(jsonPath);
        logger.debug("Extracted string from {}: {}", jsonPath, value);
        return value;
    }

    /**
     * Extract integer value from JSON path
     */
    public Integer extractInteger(String jsonPath) {
        Integer value = response.jsonPath().getInt(jsonPath);
        logger.debug("Extracted integer from {}: {}", jsonPath, value);
        return value;
    }

    /**
     * Extract list from JSON path
     */
    public <T> List<T> extractList(String jsonPath) {
        List<T> value = response.jsonPath().getList(jsonPath);
        logger.debug("Extracted list from {}: {}", jsonPath, value);
        return value;
    }

    /**
     * Extract map from JSON path
     */
    public Map<String, Object> extractMap(String jsonPath) {
        Map<String, Object> value = response.jsonPath().getMap(jsonPath);
        logger.debug("Extracted map from {}: {}", jsonPath, value);
        return value;
    }
}