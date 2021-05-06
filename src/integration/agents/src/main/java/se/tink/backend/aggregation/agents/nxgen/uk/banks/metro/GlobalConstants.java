package se.tink.backend.aggregation.agents.nxgen.uk.banks.metro;

public enum GlobalConstants {
    APP_VERSION("9.7.0"),
    PLATFORM("IOS"),
    DEVICE_NAME("Tink"),
    DEVICE_MODEL("iPhone"),
    DEVICE_COMPANY("Apple");

    private final String value;

    GlobalConstants(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
