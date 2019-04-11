package se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.creditcards;

import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.SabadellApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.SabadellConstants;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.creditcards.entities.CreditCardEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginator;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;

public class SabadellCreditCardTransactionFetcher
        implements TransactionPagePaginator<CreditCardAccount> {
    private final SabadellApiClient apiClient;

    public SabadellCreditCardTransactionFetcher(SabadellApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public PaginatorResponse getTransactionsFor(CreditCardAccount account, int page) {
        CreditCardEntity creditCardEntity =
                account.getFromTemporaryStorage(account.getBankIdentifier(), CreditCardEntity.class)
                        .orElseThrow(() -> new IllegalStateException("No account entity provided"));

        return apiClient.fetchCreditCardTransactions(
                creditCardEntity, getTotalItemsFetched(page), page);
    }

    private int getTotalItemsFetched(int page) {
        return SabadellConstants.CreditCardTransactionsRequest.ITEMS_PER_PAGE * (page - 1);
    }
}
