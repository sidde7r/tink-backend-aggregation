package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.jwt.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class UserinfoEntity {
    @JsonProperty("openbanking_intent_id")
    private OpenbankingIntentIdEntity openbankingIntentId;

    public UserinfoEntity(String intentId) {
        this.openbankingIntentId = new OpenbankingIntentIdEntity(intentId, true);
    }
}
