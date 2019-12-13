package se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.entities;

import java.util.Optional;
import se.tink.backend.aggregation.agents.Href;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.SparkassenConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.http.url.URL;

@JsonObject
public class LinksEntity {
    private Href startAuthorisationWithPsuAuthentication;
    private Href scaOAuth;
    private Href self;
    private Href status;

    public URL getStartAuthorisationWithPsuAuthenticationUrl() {
        return new URL(
                Optional.ofNullable(startAuthorisationWithPsuAuthentication)
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                SparkassenConstants.ErrorMessages
                                                        .MISSING_SCA_AUTHORIZATION_URL))
                        .getHref());
    }
}
