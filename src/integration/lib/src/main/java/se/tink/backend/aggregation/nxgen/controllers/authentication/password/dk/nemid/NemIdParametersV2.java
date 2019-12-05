package se.tink.backend.aggregation.nxgen.controllers.authentication.password.dk.nemid;

import se.tink.backend.aggregation.nxgen.http.url.URL;

public class NemIdParametersV2 {
    private final String nemIdElements;
    private final URL initialUrl;

    public NemIdParametersV2(String nemIdElements) {
        this(nemIdElements, NemIdConstantsV2.BASE_URL);
    }

    public NemIdParametersV2(String nemIdElements, URL initialUrl) {
        this.nemIdElements = nemIdElements;
        this.initialUrl = initialUrl;
    }

    public String getNemIdElements() {
        return nemIdElements;
    }

    public URL getInitialUrl() {
        return initialUrl;
    }
}
