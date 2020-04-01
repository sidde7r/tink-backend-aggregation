package se.tink.backend.aggregation.agents.nxgen.se.openbanking.volvofinans.fetcher.transactionalaccount;

import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.volvofinans.VolvoFinansApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.volvofinans.VolvoFinansConstants.ErrorMessages;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

public class VolvoFinansCreditCardTransactionsFetcher
        implements TransactionDatePaginator<CreditCardAccount> {

    private final VolvoFinansApiClient apiClient;

    public VolvoFinansCreditCardTransactionsFetcher(VolvoFinansApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public PaginatorResponse getTransactionsFor(
            CreditCardAccount account, Date fromDate, Date toDate) {
        try {
            return apiClient.fetchTransactions(account, fromDate, toDate);
        } catch (HttpResponseException e) {
            final String errorMessage = e.getResponse().getBody(String.class);
            if (errorMessage.contains(ErrorMessages.NINETY_DAYS_TRANSACTIONS_ONLY)) {
                return PaginatorResponseImpl.createEmpty(false);
            } else {
                throw e;
            }
        }
    }
}
