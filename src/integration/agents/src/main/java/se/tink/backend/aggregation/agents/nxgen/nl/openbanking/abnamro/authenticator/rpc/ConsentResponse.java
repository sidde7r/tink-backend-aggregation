package se.tink.backend.aggregation.agents.nxgen.nl.openbanking.abnamro.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ConsentResponse {

    @JsonProperty("iban")
    private String accountId;

    private String transactionId;

    private String scopes;

    @JsonProperty("valid")
    private String expiresIn;

    public String getAccountId() {
        return accountId;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public String getScopes() {
        return scopes;
    }

    public String getExpiresIn() {
        return expiresIn;
    }
}
