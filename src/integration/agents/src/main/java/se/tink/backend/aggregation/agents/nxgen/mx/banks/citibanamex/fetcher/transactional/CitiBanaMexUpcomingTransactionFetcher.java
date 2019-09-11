package se.tink.backend.aggregation.agents.nxgen.mx.banks.citibanamex.fetcher.transactional;

import java.util.Collection;
import java.util.Collections;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.UpcomingTransactionFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.UpcomingTransaction;

public class CitiBanaMexUpcomingTransactionFetcher
        implements UpcomingTransactionFetcher<TransactionalAccount> {

    @Override
    public Collection<UpcomingTransaction> fetchUpcomingTransactionsFor(
            TransactionalAccount account) {
        return Collections.emptyList();
    }
}
