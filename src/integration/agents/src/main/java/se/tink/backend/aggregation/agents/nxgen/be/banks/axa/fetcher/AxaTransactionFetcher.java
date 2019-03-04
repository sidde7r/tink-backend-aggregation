package se.tink.backend.aggregation.agents.nxgen.be.banks.axa.fetcher;

import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;

import java.util.Collections;
import java.util.List;

public final class AxaTransactionFetcher implements TransactionFetcher<TransactionalAccount> {
    @Override
    public List<AggregationTransaction> fetchTransactionsFor(TransactionalAccount account) {
        return Collections.emptyList();
    }
}
