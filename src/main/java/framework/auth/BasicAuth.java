package framework.auth;

import io.restassured.specification.RequestSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles Basic Authentication
 */
public class BasicAuth {
    private static final Logger logger = LoggerFactory.getLogger(BasicAuth.class);
    private String username;
    private String password;

    /**
     * Set credentials for basic authentication
     */
    public void setCredentials(String username, String password) {
        this.username = username;
        this.password = password;
        logger.debug("Basic auth credentials set for user: {}", username);
    }

    /**
     * Add basic authentication to request
     */
    public RequestSpecification addToRequest(RequestSpecification requestSpec) {
        if (username != null && password != null) {
            return requestSpec.auth().basic(username, password);
        }
        logger.warn("Basic auth credentials not set");
        return requestSpec;
    }

    /**
     * Clear stored credentials
     */
    public void clearCredentials() {
        this.username = null;
        this.password = null;
        logger.debug("Basic auth credentials cleared");
    }

    /**
     * Check if credentials are set
     */
    public boolean hasCredentials() {
        return username != null && password != null;
    }

    /**
     * Get username (for testing purposes)
     */
    public String getUsername() {
        return username;
    }
}