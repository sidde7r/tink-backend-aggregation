package se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.authenticator;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Data;
import lombok.experimental.Accessors;
import se.tink.backend.aggregation.annotations.JsonObject;

@Data
@JsonObject
@Accessors(chain = true)
class JwkHeader {
    private String alg;
    private Jwk jwk;

    @Data
    @JsonObject
    @Accessors(chain = true)
    static class Jwk {
        private String kty;
        private String n;
        private String e;

        @JsonInclude(Include.NON_NULL)
        private String kid;
    }
}
