package se.tink.backend.aggregation.agents.nxgen.hu.openbanking.sberbank.session;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintechblocks.FintechblocksApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintechblocks.session.FintechblocksSessionHandler;

public final class SberBankSessionHandler extends FintechblocksSessionHandler {

    public SberBankSessionHandler(FintechblocksApiClient apiClient) {
        super(apiClient);
    }
}
