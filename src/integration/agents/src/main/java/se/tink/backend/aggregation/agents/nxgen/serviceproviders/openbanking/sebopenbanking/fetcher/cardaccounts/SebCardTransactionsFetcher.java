package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebopenbanking.fetcher.cardaccounts;

import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.time.temporal.TemporalAdjusters;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebopenbanking.SebApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebopenbanking.fetcher.cardaccounts.rpc.FetchCardAccountsTransactions;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionMonthPaginator;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;

public class SebCardTransactionsFetcher implements TransactionMonthPaginator<CreditCardAccount> {

    private SebApiClient client;

    public SebCardTransactionsFetcher(SebApiClient client) {
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

        FetchCardAccountsTransactions response =
                client.fetchTransctions(account.getBankIdentifier(), fromDate, toDate);

        return PaginatorResponseImpl.create(response.tinkTransactions(account));
    }
}
