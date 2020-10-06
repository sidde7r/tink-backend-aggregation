package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.fetcher.transactionalaccount;

import java.util.Collection;
import java.util.Date;
import org.apache.commons.lang3.time.DateUtils;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.BankdataApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.fetcher.transactionalaccount.rpc.TransactionResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

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
        if (isStartDateBeforeLimit(startDate)) {
            return getTransactionResponseUntilTimeLimit(account, endDate);
        }
        TransactionResponse transactionResponse =
                apiClient.fetchTransactions(account, startDate, endDate);
        getNextTransactionsInThisPeriodOfTime(transactionResponse);
        return transactionResponse;
    }

    private PaginatorResponse getTransactionResponseUntilTimeLimit(
            TransactionalAccount account, Date endDate) {
        TransactionResponse transactionResponse =
                apiClient.fetchTransactions(account, MAX_DATE_TO_FETCH_IN_THE_PAST, endDate);
        getNextTransactionsInThisPeriodOfTime(transactionResponse);
        return PaginatorResponseImpl.create(transactionResponse.getTinkTransactions(), false);
    }

    private void getNextTransactionsInThisPeriodOfTime(TransactionResponse transactionResponse) {
        while (transactionResponse.nextKey() != null) {
            TransactionResponse nextTransactionsPage =
                    apiClient.fetchNextTransactions(transactionResponse.nextKey());
            transactionResponse
                    .getTransactions()
                    .getBooked()
                    .addAll(nextTransactionsPage.getTransactions().getBooked());
            transactionResponse
                    .getTransactions()
                    .getPending()
                    .addAll(nextTransactionsPage.getTransactions().getPending());
            transactionResponse
                    .getTransactions()
                    .setLinks(nextTransactionsPage.getTransactions().getLinks());
        }
    }

    private boolean isStartDateBeforeLimit(Date startDate) {
        return MAX_DATE_TO_FETCH_IN_THE_PAST.after(startDate);
    }
}
