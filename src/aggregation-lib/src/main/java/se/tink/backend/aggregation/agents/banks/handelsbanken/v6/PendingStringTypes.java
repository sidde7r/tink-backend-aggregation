package se.tink.backend.aggregation.agents.banks.handelsbanken.v6;

public enum PendingStringTypes {
    HANDELSBANKEN("PREL. KORTKÖP");

    private String value;

    private PendingStringTypes(String value) {
        this.setValue(value);
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
