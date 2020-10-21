package se.tink.backend.aggregation.agents.nxgen.de.openbanking.postbank.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.postbank.authenticator.entities.Links;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class ConsentResponse {

    @JsonProperty("_links")
    private Links links;

    private String consentId;
    private String consentStatus;
}
