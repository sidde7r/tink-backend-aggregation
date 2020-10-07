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
        Date dateFrom = startDate;
        if (isMaxDateToFetchInThePastAfterGivenDate(startDate)) {
            dateFrom = MAX_DATE_TO_FETCH_IN_THE_PAST;
        }

        TransactionResponse transactionResponse =
                apiClient.fetchTransactions(account, dateFrom, endDate);
        Collection<Transaction> transactions = getAllTransactions(transactionResponse);

        if (isMaxDateToFetchInThePastAfterGivenDate(startDate)) {
            return PaginatorResponseImpl.create(transactions, false);
        }
        return PaginatorResponseImpl.create(transactions);
    }

    private Collection<Transaction> getAllTransactions(TransactionResponse transactionResponse) {
        Collection<Transaction> transactions = transactionResponse.getTinkTransactions();
        transactions.addAll(getNextTransactionsInThisPeriodOfTime(transactionResponse.nextKey()));
        return transactions;
    }

    private Collection<Transaction> getNextTransactionsInThisPeriodOfTime(String nextKey) {
        List<Transaction> nextTransactions = new LinkedList<>();
        while (nextKey != null) {
            TransactionResponse nextTransactionsPage = apiClient.fetchNextTransactions(nextKey);
            nextTransactions.addAll(nextTransactionsPage.getTinkTransactions());
            nextKey = nextTransactionsPage.nextKey();
        }
        return nextTransactions;
    }

    private boolean isMaxDateToFetchInThePastAfterGivenDate(Date date) {
        return MAX_DATE_TO_FETCH_IN_THE_PAST.after(date);
    }
}
