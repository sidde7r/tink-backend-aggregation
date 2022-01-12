package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.siliconvalley.authenticator;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.UkOpenBankingApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.authenticator.UkOpenBankingAisAuthenticator;

public class SiliconValleyAisAuthenticator extends UkOpenBankingAisAuthenticator {

    public SiliconValleyAisAuthenticator(UkOpenBankingApiClient apiClient) {
        super(apiClient);
    }

    @Override
    public boolean useMaxAge() {
        return false;
    }
}
