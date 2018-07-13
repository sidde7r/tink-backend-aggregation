package se.tink.backend.aggregation.agents.nxgen.fi.banks.omasp.fetcher.transactionalaccount.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransactionsRequest {
    private String accountId;

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }
}
