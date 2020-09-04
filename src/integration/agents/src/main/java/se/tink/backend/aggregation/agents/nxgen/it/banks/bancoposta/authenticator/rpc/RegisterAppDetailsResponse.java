package se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Data
public class RegisterAppDetailsResponse {
    @JsonProperty("data")
    private Body data;

    @Data
    public static class Body {
        @JsonProperty("appRegisterID")
        private String appRegisterId;

        @JsonProperty("secretAPP")
        private String secretApp;
    }
}
