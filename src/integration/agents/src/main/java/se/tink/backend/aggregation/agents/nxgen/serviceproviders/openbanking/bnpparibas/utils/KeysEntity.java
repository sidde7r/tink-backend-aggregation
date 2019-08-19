package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.utils;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class KeysEntity {

    @JsonProperty("kty")
    public final String cryptoghraficAlgorithmFamily;

    @JsonProperty("kid")
    public final String keyIdJwks;

    @JsonProperty("x5c")
    public final List<String> x5c;

    public KeysEntity(String cryptoghraficAlgorithmFamily, String keyIdJwks, List<String> x5c) {
        this.cryptoghraficAlgorithmFamily = cryptoghraficAlgorithmFamily;
        this.keyIdJwks = keyIdJwks;
        this.x5c = x5c;
    }
}
