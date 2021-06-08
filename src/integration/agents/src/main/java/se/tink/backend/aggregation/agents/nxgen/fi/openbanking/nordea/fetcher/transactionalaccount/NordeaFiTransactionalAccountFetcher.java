package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.nordea.fetcher.transactionalaccount;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.NordeaBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.fetcher.transactionalaccount.NordeaBaseTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.fetcher.transactionalaccount.rpc.GetTransactionsResponse;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class NordeaFiTransactionalAccountFetcher<R extends GetTransactionsResponse<?>>
        extends NordeaBaseTransactionalAccountFetcher<R> {
    private final LocalDateTimeSource localDateTimeSource;

    public NordeaFiTransactionalAccountFetcher(
            NordeaBaseApiClient apiClient,
            Class<R> transactionResponseClass,
            String providerMarket,
            LocalDateTimeSource localDateTimeSource) {
        super(apiClient, transactionResponseClass, providerMarket);
        this.localDateTimeSource = localDateTimeSource;
    }

    @Override
    public TransactionKeyPaginatorResponse<String> getTransactionsFor(
            TransactionalAccount account, String key) {
        OneYearLimitGetTransactionsResponse transactions =
                (OneYearLimitGetTransactionsResponse) super.getTransactionsFor(account, key);
        return transactions.setLocalDateTimeSource(localDateTimeSource);
    }
}
