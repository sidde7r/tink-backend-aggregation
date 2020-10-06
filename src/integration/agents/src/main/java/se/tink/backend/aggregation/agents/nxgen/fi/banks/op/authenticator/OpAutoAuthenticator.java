package se.tink.backend.aggregation.agents.nxgen.fi.banks.op.authenticator;

import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.OpBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.OpBankPersistentStorage;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.authenticator.rpc.InitResponseEntity;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.authenticator.rpc.OpBankLoginRequestEntity;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.authenticator.AutoAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticator;

public class OpAutoAuthenticator implements PasswordAuthenticator, AutoAuthenticator {

    private final OpBankApiClient apiClient;
    private final OpBankPersistentStorage persistentStorage;
    private final Credentials credentials;
    private final PasswordAuthenticationController authenticationController;

    public OpAutoAuthenticator(
            OpBankApiClient client,
            OpBankPersistentStorage persistentStorage,
            Credentials credentials) {
        this.apiClient = client;
        this.persistentStorage = persistentStorage;
        this.credentials = credentials;
        this.authenticationController = new PasswordAuthenticationController(this);
    }

    @Override
    public void autoAuthenticate() throws SessionException {
        if (!persistentStorage.containsAppId()) {
            throw SessionError.SESSION_EXPIRED.exception();
        }
        try {
            authenticationController.authenticate(credentials);
        } catch (AuthenticationException | AuthorizationException e) {
            throw SessionError.SESSION_EXPIRED.exception(e);
        }
    }

    @Override
    public void authenticate(String username, String password)
            throws AuthenticationException, AuthorizationException {
        InitResponseEntity iResponse = apiClient.init();
        String authToken = OpAuthenticationTokenGenerator.calculateAuthToken(iResponse.getSeed());
        credentials.setSensitivePayload(Field.Key.ACCESS_TOKEN, authToken);
        OpBankLoginRequestEntity request =
                new OpBankLoginRequestEntity()
                        .setPassword(password)
                        .setUserid(username)
                        .setApplicationInstanceId(persistentStorage.retrieveInstanceId());

        apiClient.login(request);
        apiClient.setRepresentationType();
        apiClient.postLogin(authToken, persistentStorage.retrieveInstanceId());
    }
}
