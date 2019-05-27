package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink;

import se.tink.backend.aggregation.nxgen.http.URL;

public class SamlinkV1Configuration implements SamlinkConfiguration {

    private final String baseUrl;

    public SamlinkV1Configuration(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public URL build(String path) {
        return new URL(baseUrl + path);
    }

    public String getClientApp() {
        return null;
    }

    public boolean isV2() {
        return false;
    }
}
