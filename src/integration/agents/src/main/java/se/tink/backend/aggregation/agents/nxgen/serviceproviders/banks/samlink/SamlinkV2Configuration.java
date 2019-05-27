package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink;

import se.tink.backend.aggregation.nxgen.http.URL;

public class SamlinkV2Configuration implements SamlinkConfiguration {

    private final String baseUrl;
    private String clientApp;

    public SamlinkV2Configuration(String baseUrl, String clientApp) {
        this.baseUrl = baseUrl;
        this.clientApp = clientApp;
    }

    public URL build(String path) {
        return new URL(baseUrl + path);
    }

    public String getClientApp() {
        return clientApp;
    }

    public boolean isV2() {
        return true;
    }
}
