package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.accounts.checking;

import java.util.Collection;
import java.util.Date;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.BecApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.accounts.checking.entities.RecordEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.rpc.FetchUpcomingPaymentsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.UpcomingTransactionFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.core.transaction.UpcomingTransaction;

public class BecAccountTransactionsFetcher
        implements TransactionDatePaginator<TransactionalAccount>,
                UpcomingTransactionFetcher<TransactionalAccount> {

    private final BecApiClient apiClient;

    public BecAccountTransactionsFetcher(BecApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public PaginatorResponse getTransactionsFor(
            TransactionalAccount account, Date fromDate, Date toDate) {
        Collection<? extends Transaction> transactions =
                Optional.of(
                                this.apiClient
                                        .fetchAccountTransactions(account, fromDate, toDate)
                                        .getRecord())
                        .orElseThrow(() -> new IllegalStateException("No records")).stream()
                        .map(RecordEntity::toTinkTransaction)
                        .collect(Collectors.toList());

        return PaginatorResponseImpl.create(transactions);
    }

    @Override
    public Collection<UpcomingTransaction> fetchUpcomingTransactionsFor(
            TransactionalAccount account) {
        // we are currently not fetching all upcoming payments, only the first batch
        FetchUpcomingPaymentsResponse upcomingPaymentsResponse =
                this.apiClient.fetchAccountUpcomingTransactions(account);
        return upcomingPaymentsResponse.getTinkUpcomingTransactions();
    }
}
