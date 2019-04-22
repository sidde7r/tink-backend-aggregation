package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.samlink.fetcher.transactionalaccount;

import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.samlink.configuration.SamlinkConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.BerlinGroupTransactionFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class SamlinkTransactionFetcher extends BerlinGroupTransactionFetcher {
    private final SamlinkConfiguration configuration;

    public SamlinkTransactionFetcher(
            BerlinGroupApiClient apiClient, SamlinkConfiguration configuration) {
        super(apiClient);
        this.configuration = configuration;
    }

    @Override
    public TransactionKeyPaginatorResponse<String> getTransactionsFor(
            TransactionalAccount account, String nextUrl) {

        return TransactionKeyPaginatorResponseImpl.createEmpty();

        //        TODO: Uncomment it when endpoint is done by Samlink
        //        return apiClient.fetchTransactions(
        //                configuration.getBaseUrl()
        //                        + String.format(Urls.TRANSACTIONS, account.getApiIdentifier()));
    }
}
