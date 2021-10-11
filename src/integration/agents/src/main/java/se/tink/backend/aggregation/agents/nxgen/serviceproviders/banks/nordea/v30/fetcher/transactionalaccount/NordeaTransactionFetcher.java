package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.fetcher.transactionalaccount;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.NordeaBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.NordeaConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.fetcher.transactionalaccount.rpc.FetchAccountTransactionResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

public class NordeaTransactionFetcher implements TransactionDatePaginator<TransactionalAccount> {
    private final NordeaBaseApiClient apiClient;
    private final NordeaConfiguration nordeaConfiguration;
    private Set<String> transactionIdsSeen = new HashSet<>();

    public NordeaTransactionFetcher(
            NordeaBaseApiClient apiClient, NordeaConfiguration nordeaConfiguration) {
        this.apiClient = apiClient;
        this.nordeaConfiguration = nordeaConfiguration;
    }

    @Override
    public PaginatorResponse getTransactionsFor(
            TransactionalAccount account, Date fromDate, Date toDate) {

        List<Transaction> tinkTransactions = new ArrayList<>();
        FetchAccountTransactionResponse transactionsResponse;

        String continuationKey = null;
        do {
            // Nordea returns pending transactions in every response in the same date interval.
            // Parse the pending transactions from the first response and skip the rest.
            boolean skipPendingTransactions = continuationKey != null;

            try {
                transactionsResponse =
                        apiClient.fetchAccountTransactions(
                                account, continuationKey, fromDate, toDate);
                tinkTransactions.addAll(
                        transactionsResponse.toTinkTransactions(
                                nordeaConfiguration, skipPendingTransactions, transactionIdsSeen));

                continuationKey = transactionsResponse.getContinuationKey();
            } catch (Exception e) {
                // if the continuation key crosses a certain value, banks sends an error response
                continuationKey = null;
            }
        } while (continuationKey != null);

        return PaginatorResponseImpl.create(tinkTransactions);
    }
}
