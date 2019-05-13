package se.tink.backend.aggregation.agents.nxgen.hu.openbanking.mkb.session;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintechblocks.FintechblocksApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintechblocks.session.FintechblocksSessionHandler;

public final class MkbSessionHandler extends FintechblocksSessionHandler {

    public MkbSessionHandler(FintechblocksApiClient apiClient) {
        super(apiClient);
    }
}
