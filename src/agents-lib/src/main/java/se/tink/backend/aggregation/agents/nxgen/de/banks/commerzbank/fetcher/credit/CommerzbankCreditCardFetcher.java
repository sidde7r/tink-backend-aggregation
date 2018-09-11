package se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.fetcher.credit;

import com.google.common.base.Strings;
import java.util.Collection;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.CommerzbankApiClient;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.CommerzbankConstants;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.entities.ResultEntity;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginator;
import se.tink.backend.aggregation.nxgen.core.account.CreditCardAccount;

public class CommerzbankCreditCardFetcher implements AccountFetcher<CreditCardAccount>,
        TransactionPagePaginator<CreditCardAccount> {

    private final CommerzbankApiClient apiClient;
    private static final AggregationLogger LOGGER = new AggregationLogger(CommerzbankCreditCardFetcher.class);

    public CommerzbankCreditCardFetcher(CommerzbankApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<CreditCardAccount> fetchAccounts() {
        ResultEntity entity = apiClient.financialOverview();

        return entity.toCreditAccount();
    }

    @Override
    public PaginatorResponse getTransactionsFor(CreditCardAccount account, int page) {
        String productType = account.getFromTemporaryStorage(CommerzbankConstants.HEADERS.CREDIT_CARD_PRODUCT_TYPE);
        String identifier = account.getFromTemporaryStorage(CommerzbankConstants.HEADERS.CREDIT_CARD_IDENTIFIER);
        String productBranch = account.getFromTemporaryStorage(CommerzbankConstants.HEADERS.PRODUCT_BRANCH);

        if (!Strings.isNullOrEmpty(productType) && !Strings.isNullOrEmpty(identifier)) {
            try {
                return apiClient.transactionOverview(productType, identifier, page, productBranch).getItems()
                        .get(0);
            } catch (Exception e) {
                LOGGER.warnExtraLong(e.toString(), CommerzbankConstants.LOGTAG.CREDIT_CARD_FETCHING_ERROR);
            }
        }
        return PaginatorResponseImpl.createEmpty();
    }
}
