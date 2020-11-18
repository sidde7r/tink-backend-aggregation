package se.tink.backend.aggregation.agents.nxgen.se.business.swedbank.fetchers.transactional;

import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.SwedbankDefaultApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.fetchers.transactional.SwedbankDefaultTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.EngagementTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.LinkEntity;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class SwedbankSEBusinessTransactionalAccountFetcher
        extends SwedbankDefaultTransactionalAccountFetcher {

    public SwedbankSEBusinessTransactionalAccountFetcher(
            SwedbankDefaultApiClient apiClient, PersistentStorage persistentStorage) {
        super(apiClient, persistentStorage);
    }

    @Override
    protected EngagementTransactionsResponse fetchTransactions(
            TransactionalAccount account, LinkEntity key) {
        // the business app adds ?page=first to fetch first page
        // otherwise the link to the next page will be wrong, and it won't return any transactions
        // pagination is tracked server-side: subsequent pages have the same URL with page=next
        if (!key.getUri().contains("page=")) {
            key.setUri(key.getUri() + "?page=first");
        }
        return super.fetchTransactions(account, key);
    }
}
