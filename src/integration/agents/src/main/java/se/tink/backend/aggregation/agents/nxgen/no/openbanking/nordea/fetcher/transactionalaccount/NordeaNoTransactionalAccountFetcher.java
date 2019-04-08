package se.tink.backend.aggregation.agents.nxgen.no.openbanking.nordea.fetcher.transactionalaccount;

import se.tink.backend.aggregation.agents.nxgen.no.openbanking.nordea.NordeaNoApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.fetcher.transactionalaccount.NordeaBaseTransactionalAccountFetcher;
import se.tink.backend.aggregation.annotations.JsonObject;

public class NordeaNoTransactionalAccountFetcher extends NordeaBaseTransactionalAccountFetcher {

    public NordeaNoTransactionalAccountFetcher(NordeaNoApiClient apiClient) {
        super(apiClient);
    }
}
