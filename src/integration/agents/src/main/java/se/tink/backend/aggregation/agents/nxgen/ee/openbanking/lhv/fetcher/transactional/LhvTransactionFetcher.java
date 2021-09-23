package se.tink.backend.aggregation.agents.nxgen.ee.openbanking.lhv.fetcher.transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.ee.openbanking.lhv.LhvApiClient;
import se.tink.backend.aggregation.agents.nxgen.ee.openbanking.lhv.fetcher.transactional.rpc.TransactionsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.TransactionPaginationHelper;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

@RequiredArgsConstructor
public class LhvTransactionFetcher
        implements TransactionKeyPaginator<TransactionalAccount, String> {

    private final LhvApiClient apiClient;
    private final TransactionPaginationHelper paginationHelper;
    private final LocalDate todaysDate;

    public TransactionKeyPaginatorResponse<String> getTransactionsFor(
            TransactionalAccount account, String key) {

        final LocalDate fromDate;
        Optional<Date> certainDate = paginationHelper.getTransactionDateLimit(account);

        if (!certainDate.isPresent()) {
            fromDate = todaysDate.minusDays(90);
        } else {
            fromDate = certainDate.get().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        }

        final TransactionsResponse transactionsResponse =
                apiClient.fetchTransactions(account.getApiIdentifier(), fromDate.toString());

        return new TransactionKeyPaginatorResponseImpl<>(
                transactionsResponse.getTinkTransactions(), null);
    }
}
