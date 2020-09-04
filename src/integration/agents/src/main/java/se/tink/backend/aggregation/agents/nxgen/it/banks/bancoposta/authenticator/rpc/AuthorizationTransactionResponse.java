package se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.rpc;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import lombok.Setter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
@Setter
@JsonNaming(PropertyNamingStrategy.KebabCaseStrategy.class)
public class AuthorizationTransactionResponse {

    private CommandResult commandResult;

    @JsonObject
    @Getter
    public static class CommandResult {
        private String signature;
    }
}
