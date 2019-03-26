package se.tink.backend.aggregation.agents.nxgen.de.banks.fints;

public class FinTsConfiguration {
    private final String blz;
    private final String endpoint;
    private final String username;
    private final String password;

    public FinTsConfiguration(String blz, String endpoint, String username, String password) {
        this.blz = blz;
        this.endpoint = endpoint;
        this.username = username;
        this.password = password;
    }

    public String getBlz() {
        return blz;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
