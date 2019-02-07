package se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.transactionalaccount;

import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.LaCaixaApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.LaCaixaConstants;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginator;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class LaCaixaTransactionFetcher implements TransactionPagePaginator<TransactionalAccount> {

    private final LaCaixaApiClient bankClient;

    public LaCaixaTransactionFetcher(LaCaixaApiClient bankClient) {
        this.bankClient = bankClient;
    }

    @Override
    public PaginatorResponse getTransactionsFor(TransactionalAccount account, int page) {

        String accountReference = account.getFromTemporaryStorage(LaCaixaConstants.TemporaryStorage.ACCOUNT_REFERENCE);

        return bankClient.fetchNextAccountTransactions(accountReference,page == 0);
    }
}
