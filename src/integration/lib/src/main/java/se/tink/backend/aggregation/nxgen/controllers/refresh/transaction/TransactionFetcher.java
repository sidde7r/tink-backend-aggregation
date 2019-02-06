package se.tink.backend.aggregation.nxgen.controllers.refresh.transaction;

import java.util.List;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;

public interface TransactionFetcher<A extends Account> {
    List<AggregationTransaction> fetchTransactionsFor(A account);
}
