package se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v30.fetcher.transactionalaccount;

import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v30.NordeaFiApiClient;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v30.NordeaFiConstants;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v30.fetcher.transactionalaccount.rpc.FetchTransactionsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.index.TransactionIndexPaginator;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;

public class NordeaTransactionFetcher implements TransactionIndexPaginator<TransactionalAccount> {
    private static final Logger LOG = LoggerFactory.getLogger(NordeaTransactionFetcher.class);

    private static final long TRANSACTION_FETCHER_BACKOFF = 1500;
    private final NordeaFiApiClient apiClient;

    public NordeaTransactionFetcher(
            NordeaFiApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public PaginatorResponse getTransactionsFor(TransactionalAccount account, int numberOfTransactions,
            int startIndex) {

        return fetchWithBackoffAndRetry(account, numberOfTransactions, startIndex);
    }

    private PaginatorResponse fetchWithBackoffAndRetry(TransactionalAccount account, int numberOfTransactions, int startIndex) {
        try {
            return fetchTransactions(account, numberOfTransactions, startIndex);
        } catch (HttpResponseException hre) {
            if (hre.getResponse().getStatus() == HttpStatus.SC_INTERNAL_SERVER_ERROR) {
                backoffAWhile();
                LOG.debug(String.format("Retry fetch transactions account[%s], offset[%d], numTrans[%d] after backoff ",
                        account.getAccountNumber(), startIndex, numberOfTransactions));

                return fetchTransactions(account, numberOfTransactions, startIndex);
            }

            throw hre;
        }
    }

    private PaginatorResponse fetchTransactions(TransactionalAccount account, int numberOfTransactions,
            int startIndex) {

        return apiClient
                .fetchTransactions(startIndex, numberOfTransactions, account.getBankIdentifier(),
                        NordeaFiConstants.Products.ACCOUNT, FetchTransactionsResponse.class);
    }

    private void backoffAWhile() {
        try {
            Thread.sleep(TRANSACTION_FETCHER_BACKOFF);
        } catch (InterruptedException e) {
            LOG.debug("Woke up early");
        }
    }
}
