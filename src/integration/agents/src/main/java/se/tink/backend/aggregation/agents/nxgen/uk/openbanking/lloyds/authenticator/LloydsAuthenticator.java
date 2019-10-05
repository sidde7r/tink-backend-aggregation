package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.lloyds.authenticator;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.UkOpenBankingApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.authenticator.UkOpenBankingAisAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingAisConfig;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class LloydsAuthenticator extends UkOpenBankingAisAuthenticator {
    public LloydsAuthenticator(UkOpenBankingApiClient apiClient, UkOpenBankingAisConfig aisConfig) {
        super(apiClient, aisConfig);
    }

    @Override
    public URL decorateAuthorizeUrl(
            URL authorizeUrl, String state, String nonce, String callbackUri) {
        return super.decorateAuthorizeUrl(authorizeUrl, state, nonce, callbackUri);
    }
}
