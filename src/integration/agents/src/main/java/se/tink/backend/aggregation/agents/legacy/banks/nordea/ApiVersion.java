package se.tink.backend.aggregation.agents.banks.nordea;

public enum ApiVersion {
    V21("V2.1"), V23("V2.3");

    private final String version;

    ApiVersion(String version) {
        this.version = version;
    }

    public String get() {
        return version;
    }
}
