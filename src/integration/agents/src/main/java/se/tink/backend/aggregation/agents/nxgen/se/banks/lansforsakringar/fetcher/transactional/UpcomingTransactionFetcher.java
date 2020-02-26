package se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.transactional;

import java.util.Collection;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.LansforsakringarApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.transactional.entity.UpcomingTransactionsEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.transactional.rpc.FetchUpcomingResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.UpcomingTransaction;

public class UpcomingTransactionFetcher
        implements se.tink.backend.aggregation.nxgen.controllers.refresh.transaction
                        .UpcomingTransactionFetcher<
                TransactionalAccount> {

    private final LansforsakringarApiClient apiClient;

    public UpcomingTransactionFetcher(LansforsakringarApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<UpcomingTransaction> fetchUpcomingTransactionsFor(
            TransactionalAccount account) {
        final String accountNumber = account.getAccountNumber();
        final FetchUpcomingResponse fetchUpcomingResponse =
                apiClient.fetchUpcomingTransactions(accountNumber);

        return fetchUpcomingResponse.getUpcomingTransactions().stream()
                .map(UpcomingTransactionsEntity::toTinkTransaction)
                .collect(Collectors.toList());
    }
}
