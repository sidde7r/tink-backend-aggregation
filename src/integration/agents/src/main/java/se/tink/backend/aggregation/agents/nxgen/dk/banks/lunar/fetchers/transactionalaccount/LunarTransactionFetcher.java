package se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.fetchers.transactionalaccount;

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

    private final FetcherApiClient apiClient;

    @Override
    public TransactionKeyPaginatorResponse<String> getTransactionsFor(
            TransactionalAccount account, String key) {
        if (AccountTypes.SAVINGS == account.getType()) {
            return new TransactionKeyPaginatorResponseImpl<>(getGoalTransactions(account), null);
        }
        List<TransactionEntity> lunarTransactions = getLunarTransactions(account, key);
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
        // Delete logs when pagination key is found and confirmed
        List<TransactionEntity> transactions =
                apiClient
                        .fetchTransactions(account.getApiIdentifier(), timestampKey)
                        .getTransactions();

        if (transactions.size() >= 200) {
            log.info(
                    "There is 200 or more transactions in response! Transactions size: {}",
                    transactions.size());
        }

        return transactions;
    }

    private String getNextKey(List<TransactionEntity> lunarTransactions) {
        return lunarTransactions.stream()
                .min(Comparator.comparing(BaseResponseEntity::getSort))
                .map(transaction -> String.valueOf(transaction.getSort()))
                .orElse(null);
    }

    private List<Transaction> toTinkTransactions(List<TransactionEntity> lunarTransactions) {
        return lunarTransactions.stream()
                .filter(BaseResponseEntity::notDeleted)
                .map(TransactionEntity::toTinkTransaction)
                .collect(Collectors.toList());
    }
}
