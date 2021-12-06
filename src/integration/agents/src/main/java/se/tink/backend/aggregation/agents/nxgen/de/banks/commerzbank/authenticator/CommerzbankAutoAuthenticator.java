package se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.authenticator;

import com.google.common.base.Strings;
import java.security.KeyPair;
import java.util.Optional;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.CommerzbankApiClient;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.CommerzbankConstants;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.CommerzbankConstants.Error;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.CommerzbankConstants.Storage;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.CommerzbankConstants.Values;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.authenticator.entities.LoginInfoEntity;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.authenticator.rpc.LoginResponse;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.entities.ErrorMessageEntity;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.authenticator.AutoAuthenticator;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.cryptography.RSA;
import se.tink.libraries.encoding.EncodingUtils;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class CommerzbankAutoAuthenticator implements AutoAuthenticator {
    private final PersistentStorage persistentStorage;
    private final CommerzbankApiClient apiClient;
    private final Credentials credentials;

    public CommerzbankAutoAuthenticator(
            Credentials credentials,
            PersistentStorage persistentStorage,
            CommerzbankApiClient apiClient) {
        this.credentials = credentials;
        this.persistentStorage = persistentStorage;
        this.apiClient = apiClient;
    }

    @Override
    public void autoAuthenticate()
            throws SessionException, BankServiceException, AuthorizationException {

        String username = this.credentials.getField(Field.Key.USERNAME);
        String password = this.credentials.getField(Field.Key.PASSWORD);

        String appId = persistentStorage.get(Storage.APP_ID);
        String keyPairFromStorage = persistentStorage.get(Storage.KEY_PAIR);

        // The users who registered before the SCA introduction will not have appID and keyPair
        // stored, which is required for auto authentication. Force session expiry for these users
        // to put them in the manual authentication flow.
        if (Strings.isNullOrEmpty(appId) || Strings.isNullOrEmpty(keyPairFromStorage)) {
            throw SessionError.SESSION_EXPIRED.exception();
        }

        LoginResponse loginResponse = apiClient.autoLogin(username, password, appId);

        if (loginResponse.getError() != null) {
            handleAutoLoginError(loginResponse);
        }

        LoginInfoEntity loginInfoEntity = loginResponse.getLoginInfoEntity();
        credentials.setSensitivePayload(
                CommerzbankConstants.LOGIN_INFO_ENTITY,
                SerializationUtils.serializeToString(loginInfoEntity));

        if (!loginInfoEntity.isChallengeStatus()) {
            throw new IllegalStateException(
                    String.format(
                            "Excepted login status to be %s, but it was %s.",
                            Values.CHALLENGE, loginInfoEntity.getLoginStatus()));
        }

        String challenge = loginInfoEntity.getChallenge();

        if (Strings.isNullOrEmpty(challenge)) {
            throw new IllegalStateException(
                    "Login status was challenge, but no challenge was provided.");
        }

        KeyPair keyPair = SerializationUtils.deserializeKeyPair(keyPairFromStorage);
        byte[] signature = RSA.signSha256(keyPair.getPrivate(), challenge.getBytes());

        apiClient.approveChallenge(appId, EncodingUtils.encodeAsBase64String(signature));

        apiClient.updateAppRegistration(appId);
    }

    private void handleAutoLoginError(LoginResponse loginResponse) throws SessionException {
        Optional<ErrorMessageEntity> errorMessage = loginResponse.getError().getErrorMessage();

        if (!errorMessage.isPresent()) {
            throw new IllegalStateException("Login failed without error description present.");
        }

        if (Error.ACCOUNT_SESSION_ACTIVE_ERROR.equals(errorMessage.get().getMessageId())) {
            throw SessionError.SESSION_ALREADY_ACTIVE.exception();
        }

        throw new IllegalStateException(
                String.format(
                        "Login failed with unknown error message: %s",
                        errorMessage.get().getMessageId()));
    }
}
