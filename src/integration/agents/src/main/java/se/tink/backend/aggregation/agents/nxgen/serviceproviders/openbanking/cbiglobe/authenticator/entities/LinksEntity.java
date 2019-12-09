package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.entities;

import java.util.Optional;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LinksEntity {
    private LinkDetailsEntity scaRedirect;
    private LinkDetailsEntity scaOAuth;
    private LinkDetailsEntity self;

    public LinkDetailsEntity getAuthorizeUrl() {
        return Optional.ofNullable(scaRedirect).orElse(scaOAuth);
    }
}
