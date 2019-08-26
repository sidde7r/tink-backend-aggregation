package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.fetcher.cardaccounts;

import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.time.temporal.TemporalAdjusters;
import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.fetcher.cardaccounts.rpc.FetchCardAccountsTransactions;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbrandedcards.entities.ErrorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionMonthPaginator;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;

public class SebCardTransactionsFetcher implements TransactionMonthPaginator<CreditCardAccount> {

    private final SebBaseApiClient client;

    public SebCardTransactionsFetcher(SebBaseApiClient client) {
        this.client = client;
    }

    @Override
    public PaginatorResponse getTransactionsFor(CreditCardAccount account, Year year, Month month) {
        LocalDate fromDate = LocalDate.of(year.getValue(), month, 1);
        LocalDate toDate = fromDate.with(TemporalAdjusters.lastDayOfMonth());

        LocalDate now = LocalDate.now();
        if (toDate.isAfter(now)) {
            toDate = now;
        }

        try {
            FetchCardAccountsTransactions response =
                    client.fetchCardTransactions(account.getApiIdentifier(), fromDate, toDate);
            return PaginatorResponseImpl.create(response.tinkTransactions());
        } catch (HttpResponseException e) {
            if (e.getResponse().getStatus() == HttpStatus.SC_INTERNAL_SERVER_ERROR
                    && e.getResponse().getBody(ErrorResponse.class).isEndOfPagingError()) {

                return PaginatorResponseImpl.createEmpty(false);
            }

            throw e;
        }
    }
}
