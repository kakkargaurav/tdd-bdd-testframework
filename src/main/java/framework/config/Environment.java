package framework.config;

/**
 * Enum representing different test environments
 */
public enum Environment {
    DEV("dev"),
    STAGING("staging"),
    PROD("prod"),
    PI("PI");

    private final String name;

    Environment(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    /**
     * Get Environment from string name
     */
    public static Environment fromString(String name) {
        for (Environment env : Environment.values()) {
            if (env.getName().equalsIgnoreCase(name)) {
                return env;
            }
        }
        // Default to DEV if not found
        return DEV;
    }

    @Override
    public String toString() {
        return name;
    }
}