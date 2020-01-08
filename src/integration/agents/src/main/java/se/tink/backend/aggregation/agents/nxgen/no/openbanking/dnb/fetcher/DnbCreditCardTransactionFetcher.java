package se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.fetcher;

import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.DnbApiClient;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;

public class DnbCreditCardTransactionFetcher
        implements TransactionDatePaginator<CreditCardAccount> {

    private final DnbApiClient apiClient;

    public DnbCreditCardTransactionFetcher(DnbApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public PaginatorResponse getTransactionsFor(
            CreditCardAccount account, Date fromDate, Date toDate) {
        return apiClient.fetchCreditCardTransactions(account, fromDate, toDate);
    }
}
