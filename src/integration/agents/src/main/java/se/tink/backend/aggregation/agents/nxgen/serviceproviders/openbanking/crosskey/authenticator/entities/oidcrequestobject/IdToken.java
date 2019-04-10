package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.authenticator.entities.oidcrequestobject;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class IdToken {

    @JsonProperty("openbanking_intent_id")
    private OpenbankingIntentId openbankingIntentId;

    public IdToken(String value, Boolean essential) {
        openbankingIntentId = new OpenbankingIntentId(value, essential);
    }
}
