package se.tink.backend.aggregation.agents.nxgen.it.banks.isp.fetcher;

import java.time.LocalDate;
import java.util.Collections;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.it.banks.isp.apiclient.IspApiClient;
import se.tink.backend.aggregation.agents.nxgen.it.banks.isp.fetcher.entity.TransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.it.banks.isp.fetcher.rpc.TransactionsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginator;
import se.tink.backend.aggregation.nxgen.core.account.Account;

@RequiredArgsConstructor
public class TransactionFetcher implements TransactionPagePaginator {

    private final IspApiClient apiClient;

    @Override
    public PaginatorResponse getTransactionsFor(Account account, int page) {
        LocalDate now = LocalDate.now();
        TransactionsResponse transactionsResponse;

        transactionsResponse = apiClient.fetchTransactions(account.getApiIdentifier(), now, page);
        if (transactionsResponse.getPayload().getTransactions().isEmpty()) {
            return PaginatorResponseImpl.create(Collections.emptyList(), false);
        }
        return PaginatorResponseImpl.create(
                transactionsResponse.getPayload().getTransactions().stream()
                        .map(TransactionEntity::toTinkTransaction)
                        .collect(Collectors.toList()),
                true);
    }
}
