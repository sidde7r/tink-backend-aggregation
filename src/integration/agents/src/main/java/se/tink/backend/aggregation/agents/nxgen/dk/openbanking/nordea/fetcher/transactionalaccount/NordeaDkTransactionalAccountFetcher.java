package se.tink.backend.aggregation.agents.nxgen.dk.openbanking.nordea.fetcher.transactionalaccount;

import se.tink.backend.aggregation.agents.nxgen.dk.openbanking.nordea.NordeaDkApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.fetcher.transactionalaccount.NordeaBaseTransactionalAccountFetcher;

public class NordeaDkTransactionalAccountFetcher extends NordeaBaseTransactionalAccountFetcher {

    public NordeaDkTransactionalAccountFetcher(NordeaDkApiClient apiClient) {
        super(apiClient);
    }
}
