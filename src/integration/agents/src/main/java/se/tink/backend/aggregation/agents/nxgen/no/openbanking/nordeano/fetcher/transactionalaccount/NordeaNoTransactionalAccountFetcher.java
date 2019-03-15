package se.tink.backend.aggregation.agents.nxgen.no.openbanking.nordeano.fetcher.transactionalaccount;

import se.tink.backend.aggregation.agents.nxgen.no.openbanking.nordeano.NordeaNoApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.fetcher.transactionalaccount.NordeaBaseTransactionalAccountFetcher;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class NordeaNoTransactionalAccountFetcher extends NordeaBaseTransactionalAccountFetcher {

    public NordeaNoTransactionalAccountFetcher(NordeaNoApiClient apiClient) {
        super(apiClient);
    }
}
