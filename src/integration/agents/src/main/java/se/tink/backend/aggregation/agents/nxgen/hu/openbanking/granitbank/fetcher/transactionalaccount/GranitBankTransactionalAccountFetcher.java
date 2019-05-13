package se.tink.backend.aggregation.agents.nxgen.hu.openbanking.granitbank.fetcher.transactionalaccount;

import se.tink.backend.aggregation.agents.nxgen.hu.openbanking.granitbank.GranitBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintechblocks.fetcher.transactionalaccount.FintechblocksTransactionalAccountFetcher;

public class GranitBankTransactionalAccountFetcher
        extends FintechblocksTransactionalAccountFetcher {

    public GranitBankTransactionalAccountFetcher(GranitBankApiClient apiClient) {
        super(apiClient);
    }
}
