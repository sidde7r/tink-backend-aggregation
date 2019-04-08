package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.security.PublicKey;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class JsonWebKeySet {
    private List<JsonWebKey> keys;

    @JsonIgnore
    public Optional<PublicKey> getEncryptionKey(String keyId) {
        return keys.stream()
                .filter(jwk -> keyId.equals(jwk.getKeyId()))
                .filter(JsonWebKey::isEncryptionKey)
                .map(JsonWebKey::getPublicKey)
                .findAny();
    }

    @JsonIgnore
    public Optional<PublicKey> getSigningKey(String keyId) {
        return keys.stream()
                .filter(jwk -> keyId.equals(jwk.getKeyId()))
                .filter(JsonWebKey::isSigningKey)
                .map(JsonWebKey::getPublicKey)
                .findAny();
    }

    @JsonIgnore
    public List<PublicKey> getAllKeys() {
        return keys.stream().map(JsonWebKey::getPublicKey).collect(Collectors.toList());
    }
}
