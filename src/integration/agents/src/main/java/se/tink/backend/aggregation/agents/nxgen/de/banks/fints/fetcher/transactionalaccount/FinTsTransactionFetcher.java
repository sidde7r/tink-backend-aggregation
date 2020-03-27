package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.fetcher.transactionalaccount;

import java.util.List;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;
import se.tink.backend.aggregation.nxgen.exceptions.NotImplementedException;

public class FinTsTransactionFetcher implements TransactionFetcher<TransactionalAccount> {

    public FinTsTransactionFetcher() {}

    @Override
    public List<AggregationTransaction> fetchTransactionsFor(
            TransactionalAccount transactionalAccount) {
        throw new NotImplementedException("Will be covered in next PR");
    }
}
