package framework.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Centralized data provider for test data management
 * Supports JSON and YAML data files with caching
 */
public class DataProvider {
    private static final Logger logger = LoggerFactory.getLogger(DataProvider.class);
    private static final ObjectMapper jsonMapper = new ObjectMapper();
    private static final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
    
    // Cache for loaded data files
    private static final Map<String, Map<String, Object>> dataCache = new ConcurrentHashMap<>();

    /**
     * Load JSON data from file
     */
    public static Map<String, Object> loadJsonData(String fileName) {
        return loadData(fileName, "json");
    }

    /**
     * Load YAML data from file
     */
    public static Map<String, Object> loadYamlData(String fileName) {
        return loadData(fileName, "yaml");
    }

    /**
     * Load data from file with caching
     */
    @SuppressWarnings("unchecked")
    private static Map<String, Object> loadData(String fileName, String format) {
        String cacheKey = fileName + "_" + format;
        
        // Check cache first
        if (dataCache.containsKey(cacheKey)) {
            logger.debug("Data retrieved from cache: {}", fileName);
            return dataCache.get(cacheKey);
        }

        String filePath = "testData/" + fileName;
        if (!fileName.endsWith("." + format)) {
            filePath += "." + format;
        }

        try (InputStream inputStream = DataProvider.class.getClassLoader().getResourceAsStream(filePath)) {
            if (inputStream == null) {
                logger.error("Data file not found: {}", filePath);
                throw new RuntimeException("Data file not found: " + filePath);
            }

            ObjectMapper mapper = "yaml".equals(format) ? yamlMapper : jsonMapper;
            Map<String, Object> data = mapper.readValue(inputStream, Map.class);
            
            // Cache the loaded data
            dataCache.put(cacheKey, data);
            logger.info("Data loaded and cached: {} ({} keys)", fileName, data.size());
            
            return data;
        } catch (IOException e) {
            logger.error("Failed to load data file: {}", filePath, e);
            throw new RuntimeException("Failed to load data file: " + filePath, e);
        }
    }

    /**
     * Get specific data object from JSON file
     */
    public static <T> T getJsonData(String fileName, String key, Class<T> clazz) {
        Map<String, Object> data = loadJsonData(fileName);
        return convertToType(data.get(key), clazz, fileName, key);
    }

    /**
     * Get specific data object from YAML file
     */
    public static <T> T getYamlData(String fileName, String key, Class<T> clazz) {
        Map<String, Object> data = loadYamlData(fileName);
        return convertToType(data.get(key), clazz, fileName, key);
    }

    /**
     * Get user data from default users.json file
     */
    public static Map<String, Object> getUserData(String userType) {
        Map<String, Object> data = loadJsonData("users.json");
        Object userData = data.get(userType);
        if (userData == null) {
            logger.error("User type not found: {}", userType);
            throw new RuntimeException("User type not found: " + userType);
        }
        return convertToType(userData, Map.class, "users.json", userType);
    }

    /**
     * Get endpoint data from default endpoints.yaml file
     */
    public static String getEndpoint(String endpointName) {
        Map<String, Object> data = loadYamlData("endpoints.yaml");
        Object endpoint = data.get(endpointName);
        if (endpoint == null) {
            logger.error("Endpoint not found: {}", endpointName);
            throw new RuntimeException("Endpoint not found: " + endpointName);
        }
        return endpoint.toString();
    }

    /**
     * Get test scenario data
     */
    public static Map<String, Object> getScenarioData(String scenarioName) {
        Map<String, Object> data = loadJsonData("scenarios.json");
        Object scenarioData = data.get(scenarioName);
        if (scenarioData == null) {
            logger.error("Scenario data not found: {}", scenarioName);
            throw new RuntimeException("Scenario data not found: " + scenarioName);
        }
        return convertToType(scenarioData, Map.class, "scenarios.json", scenarioName);
    }

    /**
     * Get API response template
     */
    public static Map<String, Object> getResponseTemplate(String templateName) {
        Map<String, Object> data = loadJsonData("responseTemplates.json");
        Object template = data.get(templateName);
        if (template == null) {
            logger.error("Response template not found: {}", templateName);
            throw new RuntimeException("Response template not found: " + templateName);
        }
        return convertToType(template, Map.class, "responseTemplates.json", templateName);
    }

