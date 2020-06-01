package se.tink.backend.aggregation.agents.nxgen.se.banks.collector.fetcher.transactionalaccount;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.se.banks.collector.CollectorConstants;
import se.tink.backend.aggregation.agents.nxgen.se.banks.collector.fetcher.transactionalaccount.entities.TransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.collector.fetcher.transactionalaccount.rpc.SavingsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.TransactionPaginator;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class CollectorTransactionFetcher implements TransactionPaginator<TransactionalAccount> {

    private final SessionStorage sessionStorage;

    public CollectorTransactionFetcher(SessionStorage sessionStorage) {
        this.sessionStorage = sessionStorage;
    }

    @Override
    public void resetState() {}

    @Override
    public PaginatorResponse fetchTransactionsFor(TransactionalAccount account) {
        final List<Transaction> tinkTransaction =
                sessionStorage
                        .get(
                                CollectorConstants.Storage.TRANSACTIONS.concat(
                                        account.getAccountNumber()),
                                SavingsResponse.class)
                        .map(SavingsResponse::getTransactions).orElse(Collections.emptyList())
                        .stream()
                        .map(TransactionEntity::toTinkTransaction)
                        .collect(Collectors.toList());

        return PaginatorResponseImpl.create(tinkTransaction, false);
    }
}
