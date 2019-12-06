package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintechblocks.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ClaimsInfoEntity {

    @JsonProperty("openbanking_intent_id")
    private OpenbankingIntentIdEntity openbankingIntentId;

    public ClaimsInfoEntity(OpenbankingIntentIdEntity openbankingIntentId) {
        this.openbankingIntentId = openbankingIntentId;
    }
}
