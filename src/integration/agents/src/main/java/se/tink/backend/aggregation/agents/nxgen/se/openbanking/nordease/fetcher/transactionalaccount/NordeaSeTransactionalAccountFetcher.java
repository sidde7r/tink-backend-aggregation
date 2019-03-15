package se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordease.fetcher.transactionalaccount;

import se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordease.NordeaSeApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.fetcher.transactionalaccount.NordeaBaseTransactionalAccountFetcher;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class NordeaSeTransactionalAccountFetcher extends NordeaBaseTransactionalAccountFetcher {

    public NordeaSeTransactionalAccountFetcher(NordeaSeApiClient apiClient) {
        super(apiClient);
    }
}
