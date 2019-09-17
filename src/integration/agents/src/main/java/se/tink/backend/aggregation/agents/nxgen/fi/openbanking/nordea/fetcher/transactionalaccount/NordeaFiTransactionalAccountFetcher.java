package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.nordea.fetcher.transactionalaccount;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.NordeaBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.fetcher.transactionalaccount.NordeaBaseTransactionalAccountFetcher;

public class NordeaFiTransactionalAccountFetcher extends NordeaBaseTransactionalAccountFetcher {

    public NordeaFiTransactionalAccountFetcher(NordeaBaseApiClient apiClient) {
        super(apiClient);
    }
}
