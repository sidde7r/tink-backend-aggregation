package se.tink.backend.aggregation.nxgen.controllers.authentication.password.dk.nemid.v2;

import se.tink.backend.aggregation.nxgen.http.url.URL;

public class NemIdParametersV2 {
    private final String nemIdElements;
    private final URL initialUrl;

    public NemIdParametersV2(String nemIdElements) {
        this(nemIdElements, NemIdConstantsV2.BASE_URL);
    }

    private NemIdParametersV2(String nemIdElements, URL initialUrl) {
        this.nemIdElements = nemIdElements;
        this.initialUrl = initialUrl;
    }

    String getNemIdElements() {
        return nemIdElements;
    }

    URL getInitialUrl() {
        return initialUrl;
    }
}
