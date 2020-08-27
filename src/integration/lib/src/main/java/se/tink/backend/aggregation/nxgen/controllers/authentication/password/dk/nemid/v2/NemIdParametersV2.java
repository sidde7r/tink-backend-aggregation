package se.tink.backend.aggregation.nxgen.controllers.authentication.password.dk.nemid.v2;

public class NemIdParametersV2 {
    private final String nemIdElements;
    private final String initialUrl;

    public NemIdParametersV2(String nemIdElements) {
        this(nemIdElements, NemIdConstantsV2.NEM_ID_APPLET_URL);
    }

    private NemIdParametersV2(String nemIdElements, String initialUrl) {
        this.nemIdElements = nemIdElements;
        this.initialUrl = initialUrl;
    }

    String getNemIdElements() {
        return nemIdElements;
    }

    String getInitialUrl() {
        return initialUrl;
    }
}
