package se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.fetcher.creditcard;

import java.lang.invoke.MethodHandles;
import java.time.LocalDate;
import java.util.Collection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.ImaginBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.ImaginBankConstants;
import se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.fetcher.creditcard.rpc.CardTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.fetcher.creditcard.rpc.CardsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginator;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;

public class ImaginBankCreditCardFetcher
        implements AccountFetcher<CreditCardAccount>, TransactionPagePaginator<CreditCardAccount> {
    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final ImaginBankApiClient apiClient;

    public ImaginBankCreditCardFetcher(ImaginBankApiClient bankApi) {
        this.apiClient = bankApi;
    }

    @Override
    public Collection<CreditCardAccount> fetchAccounts() {
        apiClient.initiateCardFetching();
        CardsResponse cardsResponse = apiClient.fetchCards();

        return cardsResponse.getTinkCreditCards();
    }

    @Override
    public PaginatorResponse getTransactionsFor(CreditCardAccount account, int page) {
        // Pagination state is maintained on the server. We should only indicate if this is
        // new/first request or not.
        // The response contains a boolean that indicates if there is more data to fetch or not.
        LocalDate fromDate = ImaginBankConstants.CreditCard.START_DATE;
        LocalDate toDate = LocalDate.now();

        try {
            CardTransactionsResponse cardTransactionsResponse =
                    apiClient.fetchCardTransactions(
                            account.getApiIdentifier(), fromDate, toDate, page > 0);

            return cardTransactionsResponse.toPaginatorResponse();
        } catch (Exception e) {
            logger.warn("Failed to fetch credit card transactions ", e);
            return PaginatorResponseImpl.createEmpty(false);
        }
    }
}
