package se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.creditcards;

import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.SabadellApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.SabadellConstants;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.creditcards.entities.CreditCardEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.creditcards.rpc.CreditCardTransactionsResponse;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginator;
import se.tink.backend.aggregation.nxgen.core.account.CreditCardAccount;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class SabadellCreditCardTransactionFetcher implements TransactionPagePaginator<CreditCardAccount> {
    private final AggregationLogger log = new AggregationLogger(SabadellCreditCardTransactionFetcher.class);
    private final SabadellApiClient apiClient;

    public SabadellCreditCardTransactionFetcher(SabadellApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public PaginatorResponse getTransactionsFor(CreditCardAccount account, int page) {
        CreditCardEntity creditCardEntity = account
                .getFromTemporaryStorage(account.getBankIdentifier(), CreditCardEntity.class)
                        .orElseThrow(() -> new IllegalStateException("No account entity provided"));

        CreditCardTransactionsResponse creditCardTransactionsResponse = apiClient
                .fetchCreditCardTransactions(creditCardEntity, getTotalItemsFetched(page), page);

        // I stongly suspect that these will look like the movement list for transactional accounts. I don't want
        // to assume since all I have to go on is a null value and same field name, so just logging for now.
        Object movementList = creditCardTransactionsResponse.getGenericGroupedMovement().getPeriodMovementModelList();
        if (movementList != null) {
            log.infoExtraLong(SerializationUtils.serializeToString(movementList),
                    SabadellConstants.Tags.CREDIT_CARD_TRANSACTIONS);
        }

        return creditCardTransactionsResponse;
    }

    private int getTotalItemsFetched(int page) {
        return 20 * (page - 1);
    }
}
