package se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.authenticator;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;
import se.tink.backend.aggregation.annotations.JsonObject;

@Data
@JsonObject
@Accessors(chain = true)
class JwkHeader {
    private static final String DEFAULT_JWT_SIGNING_ALG = "RS256";
    private static final String DEFAULT_KEY_TYPE = "RSA";

    private String alg = DEFAULT_JWT_SIGNING_ALG;
    private Jwk jwk;

    @Data
    @JsonObject
    @Accessors(chain = true)
    static class Jwk {

        @JsonProperty("kty")
        private String keyType = DEFAULT_KEY_TYPE;

        @JsonProperty("n")
        private String modulus;

        @JsonProperty("e")
        private String exponent;

        @JsonInclude(Include.NON_NULL)
        private String kid;
    }
}
