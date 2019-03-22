package se.tink.backend.aggregation.agents.nxgen.at.banks.erstebank.fetcher.transactional;

import com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.nxgen.at.banks.erstebank.ErsteBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.at.banks.erstebank.ErsteBankConstants;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginator;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class ErsteBankTransactionFetcher implements TransactionPagePaginator<TransactionalAccount> {

    private final ErsteBankApiClient ersteBankApiClient;

    public ErsteBankTransactionFetcher(ErsteBankApiClient ersteBankApiClient) {
        this.ersteBankApiClient = ersteBankApiClient;
    }

    @Override
    public PaginatorResponse getTransactionsFor(TransactionalAccount account, int page) {
        String accountUrl =
                account.getFromTemporaryStorage(ErsteBankConstants.STORAGE.TRANSACTIONSURL);

        if (!Strings.isNullOrEmpty(accountUrl)) {
            return this.ersteBankApiClient.fetchTransactions(page, accountUrl);
        }
        return PaginatorResponseImpl.createEmpty();
    }
}
