package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.barclays.authenticator;

import static se.tink.backend.aggregation.agents.nxgen.uk.openbanking.barclays.BarclaysConstants.AuthenticationQueryParameters.AUTHONDEVICE;
import static se.tink.backend.aggregation.agents.nxgen.uk.openbanking.barclays.BarclaysConstants.AuthenticationQueryParameters.YES;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.UkOpenBankingApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.authenticator.UkOpenBankingAisAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingAisConfig;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class BarclaysAuthenticator extends UkOpenBankingAisAuthenticator {

    public BarclaysAuthenticator(
            UkOpenBankingApiClient apiClient, UkOpenBankingAisConfig aisConfig) {
        super(apiClient, aisConfig);
    }

    @Override
    public URL decorateAuthorizeUrl(
            URL authorizeUrl, String state, String nonce, String callbackUri) {
        URL url = super.decorateAuthorizeUrl(authorizeUrl, state, nonce, callbackUri);
        return url.queryParam(AUTHONDEVICE, YES);
    }
}
