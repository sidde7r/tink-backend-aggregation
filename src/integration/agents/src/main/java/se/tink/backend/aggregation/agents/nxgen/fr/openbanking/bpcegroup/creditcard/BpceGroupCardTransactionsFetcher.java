package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.creditcard;

import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.apiclient.BpceGroupApiClient;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginator;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;

@RequiredArgsConstructor
public class BpceGroupCardTransactionsFetcher
        implements TransactionPagePaginator<CreditCardAccount> {

    private final BpceGroupApiClient bpceGroupApiClient;

    @Override
    public PaginatorResponse getTransactionsFor(CreditCardAccount account, int page) {
        final String resourceId = account.getApiIdentifier();

        return (page == 1)
                ? bpceGroupApiClient.getTransactions(resourceId)
                : bpceGroupApiClient.getTransactions(resourceId, page);
    }
}
