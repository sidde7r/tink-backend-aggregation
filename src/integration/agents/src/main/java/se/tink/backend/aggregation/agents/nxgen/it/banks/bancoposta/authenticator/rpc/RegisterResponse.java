package se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Data
public class RegisterResponse {
    @JsonProperty("data")
    private Body data;

    @Data
    public static class Body {
        private String otpSecretKey;

        @JsonProperty("app-uuid")
        private String appUuid;
    }
}
