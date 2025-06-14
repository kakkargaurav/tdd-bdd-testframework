package framework.core;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Fluent builder for constructing API requests
 * Provides a chainable interface for building complex requests
 */
public class RequestBuilder {
    private static final Logger logger = LoggerFactory.getLogger(RequestBuilder.class);
    private final ApiClient apiClient;
    private RequestSpecification requestSpec;
    private String endpoint;
    private Object requestBody;
    private Map<String, Object> pathParams;
    private Map<String, Object> queryParams;
    private Map<String, String> headers;
    private ContentType contentType;

    public RequestBuilder(ApiClient apiClient) {
        this.apiClient = apiClient;
        this.pathParams = new HashMap<>();
        this.queryParams = new HashMap<>();
        this.headers = new HashMap<>();
        this.contentType = ContentType.JSON;
        this.requestSpec = apiClient.getRequestSpec();
    }

    /**
     * Set the endpoint for the request
     */
    public RequestBuilder endpoint(String endpoint) {
        this.endpoint = endpoint;
        return this;
    }

    /**
     * Set the request body
     */
    public RequestBuilder body(Object body) {
        this.requestBody = body;
        return this;
    }

    /**
     * Add a path parameter
     */
    public RequestBuilder pathParam(String key, Object value) {
        this.pathParams.put(key, value);
        return this;
    }

    /**
     * Add multiple path parameters
     */
    public RequestBuilder pathParams(Map<String, Object> pathParams) {
        this.pathParams.putAll(pathParams);
        return this;
    }

    /**
     * Add a query parameter
     */
    public RequestBuilder queryParam(String key, Object value) {
        this.queryParams.put(key, value);
        return this;
    }

    /**
     * Add multiple query parameters
     */
    public RequestBuilder queryParams(Map<String, Object> queryParams) {
        this.queryParams.putAll(queryParams);
        return this;
    }

    /**
     * Add a header
     */
    public RequestBuilder header(String key, String value) {
        this.headers.put(key, value);
        return this;
    }

    /**
     * Add multiple headers
     */
    public RequestBuilder headers(Map<String, String> headers) {
        this.headers.putAll(headers);
        return this;
    }

    /**
     * Set content type
     */
    public RequestBuilder contentType(ContentType contentType) {
        this.contentType = contentType;
        return this;
    }

    /**
     * Set content type as JSON
     */
    public RequestBuilder json() {
        this.contentType = ContentType.JSON;
        return this;
    }

    /**
     * Set content type as XML
     */
    public RequestBuilder xml() {
        this.contentType = ContentType.XML;
        return this;
    }

    /**
     * Set content type as form URL encoded
     */
    public RequestBuilder formUrlEncoded() {
        this.contentType = ContentType.URLENC;
        return this;
    }

    /**
     * Build the request specification
     */
    private RequestSpecification buildRequest() {
        RequestSpecification spec = apiClient.getRequestSpec()
                .contentType(contentType);

        // Add headers
        if (!headers.isEmpty()) {
            spec = spec.headers(headers);
        }

        // Add path parameters
        if (!pathParams.isEmpty()) {
            spec = spec.pathParams(pathParams);
        }

        // Add query parameters
        if (!queryParams.isEmpty()) {
            spec = spec.queryParams(queryParams);
        }

        // Add body if present
        if (requestBody != null) {
            spec = spec.body(requestBody);
        }

        return spec;
    }

    /**
     * Execute GET request
     */
    public Response get() {
        validateEndpoint();
        RequestSpecification spec = buildRequest();
        logger.info("Executing GET request to: {}", endpoint);
        return spec.get(endpoint);
    }

    /**
     * Execute POST request
     */
    public Response post() {
        validateEndpoint();
        RequestSpecification spec = buildRequest();
        logger.info("Executing POST request to: {} with body: {}", endpoint, requestBody);
        return spec.post(endpoint);
    }

    /**
     * Execute PUT request
     */
    public Response put() {
        validateEndpoint();
        RequestSpecification spec = buildRequest();
        logger.info("Executing PUT request to: {} with body: {}", endpoint, requestBody);
        return spec.put(endpoint);
    }

    /**
     * Execute DELETE request
     */
    public Response delete() {
        validateEndpoint();
        RequestSpecification spec = buildRequest();
        logger.info("Executing DELETE request to: {}", endpoint);
        return spec.delete(endpoint);
    }

    /**
     * Execute PATCH request
     */
    public Response patch() {
        validateEndpoint();
        RequestSpecification spec = buildRequest();
        logger.info("Executing PATCH request to: {} with body: {}", endpoint, requestBody);
        return spec.patch(endpoint);
    }

    /**
     * Execute HEAD request
     */
    public Response head() {
        validateEndpoint();
        RequestSpecification spec = buildRequest();
        logger.info("Executing HEAD request to: {}", endpoint);
        return spec.head(endpoint);
    }

    /**
     * Execute OPTIONS request
     */
    public Response options() {
        validateEndpoint();
        RequestSpecification spec = buildRequest();
        logger.info("Executing OPTIONS request to: {}", endpoint);
        return spec.options(endpoint);
    }

    /**
     * Validate that endpoint is set
     */
    private void validateEndpoint() {
        if (endpoint == null || endpoint.trim().isEmpty()) {
            throw new IllegalStateException("Endpoint must be set before executing request");
        }
    }

    /**
     * Get the current request specification (for advanced usage)
     */
    public RequestSpecification getRequestSpec() {
        return buildRequest();
    }

    /**
     * Reset the builder to initial state
     */
    public RequestBuilder reset() {
        this.endpoint = null;
        this.requestBody = null;
        this.pathParams.clear();
        this.queryParams.clear();
        this.headers.clear();
        this.contentType = ContentType.JSON;
        this.requestSpec = apiClient.getRequestSpec();
        return this;
    }

    /**
     * Clone the current builder state
     */
    public RequestBuilder clone() {
        RequestBuilder cloned = new RequestBuilder(apiClient);
        cloned.endpoint = this.endpoint;
        cloned.requestBody = this.requestBody;
        cloned.pathParams.putAll(this.pathParams);
        cloned.queryParams.putAll(this.queryParams);
        cloned.headers.putAll(this.headers);
        cloned.contentType = this.contentType;
        return cloned;
    }
}