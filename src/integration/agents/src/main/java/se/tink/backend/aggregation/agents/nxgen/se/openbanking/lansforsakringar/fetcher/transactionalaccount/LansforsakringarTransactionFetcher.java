package se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.fetcher.transactionalaccount;

import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.LansforsakringarApiClient;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.Account;

public class LansforsakringarTransactionFetcher<T extends Account>
        implements TransactionKeyPaginator<T, String> {

    private final LansforsakringarApiClient apiClient;
    private final LocalDateTimeSource localDateTimeSource;

    public LansforsakringarTransactionFetcher(
            LansforsakringarApiClient apiClient, LocalDateTimeSource localDateTimeSource) {
        this.apiClient = apiClient;
        this.localDateTimeSource = localDateTimeSource;
    }

    @Override
    public TransactionKeyPaginatorResponse<String> getTransactionsFor(T account, String key) {
        return Optional.ofNullable(key)
                .map(apiClient::getTransactionsForKey)
                .orElseGet(
                        () ->
                                apiClient.getTransactionsForAccount(
                                        account.getApiIdentifier(), localDateTimeSource));
    }
}
