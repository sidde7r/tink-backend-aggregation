package se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.fetcher;

import com.google.api.client.http.HttpStatusCodes;
import java.util.Collection;
import java.util.Collections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.HandelsbankenNOApiClient;
import se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.fetcher.rpc.TransactionFetchingResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.index.TransactionIndexPaginator;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;

public class HandelsbankenNOTransactionFetcher implements TransactionIndexPaginator<TransactionalAccount> {

    private final HandelsbankenNOApiClient apiClient;
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    public HandelsbankenNOTransactionFetcher(
            HandelsbankenNOApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<? extends Transaction> getTransactionsFor(TransactionalAccount account, int numberOfTransactions,
            int startIndex) {

        String transactionUrlForAccount = account.getBankIdentifier();
        HttpResponse response = apiClient.fetchTransactions(transactionUrlForAccount, numberOfTransactions, startIndex);
        if (response.getStatus() != HttpStatusCodes.STATUS_CODE_OK) {
            log.warn("Traffic error during fetching transaction: %s: %s", response.getStatus(),
                    response.getBody(String.class));
            return Collections.emptyList();
        }
        TransactionFetchingResponse transactionFetchingResponse = response.getBody(TransactionFetchingResponse.class);
        return transactionFetchingResponse.toTinkTransactions();
    }
}
