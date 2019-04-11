package se.tink.backend.aggregation.agents.nxgen.es.banks.popular.fetcher;

import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.es.banks.popular.BancoPopularApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.popular.BancoPopularPersistentStorage;
import se.tink.backend.aggregation.agents.nxgen.es.banks.popular.entities.BancoPopularContract;
import se.tink.backend.aggregation.agents.nxgen.es.banks.popular.fetcher.rpc.FetchTransactionsRequest;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class BancoPopularTransactionFetcher
        implements TransactionDatePaginator<TransactionalAccount> {

    private final BancoPopularApiClient bankClient;
    private final BancoPopularPersistentStorage persistentStorage;

    public BancoPopularTransactionFetcher(
            BancoPopularApiClient bankClient, BancoPopularPersistentStorage persistentStorage) {

        this.bankClient = bankClient;
        this.persistentStorage = persistentStorage;
    }

    @Override
    public PaginatorResponse getTransactionsFor(
            TransactionalAccount account, Date fromDate, Date toDate) {
        BancoPopularContract contract = persistentStorage.getLoginContracts().getFirstContract();

        FetchTransactionsRequest fetchTransactionsRequest =
                FetchTransactionsRequest.build(contract, account, fromDate, toDate);

        return bankClient.fetchTransactions(fetchTransactionsRequest);
    }
}
