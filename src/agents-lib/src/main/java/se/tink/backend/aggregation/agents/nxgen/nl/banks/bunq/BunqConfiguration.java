package se.tink.backend.aggregation.agents.nxgen.nl.banks.bunq;

import se.tink.backend.aggregation.nxgen.http.URL;

public class BunqConfiguration {
    private final String backendHost;

    BunqConfiguration(String backendHost) {
        this.backendHost = backendHost;
    }

    public URL getUrl(String path) {
        return new URL(backendHost + path);
    }
}
