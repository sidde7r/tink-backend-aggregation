package se.tink.backend.aggregation.agents.nxgen.fi.banks.op.authenticator;

import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.OpBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.OpBankConstants;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.OpBankPersistentStorage;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.authenticator.rpc.InitRequestEntity;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.authenticator.rpc.InitResponseEntity;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.authenticator.rpc.OpBankLoginRequestEntity;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.authenticator.rpc.OpBankLoginResponseEntity;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticator;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class OpAutoAuthenticator implements PasswordAuthenticator, AutoAuthenticator {

    private final OpBankApiClient apiClient;
    private final OpBankPersistentStorage persistentStorage;
    private final Credentials credentials;
    private String authToken;
    private final PasswordAuthenticationController authenticationController;
    private SessionStorage sessionStorage;

    public OpAutoAuthenticator(
            OpBankApiClient client,
            OpBankPersistentStorage persistentStorage,
            Credentials credentials,
            SessionStorage sessionStorage) {
        this.apiClient = client;
        this.persistentStorage = persistentStorage;
        this.credentials = credentials;
        this.sessionStorage = sessionStorage;
        this.authenticationController = new PasswordAuthenticationController(this);
    }

    @Override
    public void autoAuthenticate() throws SessionException {
        if (!persistentStorage.containsAppId()) {
            throw SessionError.SESSION_EXPIRED.exception();
        }
        try {
            authenticationController.authenticate(credentials);
        } catch (AuthenticationException e) {
            e.printStackTrace();
        } catch (AuthorizationException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void authenticate(String username, String password)
            throws AuthenticationException, AuthorizationException {
        InitResponseEntity iResponse = apiClient.init(new InitRequestEntity());
        String authToken = OpAuthenticationTokenGenerator.calculateAuthToken(iResponse.getSeed());
        this.authToken = authToken;
        OpBankLoginRequestEntity request =
                new OpBankLoginRequestEntity()
                        .setPassword(password)
                        .setUserid(username)
                        .setApplicationInstanceId(persistentStorage.retrieveInstanceId());

        final OpBankLoginResponseEntity loginResponse = apiClient.login(request);
        sessionStorage.put(OpBankConstants.Storage.FULL_NAME, loginResponse.getName());

        apiClient.setRepresentationType();
        apiClient.postLogin(this.authToken, persistentStorage.retrieveInstanceId());
    }
}
