package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.fetcher.transactionalaccount;

import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.lang3.time.DateUtils;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.BankdataApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.fetcher.transactionalaccount.rpc.TransactionResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

public class BankdataTransactionalAccountFetcher
        implements AccountFetcher<TransactionalAccount>,
                TransactionDatePaginator<TransactionalAccount> {

    private static final Date MAX_DATE_TO_FETCH_IN_THE_PAST =
            DateUtils.addDays(DateUtils.addYears(new Date(), -5), 1);

    private final BankdataApiClient apiClient;

    public BankdataTransactionalAccountFetcher(BankdataApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        return apiClient.fetchAccounts().toTinkAccounts();
    }

    @Override
    public PaginatorResponse getTransactionsFor(
            TransactionalAccount account, Date startDate, Date endDate) {
        if (isMaxDateToFetchInThePastAfterGivenDate(endDate)) {
            return PaginatorResponseImpl.createEmpty(false);
        }
        if (isMaxDateToFetchInThePastAfterGivenDate(startDate)) {
            startDate = MAX_DATE_TO_FETCH_IN_THE_PAST;
        }
        TransactionResponse transactionResponse =
                apiClient.fetchTransactions(account, startDate, endDate);

        transactionResponse
                .getTransactions()
                .toTinkTransactions()
                .addAll(getNextTransactionsInThisPeriodOfTime(transactionResponse.nextKey()));

        if (isMaxDateToFetchInThePastAfterGivenDate(startDate)) {
            return PaginatorResponseImpl.create(transactionResponse.getTinkTransactions(), false);
        }
        return transactionResponse;
    }

    private List<Transaction> getNextTransactionsInThisPeriodOfTime(String nextKey) {
        List<Transaction> result = new LinkedList<>();
        while (nextKey != null) {
            TransactionResponse nextTransactionsPage = apiClient.fetchNextTransactions(nextKey);
            result.addAll(nextTransactionsPage.getTransactions().toTinkTransactions());
            nextKey = nextTransactionsPage.nextKey();
        }
        return result;
    }

    private boolean isMaxDateToFetchInThePastAfterGivenDate(Date date) {
        return MAX_DATE_TO_FETCH_IN_THE_PAST.after(date);
    }
}
