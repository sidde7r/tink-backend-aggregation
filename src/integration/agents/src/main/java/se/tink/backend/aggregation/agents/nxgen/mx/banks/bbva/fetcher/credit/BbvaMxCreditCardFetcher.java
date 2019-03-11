package se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.fetcher.credit;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.BbvaMxApiClient;
import se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.BbvaMxConstants;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.UpcomingTransactionFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.UpcomingTransaction;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;

public class BbvaMxCreditCardFetcher
        implements AccountFetcher<CreditCardAccount>,
                TransactionDatePaginator<CreditCardAccount>,
                UpcomingTransactionFetcher<CreditCardAccount> {

    private final BbvaMxApiClient client;

    public BbvaMxCreditCardFetcher(BbvaMxApiClient client) {
        this.client = client;
    }

    @Override
    public Collection<CreditCardAccount> fetchAccounts() {
        try {
            return client.fetchCredit().getCreditCardAccounts();
        } catch (HttpResponseException e) {
            return Collections.emptyList();
        }
    }

    @Override
    public PaginatorResponse getTransactionsFor(
            CreditCardAccount account, Date fromDate, Date toDate) {
        String accountId = account.getFromTemporaryStorage(BbvaMxConstants.STORAGE.ACCOUNT_ID);
        try {
            return client.fetchCreditTransactions(accountId, fromDate, toDate);
        } catch (HttpResponseException e) {
            return PaginatorResponseImpl.createEmpty(false);
        }
    }

    @Override
    public Collection<UpcomingTransaction> fetchUpcomingTransactionsFor(CreditCardAccount account) {
        return Collections.emptyList();
    }
}
