package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.transactionalaccount;

import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.apiclient.BpceGroupApiClient;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginator;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

@RequiredArgsConstructor
public class BpceGroupTransactionFetcher implements TransactionPagePaginator<TransactionalAccount> {

    private final BpceGroupApiClient bpceGroupApiClient;

    @Override
    public PaginatorResponse getTransactionsFor(TransactionalAccount account, int page) {
        final String resourceId = account.getApiIdentifier();

        return (page == 1)
                ? bpceGroupApiClient.getTransactions(resourceId)
                : bpceGroupApiClient.getTransactions(resourceId, page);
    }
}
