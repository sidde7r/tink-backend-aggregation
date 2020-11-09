package se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Optional;
import lombok.Getter;
import se.tink.backend.aggregation.agents.Href;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.DnbConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.authenticator.entity.ConsentLinks;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ConsentResponse {

    @JsonProperty("_links")
    private ConsentLinks links;

    @Getter private String consentId;

    public String getScaRedirectLink() {
        return Optional.ofNullable(links)
                .map(ConsentLinks::getScaRedirect)
                .map(Href::getHref)
                .orElseThrow(
                        () -> new IllegalStateException(ErrorMessages.SCA_REDIRECT_LINK_MISSING));
    }
}