    /**
     * Convert data to specified type
     */
    @SuppressWarnings("unchecked")
    private static <T> T convertToType(Object data, Class<T> clazz, String fileName, String key) {
        if (data == null) {
            logger.error("Data not found: {} in file {}", key, fileName);
            throw new RuntimeException("Data not found: " + key + " in file " + fileName);
        }

        try {
            if (clazz.isInstance(data)) {
                return (T) data;
            }
            
            // Use ObjectMapper for complex type conversion
            return jsonMapper.convertValue(data, clazz);
        } catch (Exception e) {
            logger.error("Failed to convert data to type {}: {} from {}", clazz.getSimpleName(), key, fileName, e);
            throw new RuntimeException("Data type conversion failed", e);
        }
    }

    /**
     * Clear data cache
     */
    public static void clearCache() {
        dataCache.clear();
        logger.info("Data cache cleared");
    }

    /**
     * Clear specific file from cache
     */
    public static void clearCache(String fileName) {
        dataCache.entrySet().removeIf(entry -> entry.getKey().startsWith(fileName));
        logger.info("Cache cleared for file: {}", fileName);
    }

    /**
     * Get all available data keys from a file
     */
    public static String[] getDataKeys(String fileName, String format) {
        Map<String, Object> data = loadData(fileName, format);
        return data.keySet().toArray(new String[0]);
    }

    /**
     * Check if data exists
     */
    public static boolean hasData(String fileName, String key, String format) {
        try {
            Map<String, Object> data = loadData(fileName, format);
            return data.containsKey(key);
        } catch (Exception e) {
            logger.debug("Data check failed for {}.{}: {}", fileName, key, e.getMessage());
            return false;
        }
    }

    /**
     * Reload data from file (bypass cache)
     */
    public static Map<String, Object> reloadData(String fileName, String format) {
        String cacheKey = fileName + "_" + format;
        dataCache.remove(cacheKey);
        return loadData(fileName, format);
    }

    /**
     * Get data with default value
     */
    public static <T> T getDataWithDefault(String fileName, String key, String format, Class<T> clazz, T defaultValue) {
        try {
            Map<String, Object> data = loadData(fileName, format);
            Object value = data.get(key);
            if (value == null) {
                logger.debug("Data key not found, using default: {} in {}", key, fileName);
                return defaultValue;
            }
            return convertToType(value, clazz, fileName, key);
        } catch (Exception e) {
            logger.warn("Failed to get data, using default: {} in {}", key, fileName, e);
            return defaultValue;
        }
    }

    /**
     * Merge data from multiple files
     */
    public static Map<String, Object> mergeData(String... fileNames) {
        Map<String, Object> mergedData = new ConcurrentHashMap<>();
        
        for (String fileName : fileNames) {
            try {
                String format = fileName.endsWith(".yaml") || fileName.endsWith(".yml") ? "yaml" : "json";
                Map<String, Object> data = loadData(fileName, format);
                mergedData.putAll(data);
                logger.debug("Merged data from file: {}", fileName);
            } catch (Exception e) {
                logger.warn("Failed to merge data from file: {}", fileName, e);
            }
        }
        
        logger.info("Data merged from {} files, total keys: {}", fileNames.length, mergedData.size());
        return mergedData;
    }

    /**
     * Get nested data using dot notation (e.g., "user.profile.name")
     */
    @SuppressWarnings("unchecked")
    public static <T> T getNestedData(String fileName, String dotPath, String format, Class<T> clazz) {
        Map<String, Object> data = loadData(fileName, format);
        
        String[] pathParts = dotPath.split("\\.");
        Object current = data;
        
        for (String part : pathParts) {
            if (current instanceof Map) {
                current = ((Map<String, Object>) current).get(part);
                if (current == null) {
                    logger.error("Nested path not found: {} in {}", dotPath, fileName);
                    throw new RuntimeException("Nested path not found: " + dotPath);
                }
            } else {
                logger.error("Invalid nested path: {} in {}", dotPath, fileName);
                throw new RuntimeException("Invalid nested path: " + dotPath);
            }
        }
        
        return convertToType(current, clazz, fileName, dotPath);
    }
}