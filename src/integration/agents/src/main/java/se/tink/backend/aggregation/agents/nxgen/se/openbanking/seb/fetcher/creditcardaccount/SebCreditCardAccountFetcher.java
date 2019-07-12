package se.tink.backend.aggregation.agents.nxgen.se.openbanking.seb.fetcher.creditcardaccount;

import java.util.Collection;
import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.seb.SebApiClient;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;

public class SebCreditCardAccountFetcher
        implements AccountFetcher<CreditCardAccount>, TransactionDatePaginator<CreditCardAccount> {

    private final SebApiClient apiClient;

    public SebCreditCardAccountFetcher(SebApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<CreditCardAccount> fetchAccounts() {
        return apiClient.fetchCreditCardAccounts().toTinkAccounts();
    }

    @Override
    public PaginatorResponse getTransactionsFor(
            CreditCardAccount account, Date fromDate, Date toDate) {
        return apiClient.fetCreditCardTransactions(account, fromDate, toDate);
    }
}
