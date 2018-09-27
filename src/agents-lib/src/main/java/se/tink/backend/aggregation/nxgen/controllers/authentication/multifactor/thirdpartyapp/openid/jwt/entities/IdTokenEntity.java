package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.jwt.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class IdTokenEntity {
    @JsonProperty("openbanking_intent_id")
    private OpenbankingIntentIdEntity openbankingIntentId;
    private AcrEntity acr;

    public IdTokenEntity(String intentId, String... acrValues) {
        this.openbankingIntentId = new OpenbankingIntentIdEntity(intentId, true);
        this.acr = new AcrEntity(true, acrValues);
    }
}
