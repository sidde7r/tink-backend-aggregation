package se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.authenticator;

import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.IcaBankenApiClient;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.exceptions.NotImplementedException;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;


public class IcaBankenAuthenticator implements Authenticator {

    private final IcaBankenApiClient apiClient;
    private final SessionStorage sessionStorage;

    public IcaBankenAuthenticator(IcaBankenApiClient apiClient, SessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public void authenticate(Credentials credentials)
        throws AuthenticationException, AuthorizationException {
        throw new NotImplementedException("authenticate not implemented");
    }
}
