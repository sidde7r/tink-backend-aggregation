package se.tink.backend.aggregation.agents.nxgen.de.banks.fidor.fetcher.transactional;

import java.util.Collection;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fidor.FidorApiClient;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fidor.authenticator.entities.OpenTokenEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.UpcomingTransactionFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginator;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.UpcomingTransaction;

public class FidorTransactionFetcher
        implements TransactionPagePaginator<TransactionalAccount>,
                UpcomingTransactionFetcher<TransactionalAccount> {

    private final FidorApiClient fidorApiClient;

    public FidorTransactionFetcher(FidorApiClient fidorApiClient) {
        this.fidorApiClient = fidorApiClient;
    }

    @Override
    public PaginatorResponse getTransactionsFor(TransactionalAccount account, int page) {
        OpenTokenEntity tokenEntity = this.fidorApiClient.getTokenFromStorage();

        return this.fidorApiClient.fetchOpenApiTransactions(tokenEntity, page);
    }

    @Override
    public Collection<UpcomingTransaction> fetchUpcomingTransactionsFor(
            TransactionalAccount account) {
        OpenTokenEntity tokenEntity = this.fidorApiClient.getTokenFromStorage();

        return this.fidorApiClient.fetchUpcomingTransactions(tokenEntity).toUpcomingTransaction();
    }
}
