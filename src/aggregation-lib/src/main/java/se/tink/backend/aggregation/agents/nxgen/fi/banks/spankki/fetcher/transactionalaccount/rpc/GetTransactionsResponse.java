package se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.fetcher.transactionalaccount.rpc;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.fetcher.transactionalaccount.entities.TransactionsEntity;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.rpc.SpankkiResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class GetTransactionsResponse extends SpankkiResponse {
    private List<TransactionsEntity> transactions;

    public List<TransactionsEntity> getTransactions() {
        return transactions;
    }
}
