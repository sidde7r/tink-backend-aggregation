package se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.fetchers.transactionalaccount;

import static se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.LunarConstants.QueryParamsValues;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.fetchers.client.FetcherApiClient;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.fetchers.transactionalaccount.entities.BaseResponseEntity;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.fetchers.transactionalaccount.entities.FeedEntity;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.fetchers.transactionalaccount.entities.TransactionEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@RequiredArgsConstructor
@Slf4j
public class LunarTransactionFetcher
        implements TransactionKeyPaginator<TransactionalAccount, String> {

    private static final String FUTURE_STATUS = "future";
    private static final List<String> KNOWN_TRANSACTIONS_STATUSES =
            Arrays.asList(FUTURE_STATUS, "authorization", "financial");

    private final FetcherApiClient apiClient;

    @Override
    public TransactionKeyPaginatorResponse<String> getTransactionsFor(
            TransactionalAccount account, String key) {
        if (AccountTypes.SAVINGS == account.getType()) {
            return new TransactionKeyPaginatorResponseImpl<>(getGoalTransactions(account), null);
        }
        List<TransactionEntity> lunarTransactions = getLunarTransactions(account, key);
        logInfoAboutPotentialPendingTransactions(lunarTransactions);
        String nextKey = getNextKey(lunarTransactions);

        return new TransactionKeyPaginatorResponseImpl<>(
                toTinkTransactions(lunarTransactions), nextKey);
    }

    private List<Transaction> getGoalTransactions(TransactionalAccount account) {
        return apiClient.fetchGoalDetails(account.getApiIdentifier()).getFeed().stream()
                .filter(BaseResponseEntity::notDeleted)
                .filter(FeedEntity::containsAmount)
                .map(FeedEntity::toTinkTransaction)
                .collect(Collectors.toList());
    }

    private List<TransactionEntity> getLunarTransactions(
            TransactionalAccount account, String timestampKey) {
        return apiClient
                .fetchTransactions(account.getApiIdentifier(), timestampKey)
                .getTransactions();
    }

    private String getNextKey(List<TransactionEntity> lunarTransactions) {
        if (lunarTransactions.size() < QueryParamsValues.PAGE_SIZE) {
            return null;
        }
        return lunarTransactions.stream()
                .filter(transaction -> transaction.getTimestamp() > 0)
                .min(Comparator.comparing(BaseResponseEntity::getTimestamp))
                .map(transaction -> String.valueOf(transaction.getTimestamp()))
                .orElse(null);
    }

    private List<Transaction> toTinkTransactions(List<TransactionEntity> lunarTransactions) {
        return lunarTransactions.stream()
                .filter(BaseResponseEntity::notDeleted)
                .map(TransactionEntity::toTinkTransaction)
                .collect(Collectors.toList());
    }

    private void logInfoAboutPotentialPendingTransactions(
            List<TransactionEntity> lunarTransactions) {
        // Wiski delete this method after getting more data
        lunarTransactions.stream()
                .filter(transaction -> transaction.getTimestamp() > 0)
                .forEach(
                        transactionEntity -> {
                            logTransactionsWithTimestampHigherThanUpdated(transactionEntity);
                            logUnknownTransactionsStatuses(transactionEntity);
                            logTransactionsWithTimestampInTheFuture(transactionEntity);
                        });
    }

    private void logTransactionsWithTimestampHigherThanUpdated(
            TransactionEntity transactionEntity) {
        if ((transactionEntity.getTimestamp() > transactionEntity.getUpdated())
                && !FUTURE_STATUS.equalsIgnoreCase(transactionEntity.getStatus())) {
            log.info("Possible pending transaction. Status: {}", transactionEntity.getStatus());
        }
    }

    private void logUnknownTransactionsStatuses(TransactionEntity transactionEntity) {
        if (transactionEntity.getStatus() != null
                && !KNOWN_TRANSACTIONS_STATUSES.contains(
                        transactionEntity.getStatus().toLowerCase())) {
            log.info("Found unknown transaction status: {}", transactionEntity.getStatus());
        }
    }

    private void logTransactionsWithTimestampInTheFuture(TransactionEntity transactionEntity) {
        if (!FUTURE_STATUS.equalsIgnoreCase(transactionEntity.getStatus())
                && transactionEntity.getTimestamp() > System.currentTimeMillis()) {
            log.info(
                    "Transaction has a future timestamp and status is: {}",
                    transactionEntity.getStatus());
        }
    }
}
