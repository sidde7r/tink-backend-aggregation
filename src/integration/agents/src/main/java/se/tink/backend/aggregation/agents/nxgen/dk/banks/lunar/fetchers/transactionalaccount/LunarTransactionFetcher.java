package se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.fetchers.transactionalaccount;

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
        // For now it is hard to say what is pagination key, because we don't have enough data
        if (AccountTypes.SAVINGS == account.getType()) {
            return new TransactionKeyPaginatorResponseImpl<>(getGoalTransactions(account), null);
        }
        return new TransactionKeyPaginatorResponseImpl<>(getTransactions(account), null);
    }

    private List<Transaction> getGoalTransactions(TransactionalAccount account) {
        return apiClient.fetchGoalDetails(account.getApiIdentifier()).getFeed().stream()
                .filter(BaseResponseEntity::notDeleted)
                .filter(FeedEntity::containsAmount)
                .map(FeedEntity::toTinkTransaction)
                .collect(Collectors.toList());
    }

    private List<Transaction> getTransactions(TransactionalAccount account) {
        // Delete logs when user with 500 or more transactions is found
        List<Transaction> transactions =
                apiClient.fetchTransactions(account.getApiIdentifier()).getTransactions().stream()
                        .filter(BaseResponseEntity::notDeleted)
                        .map(TransactionEntity::toTinkTransaction)
                        .collect(Collectors.toList());

        if (transactions.size() >= 500) {
            log.info(
                    "There is 500 or more transactions in response! Transactions size: {}",
                    transactions.size());
        }

        return transactions;
    }
}
