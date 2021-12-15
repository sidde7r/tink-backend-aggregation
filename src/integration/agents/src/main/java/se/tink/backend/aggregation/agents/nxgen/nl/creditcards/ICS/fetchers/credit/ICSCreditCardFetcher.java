package se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.fetchers.credit;

import java.util.Date;
import lombok.RequiredArgsConstructor;
import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.ICSApiClient;
import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.ICSTimeProvider;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

@RequiredArgsConstructor
public class ICSCreditCardFetcher implements TransactionDatePaginator<CreditCardAccount> {

    private final ICSApiClient client;

    private final ICSTimeProvider timeProvider;

    @Override
    public PaginatorResponse getTransactionsFor(
            CreditCardAccount account, Date fromDate, Date toDate) {

        Date earliestFromDate = timeProvider.getConsentTransactionDate();
        if (fromDate.compareTo(earliestFromDate) < 0) {
            fromDate = earliestFromDate;
        }

        if (fromDate.compareTo(toDate) >= 0) {
            return PaginatorResponseImpl.createEmpty();
        }

        try {
            return client.getTransactionsByDate(
                    account.getApiIdentifier(),
                    timeProvider.convertToLocalDate(fromDate),
                    timeProvider.convertToLocalDate(toDate));
        } catch (HttpResponseException exception) {
            if (exception.getResponse().getStatus() == HttpStatus.SC_UNAUTHORIZED) {
                // Error when requesting transactions out of range. Happens when the whole requested
                // range is out of the consented period, but not happen when there's an overlap.
                // Can happen for users logged in before consent date was stored.
                return PaginatorResponseImpl.createEmpty(false);
            }
            throw exception;
        }
    }
}
