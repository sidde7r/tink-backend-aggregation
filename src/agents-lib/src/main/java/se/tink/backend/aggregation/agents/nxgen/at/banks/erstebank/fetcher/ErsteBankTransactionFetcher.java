package se.tink.backend.aggregation.agents.nxgen.at.banks.erstebank.fetcher;

import se.tink.backend.aggregation.agents.nxgen.at.banks.erstebank.ErsteBankApiClient;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginator;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;

public class ErsteBankTransactionFetcher implements TransactionPagePaginator<TransactionalAccount> {

    private final ErsteBankApiClient ersteBankApiClient;

    public ErsteBankTransactionFetcher(ErsteBankApiClient ersteBankApiClient){
        this.ersteBankApiClient = ersteBankApiClient;
    }

    @Override
    public PaginatorResponse getTransactionsFor(TransactionalAccount account, int page) {
        return this.ersteBankApiClient.fetchTransactions(page);
    }
}
