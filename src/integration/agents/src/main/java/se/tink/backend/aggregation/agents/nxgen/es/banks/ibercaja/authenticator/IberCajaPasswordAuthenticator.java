package se.tink.backend.aggregation.agents.nxgen.es.banks.ibercaja.authenticator;

import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ibercaja.IberCajaApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ibercaja.IberCajaConstants;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ibercaja.authenticator.rpc.LoginRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ibercaja.authenticator.rpc.LoginResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ibercaja.authenticator.rpc.SessionRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ibercaja.authenticator.rpc.SessionResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticator;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class IberCajaPasswordAuthenticator implements PasswordAuthenticator {

    private final IberCajaApiClient bankClient;
    private final SessionStorage storage;

    public IberCajaPasswordAuthenticator(IberCajaApiClient bankClient, SessionStorage storage) {

        this.bankClient = bankClient;
        this.storage = storage;
    }

    @Override
    public void authenticate(String username, String password)
            throws AuthenticationException, AuthorizationException {

        // Construct session request from username and password
        SessionResponse sessionResponse =
                bankClient.initializeSession(
                        new SessionRequest(
                                username,
                                password,
                                IberCajaConstants.DefaultRequestParams.CARD,
                                IberCajaConstants.DefaultRequestParams.LAST_ACCESS));

        LoginResponse loginResponse =
                bankClient.login(
                        new LoginRequest(sessionResponse.getNici()),
                        sessionResponse.getTicket(),
                        sessionResponse.getUser());

        loginResponse.saveResponse(storage);

        storage.put(IberCajaConstants.Storage.USERNAME, username);

        storage.put(IberCajaConstants.Storage.TICKET, sessionResponse.getTicket());
    }
}
