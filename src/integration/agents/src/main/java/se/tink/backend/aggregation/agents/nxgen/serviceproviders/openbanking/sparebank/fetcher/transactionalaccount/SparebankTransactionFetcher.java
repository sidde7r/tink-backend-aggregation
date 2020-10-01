package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.fetcher.transactionalaccount;

import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.SparebankApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.SparebankConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.fetcher.transactionalaccount.rpc.TransactionResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

public class SparebankTransactionFetcher implements TransactionDatePaginator<TransactionalAccount> {
    private SparebankApiClient apiClient;

    public SparebankTransactionFetcher(final SparebankApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public PaginatorResponse getTransactionsFor(
            TransactionalAccount account, Date fromDate, Date toDate) {
        List<Transaction> result = new LinkedList<>();

        boolean includePending = shouldIncludePendingTransactions(toDate);
        try {
            TransactionResponse transactionResponse =
                    apiClient.fetchTransactions(account.getApiIdentifier(), fromDate, toDate);

            result.addAll(transactionResponse.getTransactions().toTinkTransactions(includePending));

            while (transactionResponse.getTransactions().getLinks().hasNextLink()) {
                String nextPath =
                        transactionResponse.getTransactions().getLinks().getNext().getHref();
                transactionResponse = apiClient.fetchNextTransactions(nextPath);
                result.addAll(
                        transactionResponse.getTransactions().toTinkTransactions(includePending));
            }
        } catch (HttpResponseException e) {
            String exceptionMessage = e.getResponse().getBody(String.class);
            if (exceptionMessage.contains(SparebankConstants.TransactionsResponse.ERROR_MESSAGE)) {
                return PaginatorResponseImpl.createEmpty(false);
            }
            throw e;
        }

        return PaginatorResponseImpl.create(result);
    }

    private boolean shouldIncludePendingTransactions(Date toDate) {
        // ITE-1252, to prevent facing some issues with Sparebak1 API
        // we only parse pending transactions if we are asking for most recent transactions
        // in a very clunky way.

        // It would be awesome to improve it further. Either get a nicer way of detecting "first"
        // page,
        // or wait for fix on Sparebank1 side - which might not come.

        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.DAY_OF_MONTH, -7);
        Date dateBefore7Days = cal.getTime();
        return toDate.after(dateBefore7Days);
    }
}
