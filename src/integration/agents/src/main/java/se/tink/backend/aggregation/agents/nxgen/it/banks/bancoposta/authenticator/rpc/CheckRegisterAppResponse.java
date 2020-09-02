package se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class CheckRegisterAppResponse {
    @JsonProperty("command-result")
    private CommandResult commandResult;

    @Getter
    @JsonObject
    public static class CommandResult {
        private boolean valid;
    }
}
