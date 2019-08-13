package se.tink.backend.aggregation.configuration;

public enum Environment {
    SANDBOX("sandbox"),
    PRODUCTION("production");

    private final String value;

    Environment(String value) {
        this.value = value;
    }

    public static Environment fromString(String value) {
        return Environment.valueOf(value.toUpperCase());
    }

    @Override
    public String toString() {
        return value;
    }
}
