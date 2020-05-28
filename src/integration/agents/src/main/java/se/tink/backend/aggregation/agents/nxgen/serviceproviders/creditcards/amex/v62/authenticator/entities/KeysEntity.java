package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class KeysEntity {
    private String key;
    private String kid;

    @JsonProperty("key_purpose")
    private String keyPurpose;

    public String getKey() {
        return key;
    }

    public String getKid() {
        return kid;
    }

    public String getKeyPurpose() {
        return keyPurpose;
    }
}
