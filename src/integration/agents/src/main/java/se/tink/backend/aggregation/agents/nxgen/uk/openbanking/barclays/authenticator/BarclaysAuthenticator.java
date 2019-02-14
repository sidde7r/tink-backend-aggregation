package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.barclays.authenticator;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.UkOpenBankingApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.authenticator.UkOpenBankingAuthenticator;
import se.tink.backend.aggregation.nxgen.http.URL;
import static se.tink.backend.aggregation.agents.nxgen.uk.openbanking.barclays.BarclaysConstants.AuthenticationQueryParameters.AUTHONDEVICE;
import static se.tink.backend.aggregation.agents.nxgen.uk.openbanking.barclays.BarclaysConstants.AuthenticationQueryParameters.YES;

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
