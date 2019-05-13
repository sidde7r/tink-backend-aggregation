package se.tink.backend.aggregation.agents.nxgen.hu.openbanking.granitbank.session;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintechblocks.FintechblocksApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintechblocks.session.FintechblocksSessionHandler;

public final class GranitBankSessionHandler extends FintechblocksSessionHandler {

    public GranitBankSessionHandler(FintechblocksApiClient apiClient) {
        super(apiClient);
    }
}
