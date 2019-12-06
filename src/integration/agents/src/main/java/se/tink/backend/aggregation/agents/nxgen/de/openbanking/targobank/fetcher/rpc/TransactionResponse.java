package se.tink.backend.aggregation.agents.nxgen.de.openbanking.targobank.fetcher.rpc;

import java.util.Collection;
import java.util.Optional;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
// Not implemented because endpoint for Transactions doesnt work
public class TransactionResponse implements TransactionKeyPaginatorResponse<String> {

    @Override
    public Collection<? extends Transaction> getTinkTransactions() {
        return null;
    }

    @Override
    public Optional<Boolean> canFetchMore() {
        return Optional.of(false);
    }

    @Override
    public String nextKey() {
        return null;
    }
}
