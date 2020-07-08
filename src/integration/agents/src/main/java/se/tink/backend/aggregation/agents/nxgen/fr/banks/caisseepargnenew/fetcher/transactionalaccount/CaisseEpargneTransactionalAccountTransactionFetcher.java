package se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargnenew.fetcher.transactionalaccount;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Optional;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

public class CaisseEpargneTransactionalAccountTransactionFetcher
        implements TransactionDatePaginator<TransactionalAccount> {

    @Override
    public PaginatorResponse getTransactionsFor(
            TransactionalAccount account, Date fromDate, Date toDate) {
        return new PaginatorResponse() {
            @Override
            public Collection<? extends Transaction> getTinkTransactions() {
                return Collections.EMPTY_LIST;
            }

            @Override
            public Optional<Boolean> canFetchMore() {
                return Optional.empty();
            }
        };
    }
}
