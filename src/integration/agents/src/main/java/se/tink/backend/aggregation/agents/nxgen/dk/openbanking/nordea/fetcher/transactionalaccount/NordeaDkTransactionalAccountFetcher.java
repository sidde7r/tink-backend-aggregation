package se.tink.backend.aggregation.agents.nxgen.dk.openbanking.nordea.fetcher.transactionalaccount;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.NordeaBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.fetcher.transactionalaccount.NordeaBaseTransactionalAccountFetcher;

public class NordeaDkTransactionalAccountFetcher extends NordeaBaseTransactionalAccountFetcher {

    public NordeaDkTransactionalAccountFetcher(NordeaBaseApiClient apiClient) {
        super(apiClient);
    }
}
