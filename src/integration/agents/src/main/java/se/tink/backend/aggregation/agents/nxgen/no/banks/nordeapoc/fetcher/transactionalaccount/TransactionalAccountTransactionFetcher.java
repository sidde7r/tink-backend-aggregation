package se.tink.backend.aggregation.agents.nxgen.no.banks.nordeapoc.fetcher.transactionalaccount;

import static se.tink.backend.aggregation.agents.nxgen.no.banks.nordeapoc.NordeaNoConstants.CAN_FETCH_TRANSACTION;
import static se.tink.backend.aggregation.agents.nxgen.no.banks.nordeapoc.NordeaNoConstants.PRODUCT_CODE;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordeapoc.client.FetcherClient;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordeapoc.fetcher.transactionalaccount.entity.TransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordeapoc.fetcher.transactionalaccount.rpc.TransactionsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.libraries.amount.ExactCurrencyAmount;

@AllArgsConstructor
@Slf4j
public class TransactionalAccountTransactionFetcher
        implements TransactionKeyPaginator<TransactionalAccount, String> {

    private static final long TRANSACTION_FETCHER_BACKOFF = 2500;
    private static final int MAX_RETRY_ATTEMPTS = 2;

    private FetcherClient fetcherClient;

    @Override
    public TransactionKeyPaginatorResponse<String> getTransactionsFor(
            TransactionalAccount account, String key) {
        return canViewTransactionsOnAccount(account)
                ? fetchTransactions(account, key, 1)
                : TransactionKeyPaginatorResponseImpl.createEmpty();
    }

    private boolean canViewTransactionsOnAccount(TransactionalAccount account) {
        Optional<Boolean> canFetchTransactions =
                account.getFromTemporaryStorage(CAN_FETCH_TRANSACTION, Boolean.class);
        return canFetchTransactions.isPresent() && Boolean.TRUE.equals(canFetchTransactions.get());
    }

    private TransactionKeyPaginatorResponse<String> fetchTransactions(
            TransactionalAccount account, String key, int attempt) {
        try {
            TransactionsResponse transactionsResponse =
                    fetcherClient.fetchAccountTransactions(
                            account.getApiIdentifier(),
                            account.getFromTemporaryStorage(PRODUCT_CODE),
                            key);
            final String currencyCode = account.getExactBalance().getCurrencyCode();
            List<Transaction> tinkTransactions =
                    transactionsResponse.getTransactions().stream()
                            .map(x -> toTinkTransaction(x, currencyCode))
                            .collect(Collectors.toList());
            return new TransactionKeyPaginatorResponseImpl<>(
                    tinkTransactions, transactionsResponse.getContinuationKey());
        } catch (HttpResponseException hre) {
            return fetchWithBackoffAndRetry(hre, account, key, attempt);
        }
    }

    private Transaction toTinkTransaction(TransactionEntity transaction, String currency) {
        return Transaction.builder()
                .setAmount(ExactCurrencyAmount.of(transaction.getAmount(), currency))
                .setDescription(
                        ObjectUtils.firstNonNull(
                                transaction.getMessage(), transaction.getDescription()))
                .setPending(!transaction.isBooked())
                .setDate(transaction.getBookingDate())
                .build();
    }

    private TransactionKeyPaginatorResponse<String> fetchWithBackoffAndRetry(
            HttpResponseException hre, TransactionalAccount account, String key, int attempt) {

        if ((hre.getResponse().getStatus() == 500 || hre.getResponse().getStatus() == 504)
                && attempt <= MAX_RETRY_ATTEMPTS) {
            backoffAWhile();
            log.debug(
                    String.format(
                            "Retry [%d] fetch transactions account[%s] after backoff ",
                            attempt, account.getAccountNumber()),
                    hre);

            return fetchTransactions(account, key, ++attempt);
        }
        throw hre;
    }

    private void backoffAWhile() {
        try {
            Thread.sleep(TRANSACTION_FETCHER_BACKOFF);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.debug("Woke up early", e);
        }
    }
}
