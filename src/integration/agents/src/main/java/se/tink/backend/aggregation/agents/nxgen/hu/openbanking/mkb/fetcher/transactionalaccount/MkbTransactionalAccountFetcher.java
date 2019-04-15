package se.tink.backend.aggregation.agents.nxgen.hu.openbanking.mkb.fetcher.transactionalaccount;

import se.tink.backend.aggregation.agents.nxgen.hu.openbanking.mkb.MkbApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintechblocks.fetcher.transactionalaccount.FintechblocksTransactionalAccountFetcher;

public class MkbTransactionalAccountFetcher extends FintechblocksTransactionalAccountFetcher {

    public MkbTransactionalAccountFetcher(MkbApiClient apiClient) {
        super(apiClient);
    }
}
