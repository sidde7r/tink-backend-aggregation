package se.tink.backend.aggregation.log;

public enum Tag {
    CREDENTIALS("CREDENTIALS"),
    AUTHENTICATION("AUTHENTICATION");

    private String value;

    Tag(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
