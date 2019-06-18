package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.lloyds.authenticator;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.UkOpenBankingApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.authenticator.UkOpenBankingAuthenticator;
import se.tink.backend.aggregation.nxgen.http.URL;

public class LloydsAuthenticator extends UkOpenBankingAuthenticator {
    public LloydsAuthenticator(UkOpenBankingApiClient apiClient) {
        super(apiClient);
    }

    @Override
    public URL decorateAuthorizeUrl(URL authorizeUrl, String state, String nonce) {
        return super.decorateAuthorizeUrl(authorizeUrl, state, nonce);
    }
}
