package se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.transactionalaccount;

import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.LaCaixaApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.LaCaixaConstants;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;

public class LaCaixaTransactionFetcher implements TransactionPagePaginator<TransactionalAccount> {

    LaCaixaApiClient bankClient;

    public LaCaixaTransactionFetcher(LaCaixaApiClient bankClient) {
        this.bankClient = bankClient;
    }

    @Override
    public TransactionPagePaginatorResponse getTransactionsFor(TransactionalAccount account, int page) {
        return bankClient.fetchNextAccountTransactions(
                account.getTemporaryStorage(LaCaixaConstants.TemporaryStorage.ACCOUNT_REFERENCE, String.class),
                page == 0);
    }
}
