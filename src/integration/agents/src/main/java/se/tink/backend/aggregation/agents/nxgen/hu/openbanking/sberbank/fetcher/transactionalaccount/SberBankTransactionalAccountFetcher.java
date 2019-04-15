package se.tink.backend.aggregation.agents.nxgen.hu.openbanking.sberbank.fetcher.transactionalaccount;

import se.tink.backend.aggregation.agents.nxgen.hu.openbanking.sberbank.SberBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintechblocks.fetcher.transactionalaccount.FintechblocksTransactionalAccountFetcher;

public class SberBankTransactionalAccountFetcher extends FintechblocksTransactionalAccountFetcher {

    public SberBankTransactionalAccountFetcher(SberBankApiClient apiClient) {
        super(apiClient);
    }
}
