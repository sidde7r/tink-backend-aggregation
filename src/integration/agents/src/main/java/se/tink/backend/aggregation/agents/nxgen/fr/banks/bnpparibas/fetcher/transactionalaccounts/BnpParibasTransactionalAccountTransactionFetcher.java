package se.tink.backend.aggregation.agents.nxgen.fr.banks.bnpparibas.fetcher.transactionalaccounts;

import java.util.Collection;
import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.bnpparibas.BnpParibasApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.bnpparibas.BnpParibasConstants;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

public class BnpParibasTransactionalAccountTransactionFetcher
        implements TransactionDatePaginator<TransactionalAccount> {
    private final BnpParibasApiClient apiClient;

    public BnpParibasTransactionalAccountTransactionFetcher(BnpParibasApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public PaginatorResponse getTransactionsFor(
            TransactionalAccount account, Date fromDate, Date toDate) {
        String ibanKey = account.getFromTemporaryStorage(BnpParibasConstants.Storage.IBAN_KEY);

        Collection<? extends Transaction> transactions =
                apiClient
                        .getTransactionalAccountTransactions(fromDate, toDate, ibanKey)
                        .toTinkTransactions();

        return PaginatorResponseImpl.create(transactions);
    }
}
