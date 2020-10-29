package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.authenticator.entity.KeyEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class EnrollmentResponse {
    private String id;
    private KeyEntity key;
    private String nickname;

    @JsonProperty("signing_item_id")
    private String signingItemId;

    private String state;
    private String type;

    @JsonProperty("valid_until")
    private String validUntil;

    public String getId() {
        return id;
    }

    public KeyEntity getKey() {
        return key;
    }

    public String getNickname() {
        return nickname;
    }

    public String getSigningItemId() {
        return signingItemId;
    }

    public String getState() {
        return state;
    }

    public String getType() {
        return type;
    }

    public String getValidUntil() {
        return validUntil;
    }
}
