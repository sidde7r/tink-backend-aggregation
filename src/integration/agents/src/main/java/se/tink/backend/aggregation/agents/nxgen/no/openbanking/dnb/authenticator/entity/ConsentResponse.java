package se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.authenticator.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.DnbConstants.ErrorMessages;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ConsentResponse {

    @JsonProperty("_links")
    private ConsentLinks links;

    private String consentId;

    public ConsentLinks getConsentLinks() {
        return links;
    }

    public String getConsentId() {
        return consentId;
    }

    public String getScaRedirectLink() {
        return Optional.ofNullable(getConsentLinks())
                .map(ConsentLinks::getScaRedirect)
                .map(SCARedirect::getHref)
                .orElseThrow(
                        () -> new IllegalStateException(ErrorMessages.SCA_REDIRECT_LINK_MISSING));
    }
}
