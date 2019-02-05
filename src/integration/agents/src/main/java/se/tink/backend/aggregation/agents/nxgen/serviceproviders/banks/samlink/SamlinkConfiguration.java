package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink;

import se.tink.backend.aggregation.nxgen.http.URL;

public class SamlinkConfiguration {

    private final String baseUrl;

    public SamlinkConfiguration(String baseUrl) {

        this.baseUrl = baseUrl;
    }

    public URL build(String path) {
        return new URL(baseUrl + path);
    }
}
