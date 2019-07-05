package se.tink.backend.aggregation.agents.nxgen.se.openbanking.sdc.fetcher.transactionalaccount;

import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sdc.SdcApiClient;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

@JsonObject
public class SdcTransactionalAccountTransactionFetcher
        implements TransactionDatePaginator<TransactionalAccount> {

    private final SdcApiClient apiClient;

    public SdcTransactionalAccountTransactionFetcher(SdcApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public PaginatorResponse getTransactionsFor(
            TransactionalAccount account, Date fromDate, Date toDate) {
        return apiClient.getTransactionsFor(account, fromDate, toDate);
    }
}
