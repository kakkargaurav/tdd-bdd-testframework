package framework.auth;

import framework.config.ConfigManager;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

/**
 * Handles Bearer Token Authentication
 */
public class BearerTokenAuth {
    private static final Logger logger = LoggerFactory.getLogger(BearerTokenAuth.class);
    private final ConfigManager configManager;
    private String token;
    private LocalDateTime tokenExpiry;
    private static final int DEFAULT_TOKEN_VALIDITY_MINUTES = 60;

    public BearerTokenAuth() {
        this.configManager = ConfigManager.getInstance();
    }

    /**
     * Obtain bearer token using username and password
     */
    public String obtainToken(String username, String password) {
        try {
            String tokenEndpoint = configManager.getAuthTokenEndpoint();
            
            Map<String, String> credentials = new HashMap<>();
            credentials.put("username", username);
            credentials.put("password", password);

            Response response = RestAssured.given()
                    .contentType("application/json")
                    .body(credentials)
                    .post(tokenEndpoint);

            if (response.getStatusCode() == 200) {
                // Extract token from response - adjust based on your API response format
                String responseBody = response.getBody().asString();
                
                // Common token response formats:
                // {"token": "abc123", "expires_in": 3600}
                // {"access_token": "abc123", "token_type": "Bearer"}
                
                String extractedToken = extractTokenFromResponse(responseBody);
                if (extractedToken != null) {
                    setTokenExpiry();
                    logger.info("Bearer token obtained successfully");
                    return extractedToken;
                } else {
                    logger.error("Failed to extract token from response: {}", responseBody);
                }
            } else {
                logger.error("Failed to obtain token. Status: {}, Response: {}", 
                           response.getStatusCode(), response.getBody().asString());
            }
        } catch (Exception e) {
            logger.error("Error obtaining bearer token", e);
        }
        return null;
    }

    /**
     * Extract token from API response
     * This method should be customized based on your API's response format
     */
    private String extractTokenFromResponse(String responseBody) {
        try {
            // Simple JSON parsing - you might want to use Jackson for complex scenarios
            if (responseBody.contains("\"token\"")) {
                // Format: {"token": "abc123"}
                return responseBody.split("\"token\":\\s*\"")[1].split("\"")[0];
            } else if (responseBody.contains("\"access_token\"")) {
                // Format: {"access_token": "abc123"}
                return responseBody.split("\"access_token\":\\s*\"")[1].split("\"")[0];
            }
            
            // If response is just the token as plain text
            if (!responseBody.contains("{") && !responseBody.contains("}")) {
                return responseBody.trim();
            }
        } catch (Exception e) {
            logger.error("Error parsing token from response", e);
        }
        return null;
    }

    /**
     * Set token manually
     */
    public void setToken(String token) {
        this.token = token;
        setTokenExpiry();
        logger.debug("Bearer token set manually");
    }

    /**
     * Set token expiry time
     */
    private void setTokenExpiry() {
        this.tokenExpiry = LocalDateTime.now().plus(DEFAULT_TOKEN_VALIDITY_MINUTES, ChronoUnit.MINUTES);
    }

    /**
     * Add bearer token to request
     */
    public RequestSpecification addToRequest(RequestSpecification requestSpec) {
        if (token != null && !token.isEmpty()) {
            return requestSpec.header("Authorization", "Bearer " + token);
        }
        logger.warn("Bearer token not set");
        return requestSpec;
    }

    /**
     * Check if token is expired
     */
    public boolean isTokenExpired() {
        if (tokenExpiry == null) {
            return true;
        }
        return LocalDateTime.now().isAfter(tokenExpiry);
    }

    /**
     * Get current token
     */
    public String getToken() {
        return token;
    }

    /**
     * Clear token
     */
    public void clearToken() {
        this.token = null;
        this.tokenExpiry = null;
        logger.debug("Bearer token cleared");
    }

    /**
     * Check if token is set
     */
    public boolean hasToken() {
        return token != null && !token.isEmpty() && !isTokenExpired();
    }

    /**
     * Get token expiry time
     */
    public LocalDateTime getTokenExpiry() {
        return tokenExpiry;
    }

    /**
     * Refresh token using existing credentials
     * This is a placeholder - implement based on your API's refresh mechanism
     */
    public boolean refreshToken() {
        // This would typically use a refresh token or re-authenticate
        logger.warn("Token refresh not implemented - please re-authenticate");
        return false;
    }
}