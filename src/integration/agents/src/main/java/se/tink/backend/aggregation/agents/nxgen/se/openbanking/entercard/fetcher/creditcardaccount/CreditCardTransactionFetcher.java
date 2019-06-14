package se.tink.backend.aggregation.agents.nxgen.se.openbanking.entercard.fetcher.creditcardaccount;

import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.entercard.EnterCardApiClient;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;

public class CreditCardTransactionFetcher implements TransactionDatePaginator<CreditCardAccount> {

    private final EnterCardApiClient apiClient;

    public CreditCardTransactionFetcher(EnterCardApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public PaginatorResponse getTransactionsFor(
            CreditCardAccount account, Date fromDate, Date toDate) {
        return apiClient.fetchCreditCardTransactions(account, fromDate, toDate);
    }
}
