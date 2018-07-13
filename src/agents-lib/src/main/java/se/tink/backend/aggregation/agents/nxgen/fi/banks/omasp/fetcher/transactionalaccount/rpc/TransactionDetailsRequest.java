package se.tink.backend.aggregation.agents.nxgen.fi.banks.omasp.fetcher.transactionalaccount.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransactionDetailsRequest {
    @JsonProperty("Accept")
    private String accept;
    private String accountId;
    @JsonProperty("Authorization")
    private String authorization;

    public TransactionDetailsRequest setAccept(String accept) {
        this.accept = accept;
        return this;
    }

    public TransactionDetailsRequest setAccountId(String accountId) {
        this.accountId = accountId;
        return this;
    }

    public TransactionDetailsRequest setAuthorization(String authorization) {
        this.authorization = authorization;
        return this;
    }
}
