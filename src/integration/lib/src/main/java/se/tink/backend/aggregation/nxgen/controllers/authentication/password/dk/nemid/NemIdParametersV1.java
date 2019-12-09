package se.tink.backend.aggregation.nxgen.controllers.authentication.password.dk.nemid;

import se.tink.backend.aggregation.nxgen.http.url.URL;

public class NemIdParametersV1 {
    private final String nemIdElements;
    private final URL initialUrl;

    public NemIdParametersV1(String nemIdElements) {
        this(nemIdElements, NemIdConstantsV1.BASE_URL);
    }

    public NemIdParametersV1(String nemIdElements, URL initialUrl) {
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
