package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.nordeafi.fetcher.transactionalaccount;

import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.nordeafi.NordeaFiApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.fetcher.transactionalaccount.NordeaBaseTransactionalAccountFetcher;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class NordeaFiTransactionalAccountFetcher extends NordeaBaseTransactionalAccountFetcher {

    public NordeaFiTransactionalAccountFetcher(NordeaFiApiClient apiClient) {
        super(apiClient);
    }
}
