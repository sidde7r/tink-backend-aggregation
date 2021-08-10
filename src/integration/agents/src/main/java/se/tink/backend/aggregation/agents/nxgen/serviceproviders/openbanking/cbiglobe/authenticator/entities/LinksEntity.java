package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class LinksEntity {

    @JsonAlias({"scaRedirect", "scaOAuth", "selectAuthenticationMethod"})
    private LinkDetailsEntity authorizeUrl;
}
