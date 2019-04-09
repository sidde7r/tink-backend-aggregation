package se.tink.backend.aggregation.agents.nxgen.no.openbanking.nordea.fetcher.transactionalaccount;

import se.tink.backend.aggregation.agents.nxgen.no.openbanking.nordea.NordeaNoApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.fetcher.transactionalaccount.NordeaBaseTransactionalAccountFetcher;

public class NordeaNoTransactionalAccountFetcher extends NordeaBaseTransactionalAccountFetcher {

    public NordeaNoTransactionalAccountFetcher(NordeaNoApiClient apiClient) {
        super(apiClient);
    }
}
