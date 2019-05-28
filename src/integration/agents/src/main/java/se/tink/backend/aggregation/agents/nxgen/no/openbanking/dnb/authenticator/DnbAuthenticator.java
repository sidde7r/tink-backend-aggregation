package se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.authenticator;

import se.tink.backend.aggregation.agents.exceptions.BankServiceException;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.DnbApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.authenticator.BerlinGroupAuthenticator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.URL;

public class DnbAuthenticator extends BerlinGroupAuthenticator {

    public DnbAuthenticator(final DnbApiClient apiClient) {
        super(apiClient);
    }

    public URL buildAuthorizeUrl(final String state) {
        return apiClient.getAuthorizeUrl(state);
    }

    @Override
    public OAuth2Token exchangeAuthorizationCode(final String code) throws BankServiceException {
        return null;
    }
}
