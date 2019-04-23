package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.barclays.authenticator;

import static se.tink.backend.aggregation.agents.nxgen.uk.openbanking.barclays.BarclaysConstants.AuthenticationQueryParameters.AUTHONDEVICE;
import static se.tink.backend.aggregation.agents.nxgen.uk.openbanking.barclays.BarclaysConstants.AuthenticationQueryParameters.YES;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.UkOpenBankingApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.authenticator.UkOpenBankingAuthenticator;
import se.tink.backend.aggregation.nxgen.http.URL;

public class BarclaysAuthenticator extends UkOpenBankingAuthenticator {

    public BarclaysAuthenticator(UkOpenBankingApiClient apiClient) {
        super(apiClient);
    }

    @Override
    public URL decorateAuthorizeUrl(URL authorizeUrl, String state, String nonce) {
        URL url = super.decorateAuthorizeUrl(authorizeUrl, state, nonce);
        return url.queryParam(AUTHONDEVICE, YES);
    }
}
