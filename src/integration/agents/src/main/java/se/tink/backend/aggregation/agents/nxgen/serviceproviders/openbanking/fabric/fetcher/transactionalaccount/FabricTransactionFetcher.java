package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.fetcher.transactionalaccount;

import java.util.Date;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.FabricConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.client.FabricFetcherApiClient;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

@RequiredArgsConstructor
public class FabricTransactionFetcher implements TransactionDatePaginator<TransactionalAccount> {

    private final PersistentStorage persistentStorage;
    private final FabricFetcherApiClient fetcherApiClient;

    @Override
    public PaginatorResponse getTransactionsFor(
            TransactionalAccount account, Date fromDate, Date toDate) {
        String consentId = persistentStorage.get(StorageKeys.CONSENT_ID);
        return fetcherApiClient.fetchTransactions(
                consentId, account.getApiIdentifier(), fromDate, toDate);
    }
}
