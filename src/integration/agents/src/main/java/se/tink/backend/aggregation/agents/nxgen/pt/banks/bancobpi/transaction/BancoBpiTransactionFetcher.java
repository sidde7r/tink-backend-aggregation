package se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.transaction;

import java.util.LinkedList;
import java.util.List;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;

public class BancoBpiTransactionFetcher implements TransactionFetcher<TransactionalAccount> {

    @Override
    public List<AggregationTransaction> fetchTransactionsFor(TransactionalAccount account) {
        return new LinkedList<>();
    }
}
