package se.tink.backend.aggregation.register;

import java.util.Arrays;

public enum RegisterEnvironment {
    UNKNOWN("unknown"),
    PRODUCTION("production"),
    SANDBOX("sandbox");

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

    @Override
    public String toString() {
        return name;
    }
}
