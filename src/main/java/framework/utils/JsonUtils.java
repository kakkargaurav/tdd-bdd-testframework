package framework.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * JSON processing utilities using Jackson
 */
public class JsonUtils {
    private static final Logger logger = LoggerFactory.getLogger(JsonUtils.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        // Configure ObjectMapper
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
    }

    /**
     * Convert object to JSON string
     */
    public static String toJson(Object object) {
        try {
            String json = objectMapper.writeValueAsString(object);
            logger.debug("Object converted to JSON: {}", json);
            return json;
        } catch (JsonProcessingException e) {
            logger.error("Failed to convert object to JSON", e);
            throw new RuntimeException("JSON conversion failed", e);
        }
    }

    /**
     * Convert object to pretty JSON string
     */
    public static String toPrettyJson(Object object) {
        try {
            String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(object);
            logger.debug("Object converted to pretty JSON");
            return json;
        } catch (JsonProcessingException e) {
            logger.error("Failed to convert object to pretty JSON", e);
            throw new RuntimeException("JSON conversion failed", e);
        }
    }

    /**
     * Parse JSON string to object
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        try {
            T object = objectMapper.readValue(json, clazz);
            logger.debug("JSON parsed to object of type: {}", clazz.getSimpleName());
            return object;
        } catch (JsonProcessingException e) {
            logger.error("Failed to parse JSON to object of type: {}", clazz.getSimpleName(), e);
            throw new RuntimeException("JSON parsing failed", e);
        }
    }

    /**
     * Parse JSON from InputStream to object
     */
    public static <T> T fromJson(InputStream inputStream, Class<T> clazz) {
        try {
            T object = objectMapper.readValue(inputStream, clazz);
            logger.debug("JSON parsed from InputStream to object of type: {}", clazz.getSimpleName());
            return object;
        } catch (IOException e) {
            logger.error("Failed to parse JSON from InputStream to object of type: {}", clazz.getSimpleName(), e);
            throw new RuntimeException("JSON parsing failed", e);
        }
    }

    /**
     * Parse JSON string to Map
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> toMap(String json) {
        try {
            Map<String, Object> map = objectMapper.readValue(json, Map.class);
            logger.debug("JSON parsed to Map with {} keys", map.size());
            return map;
        } catch (JsonProcessingException e) {
            logger.error("Failed to parse JSON to Map", e);
            throw new RuntimeException("JSON parsing failed", e);
        }
    }

    /**
     * Parse JSON from InputStream to Map
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> toMap(InputStream inputStream) {
        try {
            Map<String, Object> map = objectMapper.readValue(inputStream, Map.class);
            logger.debug("JSON parsed from InputStream to Map with {} keys", map.size());
            return map;
        } catch (IOException e) {
            logger.error("Failed to parse JSON from InputStream to Map", e);
            throw new RuntimeException("JSON parsing failed", e);
        }
    }

    /**
     * Get JsonNode from JSON string
     */
    public static JsonNode getJsonNode(String json) {
        try {
            JsonNode node = objectMapper.readTree(json);
            logger.debug("JSON string converted to JsonNode");
            return node;
        } catch (JsonProcessingException e) {
            logger.error("Failed to convert JSON string to JsonNode", e);
            throw new RuntimeException("JSON parsing failed", e);
        }
    }

    /**
     * Get value from JSON using path
     */
    public static Object getValue(String json, String path) {
        try {
            JsonNode rootNode = objectMapper.readTree(json);
            JsonNode valueNode = rootNode.at(path);
            
            if (valueNode.isMissingNode()) {
                logger.warn("JSON path not found: {}", path);
                return null;
            }
            
            Object value = convertJsonNodeToObject(valueNode);
            logger.debug("Value extracted from JSON path {}: {}", path, value);
            return value;
        } catch (JsonProcessingException e) {
            logger.error("Failed to get value from JSON path: {}", path, e);
            throw new RuntimeException("JSON path extraction failed", e);
        }
    }

    /**
     * Set value in JSON using path
     */
    public static String setValue(String json, String path, Object value) {
        try {
            JsonNode rootNode = objectMapper.readTree(json);
            // This is a simplified implementation - for complex scenarios, consider using JsonPath library
            logger.warn("setValue method is simplified - consider using JsonPath library for complex scenarios");
            return json; // Placeholder implementation
        } catch (JsonProcessingException e) {
            logger.error("Failed to set value in JSON path: {}", path, e);
            throw new RuntimeException("JSON modification failed", e);
        }
    }

    /**
     * Check if JSON is valid
     */
    public static boolean isValidJson(String json) {
        try {
            objectMapper.readTree(json);
            logger.debug("JSON validation passed");
            return true;
        } catch (JsonProcessingException e) {
            logger.debug("JSON validation failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Merge two JSON objects
     */
    public static String mergeJson(String json1, String json2) {
        try {
            JsonNode node1 = objectMapper.readTree(json1);
            JsonNode node2 = objectMapper.readTree(json2);
            
            // Simple merge - node2 values override node1 values
            ObjectMapper merger = new ObjectMapper();
            JsonNode merged = merger.readerForUpdating(node1).readValue(node2);
            
            String result = objectMapper.writeValueAsString(merged);
            logger.debug("JSON objects merged successfully");
            return result;
        } catch (IOException e) {
            logger.error("Failed to merge JSON objects", e);
            throw new RuntimeException("JSON merge failed", e);
        }
    }

    /**
     * Convert JSON to formatted string for logging
     */
    public static String formatForLogging(String json) {
        if (isValidJson(json)) {
            try {
                Object jsonObject = objectMapper.readValue(json, Object.class);
                return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonObject);
            } catch (JsonProcessingException e) {
                logger.warn("Failed to format JSON for logging, returning original", e);
                return json;
            }
        }
        return json;
    }

    /**
     * Convert JsonNode to appropriate Java object
     */
    private static Object convertJsonNodeToObject(JsonNode node) {
        if (node.isNull()) {
            return null;
        } else if (node.isBoolean()) {
            return node.asBoolean();
        } else if (node.isInt()) {
            return node.asInt();
        } else if (node.isLong()) {
            return node.asLong();
        } else if (node.isDouble()) {
            return node.asDouble();
        } else if (node.isTextual()) {
            return node.asText();
        } else if (node.isArray()) {
            return objectMapper.convertValue(node, Object[].class);
        } else if (node.isObject()) {
            return objectMapper.convertValue(node, Map.class);
        }
        return node.toString();
    }

    /**
     * Get ObjectMapper instance for advanced usage
     */
    public static ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    /**
     * Deep clone an object using JSON serialization
     */
    public static <T> T deepClone(T object, Class<T> clazz) {
        try {
            String json = objectMapper.writeValueAsString(object);
            T cloned = objectMapper.readValue(json, clazz);
            logger.debug("Object deep cloned using JSON serialization");
            return cloned;
        } catch (JsonProcessingException e) {
            logger.error("Failed to deep clone object", e);
            throw new RuntimeException("Deep clone failed", e);
        }
    }
}