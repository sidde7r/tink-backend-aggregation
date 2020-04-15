package se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.fetcher.transactionalaccount;

import com.google.api.client.http.HttpStatusCodes;
import com.google.common.base.Predicates;
import java.util.Collection;
import java.util.stream.Collectors;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.NordeaSEApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.fetcher.einvoice.entities.PaymentEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.UpcomingTransactionFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.index.TransactionIndexPaginator;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.UpcomingTransaction;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

public class NordeaTransactionFetcher
        implements TransactionIndexPaginator<TransactionalAccount>,
                UpcomingTransactionFetcher<TransactionalAccount> {
    private static final Logger LOG = LoggerFactory.getLogger(NordeaTransactionFetcher.class);

    private static final long TRANSACTION_FETCHER_BACKOFF = 2500;
    private static final int MAX_RETRY_ATTEMPTS = 2;
    private static final int GOOD_ENOUGH_NUMBER_OF_TRANSACTIONS = 500;

    private final NordeaSEApiClient apiClient;

    public NordeaTransactionFetcher(NordeaSEApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public PaginatorResponse getTransactionsFor(
            TransactionalAccount account, int numberOfTransactions, int startIndex) {
        return fetchTransactions(account, numberOfTransactions, startIndex, 1);
    }

    @Override
    public Collection<UpcomingTransaction> fetchUpcomingTransactionsFor(
            TransactionalAccount account) {
        try {
            return apiClient.fetchPayments().getPayments().stream()
                    .filter(Predicates.or(PaymentEntity::isConfirmed, PaymentEntity::isInProgress))
                    .filter(
                            paymentEntity ->
                                    paymentEntity.getFrom().equals(account.getAccountNumber()))
                    .map(PaymentEntity::toUpcomingTransaction)
                    .collect(Collectors.toList());
        } catch (HttpResponseException e) {
            if (e.getResponse().getStatus() == HttpStatusCodes.STATUS_CODE_SERVER_ERROR) {
                throw BankServiceError.BANK_SIDE_FAILURE.exception();
            }
            throw e;
        }
    }

    private PaginatorResponse fetchTransactions(
            TransactionalAccount account, int numberOfTransactions, int startIndex, int attempt) {
        try {
            return apiClient.fetchAccountTransactions(
                    startIndex, numberOfTransactions, account.getApiIdentifier());
        } catch (HttpResponseException | BankServiceException e) {
            return fetchWithBackoffAndRetry(e, account, numberOfTransactions, startIndex, attempt);
        }
    }

    private PaginatorResponse fetchWithBackoffAndRetry(
            RuntimeException e,
            TransactionalAccount account,
            int numberOfTransactions,
            int startIndex,
            int attempt) {

        validateIsBankSideErrorOrElseThrow(e);

        if (attempt <= MAX_RETRY_ATTEMPTS) {
            backoffAWhile();
            LOG.debug(
                    String.format(
                            "Retry [%d] fetch transactions account[%s], offset[%d], numTrans[%d] after backoff ",
                            attempt, account.getAccountNumber(), startIndex, numberOfTransactions),
                    e);

            return fetchTransactions(account, numberOfTransactions, startIndex, ++attempt);
        }
        // this is an ugly fix for Nordea since they tend to throw INTERNAL SERVER ERROR after
        // 500 tx fetched
        if (startIndex > GOOD_ENOUGH_NUMBER_OF_TRANSACTIONS) {
            return PaginatorResponseImpl.createEmpty(false);
        }

        throw e;
    }

    private boolean validateIsBankSideErrorOrElseThrow(RuntimeException e) {
        if (e instanceof BankServiceException) {
            return true;
        }
        if (e instanceof HttpResponseException) {
            HttpResponse response = ((HttpResponseException) e).getResponse();
            if (response.getStatus() == HttpStatus.SC_GATEWAY_TIMEOUT
                    || response.getStatus() == HttpStatus.SC_INTERNAL_SERVER_ERROR) {
                return true;
            }
        }
        throw e;
    }

    private void backoffAWhile() {
        try {
            Thread.sleep(TRANSACTION_FETCHER_BACKOFF);
        } catch (InterruptedException e) {
            LOG.debug("Woke up early", e);
        }
    }
}
