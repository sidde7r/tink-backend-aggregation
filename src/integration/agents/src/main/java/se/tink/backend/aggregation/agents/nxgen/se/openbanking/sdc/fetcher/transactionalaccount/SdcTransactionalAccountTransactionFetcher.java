package se.tink.backend.aggregation.agents.nxgen.se.openbanking.sdc.fetcher.transactionalaccount;

import java.util.Date;
import org.apache.commons.lang.time.DateUtils;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sdc.SdcApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sdc.fetcher.transactionalaccount.rpc.TransactionsResponse;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

@JsonObject
public class SdcTransactionalAccountTransactionFetcher
        implements TransactionDatePaginator<TransactionalAccount> {

    private final SdcApiClient apiClient;
    private Date lastTransactionDateFetched = new Date();

    public SdcTransactionalAccountTransactionFetcher(SdcApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public TransactionsResponse getTransactionsFor(
            TransactionalAccount account, Date fromDate, Date toDate) {

        // Utilises "fetch till 3 empty responses" from DatePaginator, ignores it's to/from dates
        TransactionsResponse transactions =
                apiClient.getTransactionsFor(
                        account,
                        DateUtils.addMonths(lastTransactionDateFetched, -3),
                        lastTransactionDateFetched);

        lastTransactionDateFetched =
                transactions
                        .getOverflowTransactionDate()
                        .orElseGet(() -> DateUtils.addMonths(lastTransactionDateFetched, -3));

        return transactions;
    }
}
