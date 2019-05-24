package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink;

import se.tink.backend.aggregation.nxgen.http.URL;

public class SamlinkConfiguration {

    private final String baseUrl;
    private String clientApp;
    private boolean v2 = false;

    public SamlinkConfiguration(String baseUrl, String clientApp) {
        this.baseUrl = baseUrl;
        this.clientApp = clientApp;
        v2 = true;
    }

    public SamlinkConfiguration(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public URL build(String path) {
        return new URL(baseUrl + path);
    }

    public String getClientApp() {
        return clientApp;
    }

    public boolean isV2() {
        return v2;
    }
}
