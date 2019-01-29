package se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.fetcher.transactionalaccount;

import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.NordeaFIApiClient;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.index.TransactionIndexPaginator;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class NordeaTransactionFetcher implements TransactionIndexPaginator<TransactionalAccount> {
    private static final Logger LOG = LoggerFactory.getLogger(NordeaTransactionFetcher.class);
    private static final long TRANSACTION_FETCHER_BACKOFF = 2500;
    private static final int MAX_RETRY_ATTEMPTS = 2;
    private final NordeaFIApiClient apiClient;
    private final SessionStorage sessionStorage;

    public NordeaTransactionFetcher(
            NordeaFIApiClient apiClient, SessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public PaginatorResponse getTransactionsFor(TransactionalAccount account, int numberOfTransactions,
            int startIndex) {
        return fetchTransactions(account, numberOfTransactions, startIndex, 1);
    }

    private PaginatorResponse fetchTransactions(TransactionalAccount account,
            int numberOfTransactions,
            int startIndex,
            int attempt) {
        try {
            return apiClient
                    .fetchTransactions(startIndex, numberOfTransactions, account.getBankIdentifier());
        } catch (HttpResponseException hre) {
            return fetchWithBackoffAndRetry(hre, account, numberOfTransactions, startIndex, attempt);
        }
    }

    private PaginatorResponse fetchWithBackoffAndRetry(HttpResponseException hre,
            TransactionalAccount account,
            int numberOfTransactions,
            int startIndex,
            int attempt) {

        if (hre.getResponse().getStatus() == HttpStatus.SC_INTERNAL_SERVER_ERROR) {
            if (attempt <= MAX_RETRY_ATTEMPTS) {
                backoffAWhile();
                LOG.debug(String.format(
                        "Retry [%d] fetch transactions account[%s], offset[%d], numTrans[%d] after backoff ",
                        attempt, account.getAccountNumber(), startIndex, numberOfTransactions
                ));

                return fetchTransactions(account, numberOfTransactions, startIndex, ++attempt);
            }
        }

        throw hre;
    }

    private void backoffAWhile() {
        try {
            Thread.sleep(TRANSACTION_FETCHER_BACKOFF);
        } catch (InterruptedException e) {
            LOG.debug("Woke up early");
        }
    }
}
