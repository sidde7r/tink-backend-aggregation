package se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
@Data
public class RegistrationWithDigitalCodeResponse {
    private Header header;

    @Getter
    @JsonObject
    public static class Header {
        @JsonProperty("command-result-description")
        private String commandResultDescription;
    }

    private Body body;

    @Getter
    @JsonObject
    public static class Body {
        private String registerToken;
    }
}
