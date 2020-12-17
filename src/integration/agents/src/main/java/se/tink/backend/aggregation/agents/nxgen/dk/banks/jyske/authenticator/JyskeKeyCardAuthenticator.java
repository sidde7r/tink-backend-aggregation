package se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.authenticator;

import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.JyskeApiClient;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.JyskePersistentStorage;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.authenticator.entities.KeycardChallengeEntity;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.authenticator.entities.KeycardEnrollEntity;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.authenticator.entities.NemIdInstallIdEntity;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.authenticator.entities.NemIdLoginEncryptionEntity;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.authenticator.entities.NemIdLoginInstallIdEncryptionEntity;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.authenticator.entities.NemIdLoginWithInstallIdResponse;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.authenticator.rpc.NemIdLoginResponse;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.authenticator.rpc.NemIdResponse;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.authenticator.security.Decryptor;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.authenticator.security.Token;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.authenticator.AutoAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.keycard.KeyCardAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.keycard.KeyCardInitValues;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

public class JyskeKeyCardAuthenticator implements KeyCardAuthenticator, AutoAuthenticator {

    private final JyskeApiClient apiClient;
    private final JyskePersistentStorage persistentStorage;
    private final Credentials credentials;

    public JyskeKeyCardAuthenticator(
            JyskeApiClient client,
            JyskePersistentStorage persistentStorage,
            Credentials credentials) {
        this.apiClient = client;
        this.persistentStorage = persistentStorage;
        this.credentials = credentials;
    }

    @Override
    public KeyCardInitValues init(String username, String password) throws AuthenticationException {
        this.persistentStorage.setUserId(username);
        this.persistentStorage.setPincode(password);
        Token token = this.persistentStorage.generateToken();

        apiClient.nemIdInit(token);

        NemIdLoginEncryptionEntity encryptionEntity =
                new NemIdLoginEncryptionEntity(
                        this.persistentStorage.getUserId(), this.persistentStorage.getPincode());

        KeycardChallengeEntity challengeEntity = getKeycardChallengeEntity(encryptionEntity, token);
        this.persistentStorage.setKeycardChallengeEntity(challengeEntity);

        return new KeyCardInitValues(challengeEntity.getKeycardNo(), challengeEntity.getKey());
    }

    /*
    The codecard number and key have been empty for some users, meaning it will be sent as null to the user during the supplemental information request.
    From looking at the frida logs, it seems that some user receive this information in the challenge, and some need to do a separate call to fetch the information.
     */
    private KeycardChallengeEntity getKeycardChallengeEntity(
            NemIdLoginEncryptionEntity encryptionEntity, Token token) {

        NemIdResponse challenge = apiClient.nemIdGetChallenge(encryptionEntity, token);

        KeycardChallengeEntity nemIdChallengeEntity =
                new Decryptor(token).read(challenge, KeycardChallengeEntity.class);

        if (nemIdChallengeEntity.containsValues()) {
            return nemIdChallengeEntity;
        }

        KeycardChallengeEntity changeToKeyCardEntity =
                apiClient.changeToKeyCard().decrypt(token, KeycardChallengeEntity.class);

        if (changeToKeyCardEntity.containsValues()) {
            return changeToKeyCardEntity;
        }

        throw new IllegalStateException("Unable to find keycardNo or key!");
    }

    @Override
    public void authenticate(String code) throws AuthenticationException, AuthorizationException {
        String username = persistentStorage.getUserId();
        String password = persistentStorage.getPincode();
        Token token =
                persistentStorage
                        .getToken()
                        .orElseThrow(() -> new IllegalStateException("Can not find token!"));
        KeycardChallengeEntity challengeEntity = persistentStorage.getKeycardChallengeEntity();

        KeycardEnrollEntity enrollEntity =
                new KeycardEnrollEntity(
                        code,
                        challengeEntity.getKeycardNo(),
                        challengeEntity.getKey(),
                        password,
                        challengeEntity.getSecurityDevice());

        NemIdResponse enrollment = null;
        try {
            enrollment = apiClient.nemIdEnroll(enrollEntity, token);
        } catch (HttpResponseException httpResponseException) {
            new JyskeKeyCardExceptionHandler(httpResponseException).handle();
        }

        NemIdInstallIdEntity installIdEntity =
                new Decryptor(token).read(enrollment, NemIdInstallIdEntity.class);

        String installId = installIdEntity.getInstallId();

        authenticateWithInstallId(username, password, installId, token);

        persistentStorage.setInstallId(installId);
    }

    @Override
    public void autoAuthenticate()
            throws SessionException, BankServiceException, AuthorizationException {
        if (!persistentStorage.containsInstallId()) {
            throw SessionError.SESSION_EXPIRED.exception();
        }
        String username = credentials.getField(Field.Key.USERNAME);
        String password = credentials.getField(Field.Key.PASSWORD);

        Token token =
                persistentStorage.getToken().orElseThrow(SessionError.SESSION_EXPIRED::exception);
        apiClient.nemIdInit(token);
        String installId = persistentStorage.getInstallId();

        try {
            authenticateWithInstallId(username, password, installId, token);
        } catch (AuthenticationException e) {
            throw SessionError.SESSION_EXPIRED.exception(e);
        }
    }

    private void authenticateWithInstallId(
            String username, String password, String installId, Token token) throws LoginException {

        NemIdLoginInstallIdEncryptionEntity encryptionEntity =
                new NemIdLoginInstallIdEncryptionEntity(username, password, installId);

        NemIdResponse encryption = apiClient.nemIdLoginWithInstallId(encryptionEntity, token);
        mobileServiceLogin(encryption, token);
    }

    private void mobileServiceLogin(NemIdResponse encryption, Token token) throws LoginException {
        NemIdLoginWithInstallIdResponse loginWithInstallId =
                new Decryptor(token).read(encryption, NemIdLoginWithInstallIdResponse.class);
        NemIdLoginResponse mobileServiceInit = apiClient.sendTransportKey(token);
        if (!mobileServiceInit.isOk()) {
            throw LoginError.ERROR_WITH_MOBILE_OPERATOR.exception();
        }

        NemIdLoginResponse mobilServiceLogin =
                apiClient.mobilServiceLogin(loginWithInstallId, token);
        if (!mobilServiceLogin.isOk()) {
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }
    }
}
