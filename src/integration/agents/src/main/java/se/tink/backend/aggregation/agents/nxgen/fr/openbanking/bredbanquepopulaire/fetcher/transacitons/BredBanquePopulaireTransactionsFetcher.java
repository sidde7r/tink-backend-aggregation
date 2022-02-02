package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bredbanquepopulaire.fetcher.transacitons;

import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bredbanquepopulaire.apiclient.BredBanquePopulaireApiClient;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginator;
import se.tink.backend.aggregation.nxgen.core.account.Account;

@RequiredArgsConstructor
public class BredBanquePopulaireTransactionsFetcher<T extends Account>
        implements TransactionPagePaginator<T> {
    private final BredBanquePopulaireApiClient apiClient;

    @Override
    public PaginatorResponse getTransactionsFor(T account, int page) {
        final String resourceId = account.getApiIdentifier();

        return page == 0
                ? apiClient.getTransactions(resourceId)
                : apiClient.getTransactions(resourceId, page);
    }
}
