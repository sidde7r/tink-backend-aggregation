package se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.fetcher.credit;

import com.google.common.base.Strings;
import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.CommerzbankApiClient;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.CommerzbankConstants.Headers;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.CommerzbankConstants.Tag;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.entities.ResultEntity;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.fetcher.transaction.entities.TransactionResultEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;

public class CommerzbankCreditCardFetcher
        implements AccountFetcher<CreditCardAccount>, TransactionDatePaginator<CreditCardAccount> {
    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final CommerzbankApiClient apiClient;

    public CommerzbankCreditCardFetcher(CommerzbankApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<CreditCardAccount> fetchAccounts() {
        ResultEntity entity = apiClient.financialOverview();
        return entity.toCreditAccounts();
    }

    @Override
    public PaginatorResponse getTransactionsFor(
            CreditCardAccount account, Date fromDate, Date toDate) {
        String productType = account.getFromTemporaryStorage(Headers.CREDIT_CARD_PRODUCT_TYPE);
        String identifier = account.getFromTemporaryStorage(Headers.CREDIT_CARD_IDENTIFIER);
        String productBranch = account.getFromTemporaryStorage(Headers.PRODUCT_BRANCH);

        if (!Strings.isNullOrEmpty(productType)
                && !Strings.isNullOrEmpty(identifier)
                && !Strings.isNullOrEmpty(productBranch)) {
            try {
                TransactionResultEntity response =
                        apiClient.fetchAllPages(
                                fromDate, toDate, productType, identifier, productBranch);
                if (response != null && response.containsTransactions()) {
                    return response.getItems().get(0);
                }

            } catch (Exception e) {
                logger.error(
                        "tag={} Could not fetch credit transactions",
                        Tag.CREDIT_CARD_FETCHING_ERROR,
                        e);
                return PaginatorResponseImpl.createEmpty();
            }
        }
        return PaginatorResponseImpl.createEmpty();
    }
}
