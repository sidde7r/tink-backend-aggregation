package se.tink.backend.aggregation.agents.nxgen.be.banks.argenta.fetcher.transactional.rpc;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.be.banks.argenta.fetcher.transactional.entity.ArgentaTransaction;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ArgentaTransactionResponse {
    private int page;
    private int nextPage;
    private List<ArgentaTransaction> transactions;

    public int getPage() {
        return page;
    }

    public int getNextPage() {
        return nextPage;
    }

    public List<ArgentaTransaction> getTransactions() {
        return transactions;
    }
}
