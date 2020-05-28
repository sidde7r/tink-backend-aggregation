package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.authenticator.entities.KeysEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class RSAPayloadResponse {
    @JsonProperty("encrypted_keys")
    private List<KeysEntity> keys;

    public List<KeysEntity> getKeys() {
        return keys;
    }
}
