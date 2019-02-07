package se.tink.backend.aggregation.nxgen.controllers.refresh.transaction;

import java.util.Collection;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.core.transaction.UpcomingTransaction;

public interface UpcomingTransactionFetcher<A extends Account> {
    Collection<UpcomingTransaction> fetchUpcomingTransactionsFor(A account);
}
