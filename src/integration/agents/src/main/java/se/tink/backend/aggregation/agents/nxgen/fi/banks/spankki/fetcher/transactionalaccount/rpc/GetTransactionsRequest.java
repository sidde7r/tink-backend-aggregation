package se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.fetcher.transactionalaccount.rpc;

import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.rpc.SpankkiRequest;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class GetTransactionsRequest extends SpankkiRequest {
    private String toDate;
    private String accountId;
    private String fromDate;

    public GetTransactionsRequest setToDate(String toDate) {
        this.toDate = toDate;
        return this;
    }

    public GetTransactionsRequest setAccountId(String accountId) {
        this.accountId = accountId;
        return this;
    }

    public GetTransactionsRequest setFromDate(String fromDate) {
        this.fromDate = fromDate;
        return this;
    }
}
