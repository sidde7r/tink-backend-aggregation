package se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.fetcher.creditcard;

import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.SpankkiApiClient;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;

public class SpankkiCreditCardTransactionsFetcher
        implements TransactionDatePaginator<CreditCardAccount> {
    private final SpankkiApiClient apiClient;

    public SpankkiCreditCardTransactionsFetcher(SpankkiApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public PaginatorResponse getTransactionsFor(
            CreditCardAccount account, Date fromDate, Date toDate) {
        return PaginatorResponseImpl.createEmpty(false);
    }
}
