package se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.fetcher.transactionalaccount;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.NordeaBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.fetcher.transactionalaccount.NordeaBaseTransactionalAccountFetcher;

public class NordeaSeTransactionalAccountFetcher extends NordeaBaseTransactionalAccountFetcher {

    public NordeaSeTransactionalAccountFetcher(NordeaBaseApiClient apiClient) {
        super(apiClient);
    }
}
