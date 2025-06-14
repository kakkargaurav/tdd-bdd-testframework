package framework.auth;

import framework.config.ConfigManager;
import io.restassured.specification.RequestSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages authentication for API requests
 * Supports both Bearer Token and Basic Authentication
 */
public class AuthenticationManager {
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationManager.class);
    private final ConfigManager configManager;
    private BearerTokenAuth bearerTokenAuth;
    private BasicAuth basicAuth;
    private AuthType currentAuthType;
    private boolean isAuthenticated = false;

    public enum AuthType {
        BASIC, BEARER, NONE
    }

    public AuthenticationManager() {
        this.configManager = ConfigManager.getInstance();
        this.bearerTokenAuth = new BearerTokenAuth();
        this.basicAuth = new BasicAuth();
        this.currentAuthType = AuthType.NONE;
    }

    /**
     * Authenticate using username and password
     * Auth type is determined by configuration
     */
    public void authenticate(String username, String password) {
        String authType = configManager.getAuthType().toLowerCase();
        
        switch (authType) {
            case "basic":
                authenticateBasic(username, password);
                break;
            case "bearer":
                authenticateBearer(username, password);
                break;
            default:
                logger.warn("Unknown auth type: {}. No authentication will be applied.", authType);
                currentAuthType = AuthType.NONE;
                isAuthenticated = false;
        }
    }

    /**
     * Authenticate using Basic Auth
     */
    public void authenticateBasic(String username, String password) {
        basicAuth.setCredentials(username, password);
        currentAuthType = AuthType.BASIC;
        isAuthenticated = true;
        logger.info("Basic authentication configured for user: {}", username);
    }

    /**
     * Authenticate using Bearer Token (obtain token first)
     */
    public void authenticateBearer(String username, String password) {
        String token = bearerTokenAuth.obtainToken(username, password);
        if (token != null && !token.isEmpty()) {
            bearerTokenAuth.setToken(token);
            currentAuthType = AuthType.BEARER;
            isAuthenticated = true;
            logger.info("Bearer token authentication configured");
        } else {
            logger.error("Failed to obtain bearer token for user: {}", username);
            isAuthenticated = false;
        }
    }

    /**
     * Authenticate using pre-obtained token
     */
    public void authenticateWithToken(String token) {
        bearerTokenAuth.setToken(token);
        currentAuthType = AuthType.BEARER;
        isAuthenticated = true;
        logger.info("Bearer token authentication configured with provided token");
    }

    /**
     * Add authentication to request specification
     */
    public RequestSpecification addAuthenticationToRequest(RequestSpecification requestSpec) {
        if (!isAuthenticated) {
            return requestSpec;
        }

        switch (currentAuthType) {
            case BASIC:
                return basicAuth.addToRequest(requestSpec);
            case BEARER:
                return bearerTokenAuth.addToRequest(requestSpec);
            default:
                return requestSpec;
        }
    }

    /**
     * Check if currently authenticated
     */
    public boolean isAuthenticated() {
        return isAuthenticated;
    }

    /**
     * Get current authentication type
     */
    public AuthType getCurrentAuthType() {
        return currentAuthType;
    }

    /**
     * Clear authentication
     */
    public void clearAuthentication() {
        basicAuth.clearCredentials();
        bearerTokenAuth.clearToken();
        currentAuthType = AuthType.NONE;
        isAuthenticated = false;
        logger.info("Authentication cleared");
    }

    /**
     * Refresh bearer token if needed
     */
    public boolean refreshTokenIfNeeded() {
        if (currentAuthType == AuthType.BEARER && bearerTokenAuth.isTokenExpired()) {
            logger.info("Token expired, attempting to refresh...");
            // This would typically use refresh token logic
            // For now, return false to indicate refresh needed
            return false;
        }
        return true;
    }

    /**
     * Get current token (if using bearer auth)
     */
    public String getCurrentToken() {
        if (currentAuthType == AuthType.BEARER) {
            return bearerTokenAuth.getToken();
        }
        return null;
    }
}