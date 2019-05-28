package se.tink.backend.aggregation.register;

import java.util.Arrays;

public enum RegisterEnvironment {
    UNKNOWN("unknown"),
    LOCAL("local"),
    PRODUCTION("production"),
    STAGING("staging");

    private final String name;

    RegisterEnvironment(String name) {
        this.name = name;
    }

    public static RegisterEnvironment fromString(String text) {
        return Arrays.stream(RegisterEnvironment.values())
                .filter(e -> e.name.equalsIgnoreCase(text))
                .findFirst()
                .orElse(UNKNOWN);
    }

    public String toString() {
        return name;
    }
}
