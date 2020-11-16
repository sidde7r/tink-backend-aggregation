package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.security.PublicKey;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class JsonWebKeySet {
    private List<JsonWebKey> keys;

    @JsonIgnore
    public List<PublicKey> getAllKeys() {
        return keys.stream().map(JsonWebKey::getPublicKey).collect(Collectors.toList());
    }

    @JsonIgnore
    public Map<String, PublicKey> getAllKeysMap() {
        return keys.stream()
                .collect(Collectors.toMap(JsonWebKey::getKeyId, JsonWebKey::getPublicKey));
    }
}
