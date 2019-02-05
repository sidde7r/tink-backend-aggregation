package se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public enum AuthScopeEntity {
    @JsonProperty("account.read")
    ACCOUNT_READ,
    @JsonProperty("account.write")
    ACCOUNT_WRITE,
    @JsonProperty("loan.read")
    LOAN_READ,
    @JsonProperty("account.read loan.read")
    ACCOUNT_LOAN_READ,
}
