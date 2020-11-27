package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.fetcher.transactionalaccount;

import com.google.common.collect.Lists;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.UnicreditBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.fetcher.transactionalaccount.rpc.TransactionsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.TransactionPaginationHelper;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;

public class UnicreditTransactionalAccountTransactionFetcher
        implements TransactionFetcher<TransactionalAccount> {

    private final UnicreditBaseApiClient apiClient;
    private final TransactionPaginationHelper transactionPaginationHelper;

    public UnicreditTransactionalAccountTransactionFetcher(
            UnicreditBaseApiClient apiClient,
            TransactionPaginationHelper transactionPaginationHelper) {
        this.apiClient = apiClient;
        this.transactionPaginationHelper = transactionPaginationHelper;
    }

    @Override
    public List<AggregationTransaction> fetchTransactionsFor(TransactionalAccount account) {
        List<AggregationTransaction> transactions = Lists.newArrayList();

        Optional<Date> dateOfLastFetchedTransactions =
                transactionPaginationHelper.getContentWithRefreshDate(account);

        TransactionsResponse transactionsResponse =
                apiClient.getTransactionsFor(
                        account, dateOfLastFetchedTransactions.orElse(new Date(0)));
        transactions.addAll(transactionsResponse.getTinkTransactions());

        while (transactionsResponse.nextKey() != null) {
            transactionsResponse =
                    apiClient.getTransactionsForNextUrl(transactionsResponse.nextKey());
            transactions.addAll(transactionsResponse.getTinkTransactions());
        }
        return transactions;
    }
}
