package se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.fetchers.transactionalaccount;

import static se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.LunarConstants.QueryParamsValues;

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
        return apiClient
                .fetchTransactions(account.getApiIdentifier(), timestampKey)
                .getTransactions();
    }

    private String getNextKey(List<TransactionEntity> lunarTransactions) {
        if (lunarTransactions.size() < QueryParamsValues.PAGE_SIZE) {
            return null;
        }
        String sort =
                lunarTransactions.stream()
                        .filter(transaction -> transaction.getTimestamp() > 0)
                        .min(Comparator.comparing(BaseResponseEntity::getTimestamp))
                        .map(transaction -> String.valueOf(transaction.getTimestamp()))
                        .orElse(null);
        log.info("[Lunar] Next key for transactions: {}", sort);
        return sort;
    }

    private List<Transaction> toTinkTransactions(List<TransactionEntity> lunarTransactions) {
        return lunarTransactions.stream()
                .filter(BaseResponseEntity::notDeleted)
                .map(TransactionEntity::toTinkTransaction)
                .collect(Collectors.toList());
    }
}
