package se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ChallengeResponse {

    @JsonProperty("jti")
    private String transactionId;

    @JsonProperty("transaction-challenge")
    private String transactionChallenge;

    private String randK;
}
