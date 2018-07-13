package se.tink.backend.aggregation.nxgen.controllers.authentication.password.dk.nemid;

import se.tink.backend.aggregation.nxgen.http.URL;

public class NemIdParameters {
    private final String nemIdElements;
    private final URL initialUrl;

    public NemIdParameters(String nemIdElements) {
        this(nemIdElements, NemIdConstants.BASE_URL);
    }

    public NemIdParameters(String nemIdElements, URL initialUrl) {
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
