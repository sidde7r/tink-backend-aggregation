package se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.fetcher.creditcard;

import org.assertj.core.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.NordeaFIApiClient;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.NordeaFIConstants;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginator;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class NordeaCreditCardTransactionsFetcher
        implements TransactionPagePaginator<CreditCardAccount> {
    private static final Logger logger =
            LoggerFactory.getLogger(NordeaCreditCardTransactionsFetcher.class);
    private final NordeaFIApiClient apiClient;
    private final SessionStorage sessionStorage;

    public NordeaCreditCardTransactionsFetcher(
            NordeaFIApiClient apiClient, SessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public PaginatorResponse getTransactionsFor(CreditCardAccount account, int page) {
        String accountId = account.getBankIdentifier();

        if (Strings.isNullOrEmpty(accountId)) {
            return PaginatorResponseImpl.createEmpty(false);
        }

        try {
            return apiClient.fetchCardTransactions(page, accountId);
        } catch (Exception e) {
            logger.error(
                    String.format(
                            "%s: %s",
                            NordeaFIConstants.LogTags.CREDIT_TRANSACTIONS_ERROR.toString(),
                            e.toString()));
            return PaginatorResponseImpl.createEmpty(false);
        }
    }
}
