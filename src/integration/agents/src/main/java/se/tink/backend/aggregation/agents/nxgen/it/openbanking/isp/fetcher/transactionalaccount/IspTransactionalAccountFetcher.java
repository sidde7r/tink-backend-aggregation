package se.tink.backend.aggregation.agents.nxgen.it.openbanking.isp.fetcher.transactionalaccount;

import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.CbiGlobeAuthenticationController;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.fetcher.transactionalaccount.CbiGlobeTransactionalAccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class IspTransactionalAccountFetcher extends CbiGlobeTransactionalAccountFetcher {

    public IspTransactionalAccountFetcher(
            CbiGlobeApiClient apiClient,
            PersistentStorage persistentStorage,
            CbiGlobeAuthenticationController controller) {
        super(apiClient, persistentStorage, controller);
    }

    @Override
    public PaginatorResponse getTransactionsFor(
            TransactionalAccount account, Date fromDate, Date toDate) {
        fromDate = calculateFromDate(toDate);
        return apiClient.getTransactions(
                account.getApiIdentifier(), fromDate, toDate, QueryValues.BOOKED);
    }
}
