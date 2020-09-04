package se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@AllArgsConstructor
public class SignedChallenge {
    private String jti;

    @JsonProperty("sigtype")
    private String signatureType;

    private String signature;
}
