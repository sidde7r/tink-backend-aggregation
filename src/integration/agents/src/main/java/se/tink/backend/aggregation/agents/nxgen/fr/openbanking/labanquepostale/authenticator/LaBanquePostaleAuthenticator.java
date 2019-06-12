package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.authenticator;

import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.BankServiceException;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.LaBanquePostaleApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.authenticator.BerlinGroupAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class LaBanquePostaleAuthenticator extends BerlinGroupAuthenticator
        implements Authenticator {

    private final LaBanquePostaleApiClient apiClient;
    private final SessionStorage sessionStorage;

    public LaBanquePostaleAuthenticator(
            LaBanquePostaleApiClient apiClient, SessionStorage sessionStorage) {
        super(apiClient);
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public void authenticate(Credentials credentials)
            throws AuthenticationException, AuthorizationException {}

    @Override
    public OAuth2Token exchangeAuthorizationCode(String code) throws BankServiceException {
        return null;
    }
}
