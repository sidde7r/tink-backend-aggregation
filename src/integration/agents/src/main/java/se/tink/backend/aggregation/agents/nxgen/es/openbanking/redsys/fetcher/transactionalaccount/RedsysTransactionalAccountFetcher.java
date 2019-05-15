package se.tink.backend.aggregation.agents.nxgen.es.openbanking.redsys.fetcher.transactionalaccount;

import java.util.Collection;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.redsys.RedsysApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.redsys.RedsysConstants;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.redsys.entities.LinkEntity;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.redsys.fetcher.transactionalaccount.rpc.ListAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.redsys.fetcher.transactionalaccount.rpc.TransactionsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

public class RedsysTransactionalAccountFetcher
        implements AccountFetcher<TransactionalAccount>,
                TransactionKeyPaginator<TransactionalAccount, String> {

    private final RedsysApiClient apiClient;

    public RedsysTransactionalAccountFetcher(RedsysApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        ListAccountsResponse accountsResponse = apiClient.fetchAccounts();
        return accountsResponse.toTinkAccounts();
    }

    @Override
    public TransactionKeyPaginatorResponse<String> getTransactionsFor(
            TransactionalAccount account, String fromTransactionId) {
        TransactionsResponse transactionsResponse =
                apiClient.fetchTransactions(account.getApiIdentifier(), fromTransactionId);
        Collection<Transaction> transactions =
                (Collection<Transaction>)
                        transactionsResponse.getTransactions().getTinkTransactions();

        TransactionKeyPaginatorResponseImpl<String> paginatorResponse =
                new TransactionKeyPaginatorResponseImpl<String>();
        paginatorResponse.setTransactions(transactions);
        paginatorResponse.setNext(
                transactionsResponse
                        .getLink(RedsysConstants.Links.NEXT)
                        .map(LinkEntity::getHref)
                        .orElse(null));
        return paginatorResponse;
    }
}
