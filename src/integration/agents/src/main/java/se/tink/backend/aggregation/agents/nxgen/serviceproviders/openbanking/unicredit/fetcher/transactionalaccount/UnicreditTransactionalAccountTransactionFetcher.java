package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.fetcher.transactionalaccount;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.UnicreditBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.fetcher.transactionalaccount.rpc.TransactionsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.TransactionPaginationHelper;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;

@RequiredArgsConstructor
public class UnicreditTransactionalAccountTransactionFetcher
        implements TransactionFetcher<TransactionalAccount> {

    private final UnicreditBaseApiClient apiClient;
    private final TransactionPaginationHelper transactionPaginationHelper;
    private final UnicreditTransactionsDateFromChooser unicreditTransactionsDateFromChooser;

    @Override
    public List<AggregationTransaction> fetchTransactionsFor(TransactionalAccount account) {
        Optional<LocalDate> dateOfLastFetchedTransactions =
                transactionPaginationHelper
                        .getTransactionDateLimit(account)
                        .map(date -> Optional.of(toLocaDate(date)))
                        .orElse(Optional.empty());
        LocalDate dateFrom =
                unicreditTransactionsDateFromChooser.getDateFrom(dateOfLastFetchedTransactions);
        TransactionsResponse transactionsResponse = apiClient.getTransactionsFor(account, dateFrom);
        List<AggregationTransaction> transactions =
                new ArrayList<>(transactionsResponse.getTinkTransactions());

        while (transactionsResponse.nextKey() != null) {
            transactionsResponse =
                    apiClient.getTransactionsForNextUrl(transactionsResponse.nextKey());
            transactions.addAll(transactionsResponse.getTinkTransactions());
        }
        return transactions;
    }

    private LocalDate toLocaDate(Date date) {
        return new java.sql.Date(date.getTime()).toLocalDate();
    }
}
