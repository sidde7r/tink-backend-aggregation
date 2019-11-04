package se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.fetchers.credit;

import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.ICSApiClient;
import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.ICSConstants;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;

public class ICSCreditCardFetcher implements TransactionDatePaginator<CreditCardAccount> {

    private final ICSApiClient client;

    public ICSCreditCardFetcher(ICSApiClient client) {
        this.client = client;
    }

    @Override
    public PaginatorResponse getTransactionsFor(
            CreditCardAccount account, Date fromDate, Date toDate) {
        try {
            return client.getTransactionsByDate(
                    account.getFromTemporaryStorage(ICSConstants.StorageKeys.ACCOUNT_ID),
                    fromDate,
                    toDate);
        } catch (HttpResponseException exception) {
            if (exception.getResponse().getStatus() == 401) {
                return PaginatorResponseImpl.createEmpty(false);
            }
            throw exception;
        }
    }
}
