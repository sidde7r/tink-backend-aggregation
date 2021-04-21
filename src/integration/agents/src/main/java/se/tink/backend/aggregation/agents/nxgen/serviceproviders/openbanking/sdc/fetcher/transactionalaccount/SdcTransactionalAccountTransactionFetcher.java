package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sdc.fetcher.transactionalaccount;

import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sdc.SdcApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sdc.fetcher.transactionalaccount.rpc.TransactionsResponse;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

@JsonObject
public class SdcTransactionalAccountTransactionFetcher
        implements TransactionDatePaginator<TransactionalAccount> {

    private final SdcApiClient apiClient;
    private final String providerMarket;

    public SdcTransactionalAccountTransactionFetcher(
            SdcApiClient apiClient, String providerMarket) {
        this.apiClient = apiClient;
        this.providerMarket = providerMarket;
    }

    @Override
    public TransactionsResponse getTransactionsFor(
            TransactionalAccount account, Date fromDate, Date toDate) {
        return apiClient.getTransactionsFor(
                account.getApiIdentifier(), fromDate, toDate, providerMarket);
    }
}
