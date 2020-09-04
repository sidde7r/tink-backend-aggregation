package se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.rpc;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
@JsonNaming(PropertyNamingStrategy.KebabCaseStrategy.class)
public class CheckRegisterAppResponse {
    private CommandResult commandResult;

    @Getter
    @JsonObject
    public static class CommandResult {
        private boolean valid;
    }
}
