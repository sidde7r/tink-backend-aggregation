package se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class InitRegistrationWithDigitalCodeResponse {
    private Body body;

    @Getter
    @JsonObject
    public static class Body {
        @JsonProperty("suggestedAlias")
        private String accountAlias;
    }
}
