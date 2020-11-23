package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.fetcher.transactionalaccount;

import javax.annotation.Nullable;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.SparebankApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.SparebankConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.fetcher.transactionalaccount.entities.TransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.fetcher.transactionalaccount.rpc.TransactionResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

public class SparebankTransactionFetcher
        implements TransactionKeyPaginator<TransactionalAccount, String> {

    private SparebankApiClient apiClient;

    public SparebankTransactionFetcher(final SparebankApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public TransactionKeyPaginatorResponse<String> getTransactionsFor(
            TransactionalAccount account, @Nullable String key) {
        TransactionResponse transactionResponse;
        // ITE-1648 With fetching of entire history this might not be needed, needs observation
        boolean includePending = false;
        try {
            if (key == null) {
                transactionResponse = apiClient.fetchTransactions(account.getApiIdentifier());
                includePending = true;
            } else {
                transactionResponse = apiClient.fetchNextTransactions(key);
            }
        } catch (HttpResponseException e) {
            String exceptionMessage = e.getResponse().getBody(String.class);
            if (exceptionMessage.contains(SparebankConstants.TransactionsResponse.ERROR_MESSAGE)) {
                return TransactionKeyPaginatorResponseImpl.createEmpty();
            }
            throw e;
        }
        TransactionEntity transactions = transactionResponse.getTransactions();

        return new TransactionKeyPaginatorResponseImpl<>(
                transactions.toTinkTransactions(includePending),
                transactions.getNext().orElse(null));
    }
}
