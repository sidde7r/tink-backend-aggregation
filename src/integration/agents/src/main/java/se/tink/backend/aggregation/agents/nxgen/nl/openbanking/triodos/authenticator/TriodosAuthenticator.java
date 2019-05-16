package se.tink.backend.aggregation.agents.nxgen.nl.openbanking.triodos.authenticator;

import se.tink.backend.aggregation.agents.exceptions.BankServiceException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.authenticator.BerlinGroupAuthenticator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;

public class TriodosAuthenticator extends BerlinGroupAuthenticator {

    public TriodosAuthenticator(BerlinGroupApiClient apiClient) {
        super(apiClient);
    }

    @Override
    public OAuth2Token exchangeAuthorizationCode(String code) throws BankServiceException {
        return apiClient.getToken(code);
    }
}
