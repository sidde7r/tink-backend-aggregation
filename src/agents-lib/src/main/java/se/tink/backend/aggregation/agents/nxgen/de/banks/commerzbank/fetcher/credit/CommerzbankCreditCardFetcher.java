package se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.fetcher.credit;

import com.google.common.base.Strings;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.CommerzbankApiClient;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.CommerzbankConstants;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.entities.ResultEntity;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.fetcher.account.entities.ProductsEntity;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcher;
import se.tink.backend.aggregation.nxgen.core.account.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;

public class CommerzbankCreditCardFetcher implements AccountFetcher<CreditCardAccount>,
        TransactionFetcher<CreditCardAccount> {

    private final CommerzbankApiClient apiClient;
    private static final AggregationLogger LOGGER = new AggregationLogger(CommerzbankCreditCardFetcher.class);

    public CommerzbankCreditCardFetcher(CommerzbankApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<CreditCardAccount> fetchAccounts() {
        ResultEntity entity = apiClient.financialOverview();

        return entity.getItems().get(0).getProducts().stream()
                .filter(ProductsEntity::isCreditCard)
                .map(productsEntity -> productsEntity.toCreditCardAccount())
                .collect(Collectors.toList());
    }

    @Override
    public List<AggregationTransaction> fetchTransactionsFor(CreditCardAccount account) {
        String productType = account.getFromTemporaryStorage(CommerzbankConstants.HEADERS.CREDIT_CARD_PRODUCT_TYPE);
        String identifier = account.getFromTemporaryStorage(CommerzbankConstants.HEADERS.CREDIT_CARD_IDENTIFIER);

        if (!Strings.isNullOrEmpty(productType) && !Strings.isNullOrEmpty(identifier)) {
            try {
                return apiClient.transactionOverview(productType, identifier, 0, 1000).getItems()
                        .get(0).getTinkTransactions().stream().collect(Collectors.toList());
            } catch (Exception e) {
                LOGGER.warnExtraLong(e.toString(), CommerzbankConstants.LOGTAG.CREDIT_CARD_FETCHING_ERROR);
            }
        }
        return Collections.EMPTY_LIST;
    }
}
