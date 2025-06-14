package framework.config;

import framework.utils.LogManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Manages configuration properties for different environments
 * Singleton pattern to ensure single instance across the framework
 */
public class ConfigManager {
    private static final Logger logger = LoggerFactory.getLogger(ConfigManager.class);
    private static ConfigManager instance;
    private Properties properties;
    private Environment currentEnvironment;

    private ConfigManager() {
        loadConfiguration();
    }

    /**
     * Get singleton instance of ConfigManager
     */
    public static synchronized ConfigManager getInstance() {
        if (instance == null) {
            instance = new ConfigManager();
        }
        return instance;
    }

    /**
     * Load configuration based on environment
     */
    private void loadConfiguration() {
        // Get environment from system property, default to dev
        String envName = System.getProperty("environment", "dev");
        currentEnvironment = Environment.fromString(envName);
        
        String configFile = "config/" + currentEnvironment.getName() + ".properties";
        properties = new Properties();
        
        try (InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(configFile)) {
            if (inputStream != null) {
                properties.load(inputStream);
                logger.info("Configuration loaded for environment: {}", currentEnvironment.getName());
            } else {
                logger.warn("Configuration file not found: {}. Loading default properties.", configFile);
                loadDefaultProperties();
            }
        } catch (IOException e) {
            logger.error("Error loading configuration file: {}", configFile, e);
            loadDefaultProperties();
        }
    }

    /**
     * Load default properties if environment-specific file is not found
     */
    private void loadDefaultProperties() {
        properties.setProperty("base.url", "http://localhost:8080");
        properties.setProperty("timeout", "30000");
        properties.setProperty("auth.type", "basic");
        properties.setProperty("auth.username", "testuser");
        properties.setProperty("auth.password", "testpass");
        logger.info("Default properties loaded");
    }

    /**
     * Get base URL for API
     */
    public String getBaseUrl() {
        return getProperty("base.url", "http://localhost:8080");
    }

    /**
     * Get request timeout in milliseconds
     */
    public int getTimeout() {
        return Integer.parseInt(getProperty("timeout", "30000"));
    }

    /**
     * Get authentication type
     */
    public String getAuthType() {
        return getProperty("auth.type", "basic");
    }

    /**
     * Get authentication username
     */
    public String getAuthUsername() {
        return getProperty("auth.username", "");
    }

    /**
     * Get authentication password
     */
    public String getAuthPassword() {
        return getProperty("auth.password", "");
    }

    /**
     * Get authentication token endpoint
     */
    public String getAuthTokenEndpoint() {
        return getProperty("auth.token.endpoint", "/api/auth/token");
    }

    /**
     * Get current environment
     */
    public Environment getCurrentEnvironment() {
        return currentEnvironment;
    }

    /**
     * Get property value with default fallback
     */
    public String getProperty(String key, String defaultValue) {
        String value = properties.getProperty(key, defaultValue);
        // Replace environment variables
        if (value.startsWith("${") && value.endsWith("}")) {
            String envVar = value.substring(2, value.length() - 1);
            value = System.getenv(envVar);
            if (value == null) {
                logger.warn("Environment variable {} not found, using default: {}", envVar, defaultValue);
                value = defaultValue;
            }
        }
        return value;
    }

    /**
     * Get property value
     */
    public String getProperty(String key) {
        return getProperty(key, null);
    }

    /**
     * Set property value (for testing purposes)
     */
    public void setProperty(String key, String value) {
        properties.setProperty(key, value);
        logger.debug("Property set: {} = {}", key, value);
    }

    /**
     * Reload configuration for different environment
     */
    public void reloadConfiguration(Environment environment) {
        currentEnvironment = environment;
        loadConfiguration();
        logger.info("Configuration reloaded for environment: {}", environment.getName());
    }

    /**
     * Get all properties (for debugging)
     */
    public Properties getAllProperties() {
        return new Properties(properties);
    }

    /**
     * Check if property exists
     */
    public boolean hasProperty(String key) {
        return properties.containsKey(key);
    }
}