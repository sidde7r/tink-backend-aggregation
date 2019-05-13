package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bunq;

import se.tink.backend.aggregation.nxgen.http.URL;

public class BunqBaseConfiguration {
    private final String backendHost;

    BunqBaseConfiguration(String backendHost) {
        this.backendHost = backendHost;
    }

    public URL getUrl(String path) {
        return new URL(backendHost + path);
    }
}
