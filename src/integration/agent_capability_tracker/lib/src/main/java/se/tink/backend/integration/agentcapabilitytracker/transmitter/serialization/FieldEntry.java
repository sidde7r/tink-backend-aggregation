package se.tink.backend.integration.agentcapabilitytracker.transmitter.serialization;

public class FieldEntry {
    String name;
    String value;

    private FieldEntry(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public static FieldEntry of(String name, String value) {
        return new FieldEntry(name, value);
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }
}
