package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.creditcard;

import com.google.common.base.Strings;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaConstants;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.transactionalaccount.rpc.FetchProductsResponse;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcher;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class BbvaCreditCardFetcher  implements AccountFetcher<CreditCardAccount>,
        TransactionFetcher<CreditCardAccount> {
    private static final AggregationLogger LOGGER = new AggregationLogger(BbvaCreditCardFetcher.class);

    private final BbvaApiClient apiClient;

    public BbvaCreditCardFetcher(BbvaApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<CreditCardAccount> fetchAccounts() {
        FetchProductsResponse productsResponse = apiClient.fetchProducts();

        return productsResponse
                .getCards()
                .stream()
                .map(c -> c.toTinkCreditCard())
                .collect(Collectors.toList());
    }

    @Override
    public List<AggregationTransaction> fetchTransactionsFor(CreditCardAccount account) {
        return Collections.emptyList();
    }

    private void logCreditCardData(List<AccountEntity> cards) {
        if (cards == null) {
            return;
        }

        try {
            cards.stream()
                    .filter(AccountEntity::isCreditCard)
                    .forEach(a -> {
                        LOGGER.infoExtraLong(SerializationUtils.serializeToString(a), BbvaConstants.Logging.CREDIT_CARD);
                        try {
                            String cardTransactionsResponse = apiClient.getCardTransactions(a.getId());
                            if (!Strings.isNullOrEmpty(cardTransactionsResponse)) {
                                LOGGER.infoExtraLong(apiClient.getCardTransactions(a.getId()), BbvaConstants.Logging.CREDIT_CARD);
                            }
                        } catch (Exception e) {
                            LOGGER.warn("Failed to log credit card transactions, " + e.getMessage());
                        }
                    });
        } catch (Exception e) {
            LOGGER.warn("Failed to log credit card, " + e.getMessage());
        }
    }
}
