package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.samlink;

import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.authenticator.BerlinGroupAuthenticator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;

public class SamlinkAuthenticator extends BerlinGroupAuthenticator {

    public SamlinkAuthenticator(final BerlinGroupApiClient apiClient) {
        super(apiClient);
    }

    @Override
    public OAuth2Token exchangeAuthorizationCode(final String code) throws BankServiceException {
        return apiClient.getToken(code);
    }
}
