package se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.authenticator;

import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.JyskeApiClient;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.JyskePersistentStorage;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.authenticator.entities.NemIdErrorEntity;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.authenticator.entities.NemIdLoginEncryptionEntity;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.authenticator.rpc.NemIdResponse;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.authenticator.security.Token;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticator;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;

public class JyskeAutoAuthenticator implements PasswordAuthenticator, AutoAuthenticator {
    private final JyskeApiClient apiClient;
    private final JyskePersistentStorage persistentStorage;
    private final Credentials credentials;
    private final PasswordAuthenticationController authenticationController;
    private final JyskeServiceAuthenticator serviceAuthenticator;

    public JyskeAutoAuthenticator(
            JyskeApiClient client,
            JyskePersistentStorage persistentStorage,
            Credentials credentials) {
        this.apiClient = client;
        this.persistentStorage = persistentStorage;
        this.credentials = credentials;
        this.authenticationController = new PasswordAuthenticationController(this);
        this.serviceAuthenticator = new JyskeServiceAuthenticator(apiClient);
    }

    @Override
    public void autoAuthenticate() throws SessionException {
        if (!persistentStorage.readyForSingleFactor()) {
            throw SessionError.SESSION_EXPIRED.exception();
        }
        try {
            authenticationController.authenticate(credentials);
        } catch (AuthorizationException
                | AuthenticationException e) { // AuthorizationException never thrown...
            throw SessionError.SESSION_EXPIRED.exception();
        }
    }

    @Override
    public void authenticate(String username, String password)
            throws AuthenticationException, AuthorizationException {
        Token token = Token.generate();
        apiClient.nemIdInit(token);

        NemIdLoginEncryptionEntity encryptionEntity = new NemIdLoginEncryptionEntity();

        String installId = persistentStorage.getInstallId();
        encryptionEntity.setInstallId(installId);
        encryptionEntity.setUserId(username);
        encryptionEntity.setPinCode(password);
        try {
            NemIdResponse encryption = apiClient.nemIdLoginWithInstallId(encryptionEntity, token);
            serviceAuthenticator.authenticate(encryption, token);
        } catch (HttpResponseException e) {
            NemIdErrorEntity.throwError(e);
        }
    }
}
