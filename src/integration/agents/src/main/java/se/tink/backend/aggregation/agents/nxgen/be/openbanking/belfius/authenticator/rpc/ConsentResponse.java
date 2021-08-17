package se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class ConsentResponse {

    @JsonProperty("consent_uri")
    private String consentUri;
}
