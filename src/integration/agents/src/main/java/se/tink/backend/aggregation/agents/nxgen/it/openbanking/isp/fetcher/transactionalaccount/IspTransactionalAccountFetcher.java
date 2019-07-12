package se.tink.backend.aggregation.agents.nxgen.it.openbanking.isp.fetcher.transactionalaccount;

import java.util.Date;
import org.joda.time.DateTime;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.CbiGlobeAuthenticationController;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.fetcher.transactionalaccount.CbiGlobeTransactionalAccountFetcher;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class IspTransactionalAccountFetcher extends CbiGlobeTransactionalAccountFetcher {

    public IspTransactionalAccountFetcher(
            CbiGlobeApiClient apiClient,
            PersistentStorage persistentStorage,
            CbiGlobeAuthenticationController controller) {
        super(apiClient, persistentStorage, controller);
    }

    @Override
    protected Date calculateFromDate(Date toDate) {
        return new DateTime(toDate).minusDays(15).toDate();
    }
}
