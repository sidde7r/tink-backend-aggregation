package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.configuration;

import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.http.URL;

@JsonObject
public class ProviderConfiguration {

    private String organizationId;
    private URL apiBaseURL;
    private URL wellKnownURL;

    private ClientInfo clientInfo;

    public URL getWellKnownURL() {
        return wellKnownURL;
    }
}
