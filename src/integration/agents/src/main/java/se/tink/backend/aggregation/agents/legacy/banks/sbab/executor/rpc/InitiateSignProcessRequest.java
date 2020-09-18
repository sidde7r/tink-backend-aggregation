package se.tink.backend.aggregation.agents.banks.sbab.executor.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.agents.banks.sbab.SBABConstants;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class InitiateSignProcessRequest {
    private String authMethod;

    @JsonIgnore
    private InitiateSignProcessRequest() {
        this.authMethod = SBABConstants.BankId.BANKID_AUTH_METHOD;
    }

    @JsonIgnore
    public static InitiateSignProcessRequest create() {
        return new InitiateSignProcessRequest();
    }
}
