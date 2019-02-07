package se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.accounts.checking;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.JyskeApiClient;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.accounts.checking.entities.TransactionsEntity;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.accounts.checking.rpc.GetTransactionsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.UpcomingTransactionFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginator;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.UpcomingTransaction;

public class JyskeTransactionFetcher implements TransactionPagePaginator<TransactionalAccount>,
        UpcomingTransactionFetcher<TransactionalAccount> {

    private JyskeApiClient apiClient;

    public JyskeTransactionFetcher(JyskeApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public PaginatorResponse getTransactionsFor(TransactionalAccount account, int page) {
        return apiClient.fetchTransactions(account, page);
    }

    @Override
    public Collection<UpcomingTransaction> fetchUpcomingTransactionsFor(TransactionalAccount account) {
        GetTransactionsResponse response = apiClient.fetchFutureTransactions(account);
        if (response.getNumOfTransactions() > 0) {
            return response.getLstTransactions().stream()
                    .map(TransactionsEntity::toTinkUpcomingTransaction)
                    .collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }
}
