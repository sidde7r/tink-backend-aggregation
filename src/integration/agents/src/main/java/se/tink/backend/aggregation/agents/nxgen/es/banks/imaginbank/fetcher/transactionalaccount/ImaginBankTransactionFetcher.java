package se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.fetcher.transactionalaccount;

import se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.ImaginBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.ImaginBankConstants;
import se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.fetcher.transactionalaccount.rpc.AccountTransactionResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginator;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class ImaginBankTransactionFetcher
        implements TransactionPagePaginator<TransactionalAccount> {

    private final ImaginBankApiClient apiClient;

    public ImaginBankTransactionFetcher(ImaginBankApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public PaginatorResponse getTransactionsFor(TransactionalAccount account, int page) {

        String accountReference =
                account.getFromTemporaryStorage(
                        ImaginBankConstants.TemporaryStorage.ACCOUNT_REFERENCE);

        AccountTransactionResponse response =
                apiClient.fetchNextAccountTransactions(accountReference, page == 0);

        return response;
    }
}
