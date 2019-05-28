package se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.authenticator.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.DnbConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.authenticator.rpc.ConsentBaseResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ConsentResponse extends ConsentBaseResponse {

    @JsonProperty("_links")
    private ConsentLinks links;

    public ConsentLinks getLinks() {
        return links;
    }

    public String getScaRedirectLink() {
        return Optional.ofNullable(getLinks())
                .map(ConsentLinks::getScaRedirect)
                .map(SCARedirect::getHref)
                .orElseThrow(
                        () -> new IllegalStateException(ErrorMessages.SCA_REDIRECT_LINK_MISSING));
    }
}
